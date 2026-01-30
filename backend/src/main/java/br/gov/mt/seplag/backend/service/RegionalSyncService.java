package br.gov.mt.seplag.backend.service;

import br.gov.mt.seplag.backend.dto.RegionalExternaDTO;
import br.gov.mt.seplag.backend.model.Regional;
import br.gov.mt.seplag.backend.repository.RegionalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service para sincronização de Regionais com API externa
 *
 * ALGORITMO O(n) - Complexidade Linear
 *
 * API: https://integrador-argus-api.geia.vip/v1/regionais
 *
 * ESTRATÉGIA COM MD5 HASH:
 * 1. Buscar regionais do banco (n registros) - O(n)
 * 2. Criar HashMap por codigoExterno (ID da API) - O(n)
 * 3. Buscar regionais da API (m registros) - O(m)
 * 4. Para cada registro da API:
 *    - Calcular hash MD5(nome) - O(1)
 *    - Buscar no HashMap - O(1)
 *    - Comparar hash: se diferente → houve mudança
 * 5. Desativar regionais removidas da API - O(n)
 *
 * Complexidade Total: O(n + m) = O(n) onde n ≈ m
 *
 * VANTAGEM DO HASH:
 * - Detecta mudanças sem comparar campo a campo
 * - Escalável para muitos campos
 * - Comparação de string (hash) em O(1)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegionalSyncService {

    private final RegionalRepository regionalRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${external.regionais-api-url}")
    private String apiUrl;

    /**
     * Sincronização automática a cada 1 hora
     * Cron: 0 0 * * * * = a cada hora cheia
     */
    @Scheduled(cron = "0 0 * * * *")
    public void syncAutomatico() {
        log.info("=== SINCRONIZAÇÃO AUTOMÁTICA DE REGIONAIS ===");
        sincronizar();
    }

    /**
     * Sincronização manual (via endpoint)
     */
    @Transactional
    public Map<String, Object> sincronizar() {
        long startTime = System.currentTimeMillis();
        log.info("╔════════════════════════════════════════════════════════╗");
        log.info("║  INICIANDO SINCRONIZAÇÃO O(n) COM MD5 HASH            ║");
        log.info("╚════════════════════════════════════════════════════════╝");

        // ═══════════════════════════════════════════════════════════
        // PASSO 1: Buscar regionais do banco - O(n)
        // ═══════════════════════════════════════════════════════════
        log.info("→ PASSO 1: Buscando regionais do banco de dados...");
        List<Regional> regionaisLocal = regionalRepository.findAllForSync();
        log.info("  ✓ Regionais no banco: {}", regionaisLocal.size());

        // ═══════════════════════════════════════════════════════════
        // PASSO 2: Criar HashMap por código externo - O(n)
        // ⚡ ESTE É O SEGREDO DO O(n) - LOOKUP EM O(1)!
        // ═══════════════════════════════════════════════════════════
        log.info("→ PASSO 2: Criando HashMap para lookup O(1)...");
        Map<Integer, Regional> regionaisMap = new HashMap<>();
        for (Regional regional : regionaisLocal) {
            regionaisMap.put(regional.getCodigoExterno(), regional);
        }
        log.info("  ✓ HashMap criado com {} entradas", regionaisMap.size());

        // ═══════════════════════════════════════════════════════════
        // PASSO 3: Buscar regionais da API externa - O(m)
        // ═══════════════════════════════════════════════════════════
        log.info("→ PASSO 3: Buscando regionais da API externa...");
        log.info("  URL: {}", apiUrl);
        List<RegionalExternaDTO> regionaisExternas = buscarRegionaisAPI();
        log.info("  ✓ Regionais da API: {}", regionaisExternas.size());

        if (regionaisExternas.isEmpty()) {
            log.warn("  ⚠ Nenhuma regional retornada da API!");
            return criarResultadoErro("API retornou lista vazia");
        }

        // Contadores
        int criados = 0;
        int atualizados = 0;
        int desativados = 0;
        int semMudancas = 0;

        // OTIMIZAÇÃO: Listas para batch save (reduz N transações para 1)
        List<Regional> regionaisParaSalvar = new ArrayList<>();

        // ═══════════════════════════════════════════════════════════
        // PASSO 4: Processar regionais da API - O(m)
        // ═══════════════════════════════════════════════════════════
        log.info("→ PASSO 4: Processando regionais da API com MD5 hash...");
        Set<Integer> codigosProcessados = new HashSet<>();

        for (RegionalExternaDTO externaDTO : regionaisExternas) {
            Integer codigoExterno = externaDTO.getId();
            codigosProcessados.add(codigoExterno);

            // ═══════════════════════════════════════════════════════
            // CALCULAR HASH MD5 - O(1)
            // Vantagem: Detecta qualquer mudança nos dados
            // ═══════════════════════════════════════════════════════
            String hashExterno = calcularMD5(externaDTO.getNome());

            // ⚡ LOOKUP O(1) no HashMap - NÃO É O(n)!
            Regional regional = regionaisMap.get(codigoExterno);

            if (regional == null) {
                // ═══════════════════════════════════════════════
                // CRIAR nova regional
                // ═══════════════════════════════════════════════
                regional = Regional.builder()
                        .codigoExterno(codigoExterno)
                        .nome(externaDTO.getNome())
                        .externalHash(hashExterno)
                        .ativa(true)
                        .ultimaSincronizacao(LocalDateTime.now())
                        .build();

                regionaisParaSalvar.add(regional);
                criados++;
                log.debug("  + CRIADA: [{}] {} (hash: {})",
                        codigoExterno, externaDTO.getNome(), hashExterno.substring(0, 8) + "...");

            } else {
                // ═══════════════════════════════════════════════
                // VERIFICAR MUDANÇAS VIA HASH - O(1)
                // Se hash diferente → dados mudaram
                // ═══════════════════════════════════════════════
                String hashLocal = regional.getExternalHash();
                boolean hashMudou = !Objects.equals(hashLocal, hashExterno);
                boolean estaInativa = !regional.getAtiva();

                if (hashMudou || estaInativa) {
                    if (hashMudou) {
                        log.debug("  ↻ Hash diferente: [{}] '{}' → '{}'",
                                codigoExterno,
                                hashLocal != null ? hashLocal.substring(0, 8) + "..." : "null",
                                hashExterno.substring(0, 8) + "...");
                        log.debug("    Nome: '{}' → '{}'", regional.getNome(), externaDTO.getNome());
                    }

                    if (estaInativa) {
                        log.debug("  ↑ REATIVADA: [{}] {}", codigoExterno, regional.getNome());
                    }

                    // Atualizar dados
                    regional.setNome(externaDTO.getNome());
                    regional.setExternalHash(hashExterno);
                    regional.setAtiva(true);
                    regional.setUltimaSincronizacao(LocalDateTime.now());

                    regionaisParaSalvar.add(regional);
                    atualizados++;

                } else {
                    // Hash igual → sem mudanças
                    semMudancas++;
                }
            }
        }

        // ═══════════════════════════════════════════════════════════
        // PASSO 5: Desativar regionais removidas da API - O(n)
        // ═══════════════════════════════════════════════════════════
        log.info("→ PASSO 5: Verificando regionais removidas da API...");
        for (Regional regional : regionaisLocal) {
            if (!codigosProcessados.contains(regional.getCodigoExterno()) && regional.getAtiva()) {
                regional.setAtiva(false);
                regional.setUltimaSincronizacao(LocalDateTime.now());
                regionaisParaSalvar.add(regional);
                desativados++;
                log.warn("  ✗ DESATIVADA: [{}] {} (não existe mais na API)",
                        regional.getCodigoExterno(), regional.getNome());
            }
        }

        // ═══════════════════════════════════════════════════════════
        // PASSO 6: Batch Save - Uma única transação para todas as alterações
        // ═══════════════════════════════════════════════════════════
        if (!regionaisParaSalvar.isEmpty()) {
            log.info("→ PASSO 6: Salvando {} registros em batch...", regionaisParaSalvar.size());
            regionalRepository.saveAll(regionaisParaSalvar);
            log.info("  ✓ Batch save concluído");
        }

        // ═══════════════════════════════════════════════════════════
        // RESULTADO
        // ═══════════════════════════════════════════════════════════
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        log.info("╔════════════════════════════════════════════════════════╗");
        log.info("║  SINCRONIZAÇÃO CONCLUÍDA (COM MD5 HASH)                ║");
        log.info("╠════════════════════════════════════════════════════════╣");
        log.info("║  ✓ Criadas: {:>5}                                      ║", criados);
        log.info("║  ↻ Atualizadas: {:>5}                                  ║", atualizados);
        log.info("║  ✗ Desativadas: {:>5}                                  ║", desativados);
        log.info("║  = Sem mudanças: {:>5}                                 ║", semMudancas);
        log.info("║  ═ Total API: {:>5}                                    ║", regionaisExternas.size());
        log.info("╠════════════════════════════════════════════════════════╣");
        log.info("║  ⏱ Tempo: {}ms                                    ║", String.format("%5d", duration));
        log.info("║  ⚡ Complexidade: O(n) onde n = {}                ║", String.format("%5d", regionaisExternas.size()));
        log.info("╚════════════════════════════════════════════════════════╝");

        // Análise de complexidade
        String analise = analisarComplexidade(
                regionaisLocal.size(),
                regionaisExternas.size()
        );

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("sucesso", true);
        resultado.put("mensagem", "Sincronização concluída com sucesso");

        Map<String, Integer> stats = new HashMap<>();
        stats.put("criados", criados);
        stats.put("atualizados", atualizados);
        stats.put("desativados", desativados);
        stats.put("semMudancas", semMudancas);
        stats.put("totalAPI", regionaisExternas.size());
        stats.put("totalBanco", regionaisLocal.size());
        stats.put("duracaoMs", (int) duration);
        resultado.put("estatisticas", stats);

        resultado.put("analiseComplexidade", analise);

        return resultado;
    }

    /**
     * Calcular hash MD5 de uma string.
     *
     * OBJETIVO: Detectar mudanças nos dados sem comparar campo a campo.
     *
     * EXEMPLO:
     * - calcularMD5("REGIONAL DE CUIABÁ") → "a1b2c3d4e5f6..."
     * - Se nome mudar, hash muda → detectamos a alteração
     *
     * @param texto String para calcular hash
     * @return Hash MD5 em hexadecimal (32 caracteres)
     */
    private String calcularMD5(String texto) {
        if (texto == null) {
            return "";
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(texto.getBytes(StandardCharsets.UTF_8));

            // Converter bytes para hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            // MD5 sempre disponível em qualquer JVM
            log.error("Erro ao calcular MD5: {}", e.getMessage());
            return texto; // Fallback: usar próprio texto como "hash"
        }
    }

    /**
     * Buscar regionais da API externa
     */
    private List<RegionalExternaDTO> buscarRegionaisAPI() {
        try {
            WebClient webClient = webClientBuilder.build();

            log.info("  → Chamando API externa GET {} ...", apiUrl);
            List<RegionalExternaDTO> regionais = webClient.get()
                    .uri(apiUrl)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<RegionalExternaDTO>>() {})
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (regionais == null) {
                log.error("  ✗ API retornou null!");
                return Collections.emptyList();
            }

            log.info("  ✓ {} regionais recebidas da API", regionais.size());
            return regionais;

        } catch (Exception e) {
            log.error("  ✗ ERRO ao buscar regionais da API: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            log.error("  Causa: verifique conectividade (rede/DNS/SSL) e URL: {}", apiUrl, e);
            return Collections.emptyList();
        }
    }

    /**
     * Criar resultado de erro
     */
    private Map<String, Object> criarResultadoErro(String mensagem) {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("sucesso", false);
        resultado.put("mensagem", mensagem);
        resultado.put("estatisticas", Collections.emptyMap());
        resultado.put("analiseComplexidade", "N/A");
        return resultado;
    }

    /**
     * Análise de complexidade detalhada
     */
    public String analisarComplexidade(int n, int m) {
        int totalOps = n + n + m + m + n; // Buscar + HashMap + API + Processar + Desativar
        int opsQuadraticas = n * m; // Algoritmo ruim O(n*m)
        double ganho = opsQuadraticas > 0 ? (double) opsQuadraticas / totalOps : 0;

        return String.format(
                        "╔════════════════════════════════════════════════════════╗\n" +
                        "║  ANÁLISE DE COMPLEXIDADE - ALGORITMO O(n) COM MD5     ║\n" +
                        "╠════════════════════════════════════════════════════════╣\n" +
                        "║  Regionais no banco: %5d                               ║\n" +
                        "║  Regionais na API: %5d                                 ║\n" +
                        "╠════════════════════════════════════════════════════════╣\n" +
                        "║  OPERAÇÕES POR PASSO:                                  ║\n" +
                        "║  1. Buscar banco: O(n) = %5d ops                       ║\n" +
                        "║  2. Criar HashMap: O(n) = %5d ops                      ║\n" +
                        "║  3. Buscar API: O(m) = %5d ops                         ║\n" +
                        "║  4. Processar API: O(m) = %5d ops                      ║\n" +
                        "║     └─ Calcular MD5: O(1)                              ║\n" +
                        "║     └─ Lookup HashMap: O(1)                            ║\n" +
                        "║     └─ Comparar hash: O(1)                             ║\n" +
                        "║  5. Desativar: O(n) = %5d ops                          ║\n" +
                        "╠════════════════════════════════════════════════════════╣\n" +
                        "║  TOTAL: O(n+m) = %5d operações                         ║\n" +
                        "╠════════════════════════════════════════════════════════╣\n" +
                        "║  COMPARAÇÃO:                                           ║\n" +
                        "║  • Algoritmo RUIM O(n×m): %,10d ops                    ║\n" +
                        "║  • Algoritmo BOM O(n): %,10d ops                       ║\n" +
                        "║  • GANHO: %.2fx mais rápido! 🚀                        ║\n" +
                        "╚════════════════════════════════════════════════════════╝",
                n, m, n, n, m, m, n, totalOps, opsQuadraticas, totalOps, ganho
        );
    }
}
