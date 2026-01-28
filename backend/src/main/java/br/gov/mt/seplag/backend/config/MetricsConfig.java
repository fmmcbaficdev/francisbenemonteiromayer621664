package br.gov.mt.seplag.backend.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração de métricas para observabilidade
 * Integra com Prometheus/Grafana para monitoramento
 */
@Configuration
public class MetricsConfig {

    /**
     * Customiza o registry de métricas com tags comuns
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags(List.of(
                        Tag.of("application", "seplag-backend"),
                        Tag.of("team", "seplag-mt")
                ));
    }

    /**
     * Habilita o aspecto @Timed para medir tempo de execução de métodos
     * Uso: @Timed("nome.da.metrica")
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
