package br.gov.mt.seplag.backend.config;

import br.gov.mt.seplag.backend.service.RegionalSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Executa a sincronização de regionais uma vez após a aplicação subir.
 * Assim o banco já fica populado com todas as regionais da API externa,
 * sem depender do usuário clicar em "Sincronizar" no frontend.
 *
 * Se a API estiver indisponível (rede, timeout), apenas loga o erro e
 * a aplicação continua com as 3 regionais do seed (Flyway V2).
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class RegionalSyncStartupRunner implements ApplicationRunner {

    private final RegionalSyncService regionalSyncService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("=== Executando sincronização inicial de regionais (startup) ===");
        try {
            Map<String, Object> resultado = regionalSyncService.sincronizar();
            Boolean sucesso = (Boolean) resultado.get("sucesso");
            if (Boolean.TRUE.equals(sucesso)) {
                log.info("Sincronização inicial de regionais concluída com sucesso");
            } else {
                log.warn("Sincronização inicial retornou: {}", resultado.get("mensagem"));
            }
        } catch (Exception e) {
            log.warn("Sincronização inicial de regionais falhou (aplicação continua). " +
                    "Clique em 'Sincronizar' na tela Regionais ou aguarde o cron horário. Erro: {}", e.getMessage());
        }
    }
}
