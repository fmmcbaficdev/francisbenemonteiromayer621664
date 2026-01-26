package br.gov.mt.seplag.backend.service;

import br.gov.mt.seplag.backend.dto.RegionalExternaDTO;
import br.gov.mt.seplag.backend.dto.SyncResultDTO;
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

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service para sincronização de Regionais com API externa
 *
 * ALGORITMO O(n) - Complexidade Linear
 *
 * API: https://integrador-argus-api.geia.vip/v1/regionais
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
    public SyncResultDTO sincronizar() {
        long startTime = System.currentTimeMillis();
        log.info("╔════════════════════════════════════════════════════════╗");
        log.info("║  INICIANDO SINCRONIZAÇÃO O(n) - REGIONAIS             ║");
        log.info("╚════════════════════════════════════════════════════════╝");

        // PASSO 1: Buscar regionais do banco - O(n)
        log.info("→ PASSO 1: Buscando regionais do banco de dados...");
        List<Regional> regionaisLocal = regionalRepository.findAllForSync();
        log.info("  ✓ Regionais no banco: {}", regionaisLocal.size());

        // PASSO 2: Criar HashMap por código externo - O(n)
        log.info("→ PASSO 2: Criando HashMap para lookup O(1)...");
        Map<Integer, Regional> regionaisMap = new HashMap<>();
        for (Regional regional : regionaisLocal) {
            regionaisMap.put(regional.getCodigoExterno(), regional);
        }
        log.info("  ✓ HashMap criado com {} entradas", regionaisMap.size());

        // PASSO 3: Buscar regionais da API externa - O(m)
        log.info("→ PASSO 3: Buscando regionais da API externa...");
        log.info("  URL: {}", apiUrl);
        List<RegionalExternaDTO> regionaisExternas = buscarRegionaisAPI();
        log.info("  ✓ Regionais da API: {}", regionaisExternas.size());

        if (regionaisExternas.isEmpty()) {
            log.warn("  ⚠ Nenhuma regional retornada da API!");
            return SyncResultDTO.erro("API retornou lista vazia");
        }

        // Contadores
        int criados = 0;
        int atualizados = 0;
        int desativados = 0;
        int semMudancas = 0;

        List<Regional> regionaisParaSalvar = new ArrayList<>();

        // PASSO 4: Processar regionais da API - O(m)
        log.info("→ PASSO 4: Processando regionais da API...");
        Set<Integer> codigosProcessados = new HashSet<>();

        for (RegionalExternaDTO externaDTO : regionaisExternas) {
            Integer codigoExterno = externaDTO.id();  // Record: usa método direto
            codigosProcessados.add(codigoExterno);

            Regional regional = regionaisMap.get(codigoExterno);

            if (regional == null) {
                // CRIAR nova regional
                regional = Regional.builder()
                        .codigoExterno(codigoExterno)
                        .nome(externaDTO.nome())  // Record: usa método direto
                        .ativa(true)
                        .ultimaSincronizacao(LocalDateTime.now())
                        .build();

                regionaisParaSalvar.add(regional);
                criados++;
                log.debug("  + CRIADA: [{}] {}", codigoExterno, externaDTO.nome());

            } else {
                // ATUALIZAR regional existente
                boolean modificado = false;

                if (!Objects.equals(regional.getNome(), externaDTO.nome())) {
                    log.debug("  ↻ Nome alterado: [{}] '{}' → '{}'",
                            codigoExterno, regional.getNome(), externaDTO.nome());
                    regional.setNome(externaDTO.nome());
                    modificado = true;
                }

                if (!regional.getAtiva()) {
                    regional.setAtiva(true);
                    modificado = true;
                    log.debug("  ↑ REATIVADA: [{}] {}", codigoExterno, regional.getNome());
                }

                regional.setUltimaSincronizacao(LocalDateTime.now());

                if (modificado) {
                    regionaisParaSalvar.add(regional);
                    atualizados++;
                    log.debug("  ✓ ATUALIZADA: [{}] {}", codigoExterno, regional.getNome());
                } else {
                    semMudancas++;
                }
            }
        }

        // PASSO 5: Desativar regionais removidas da API - O(n)
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

        // PASSO 6: Batch Save
        if (!regionaisParaSalvar.isEmpty()) {
            log.info("→ PASSO 6: Salvando {} registros em batch...", regionaisParaSalvar.size());
            regionalRepository.saveAll(regionaisParaSalvar);
            log.info("  ✓ Batch save concluído");
        }

        // RESULTADO
        long duration = System.currentTimeMillis() - startTime;

        log.info("╔════════════════════════════════════════════════════════╗");
        log.info("║  SINCRONIZAÇÃO CONCLUÍDA                               ║");
        log.info("║  ✓ Criadas: {} | ↻ Atualizadas: {} | ✗ Desativadas: {} ║",
                       criados, atualizados, desativados);
        log.info("║  ⏱ Tempo: {}ms | ⚡ Complexidade: O(n)                  ║", duration);
        log.info("╚════════════════════════════════════════════════════════╝");

        return new SyncResultDTO(
                true,
                "Sincronização concluída com sucesso",
                criados,
                atualizados,
                desativados,
                semMudancas,
                regionaisExternas.size(),
                regionaisLocal.size(),
                duration
        );
    }

    /**
     * Buscar regionais da API externa
     */
    private List<RegionalExternaDTO> buscarRegionaisAPI() {
        try {
            WebClient webClient = webClientBuilder.build();

            log.debug("  → Realizando chamada HTTP GET...");
            List<RegionalExternaDTO> regionais = webClient.get()
                    .uri(apiUrl)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<RegionalExternaDTO>>() {})
                    .block();

            if (regionais == null) {
                log.error("  ✗ API retornou null!");
                return Collections.emptyList();
            }

            log.debug("  ✓ {} regionais recebidas da API", regionais.size());
            return regionais;

        } catch (Exception e) {
            log.error("  ✗ ERRO ao buscar regionais da API: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
