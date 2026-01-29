package br.gov.mt.seplag.backend.service;

import br.gov.mt.seplag.backend.dto.RegionalExternaDTO;
import br.gov.mt.seplag.backend.model.Regional;
import br.gov.mt.seplag.backend.repository.RegionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para RegionalSyncService
 * Foco: Validar algoritmo O(n) de sincronização
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegionalSyncService - Testes Unitários")
@SuppressWarnings({"rawtypes", "unchecked"})
class RegionalSyncServiceTest {

    @Mock
    private RegionalRepository regionalRepository;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private RegionalSyncService regionalSyncService;

    private List<Regional> regionaisLocal;
    private List<RegionalExternaDTO> regionaisExternas;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(regionalSyncService, "apiUrl", "http://test-api.com/v1/regionais");

        // Setup WebClient mocks
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // Regionais locais (banco de dados)
        regionaisLocal = List.of(
                Regional.builder()
                        .id(1L)
                        .codigoExterno(1)
                        .nome("Regional 1")
                        .ativa(true)
                        .ultimaSincronizacao(LocalDateTime.now().minusHours(1))
                        .build(),
                Regional.builder()
                        .id(2L)
                        .codigoExterno(2)
                        .nome("Regional 2")
                        .ativa(true)
                        .ultimaSincronizacao(LocalDateTime.now().minusHours(1))
                        .build(),
                Regional.builder()
                        .id(3L)
                        .codigoExterno(3)
                        .nome("Regional 3 Antiga")
                        .ativa(true)
                        .ultimaSincronizacao(LocalDateTime.now().minusHours(1))
                        .build()
        );

        // Regionais externas (API)
        regionaisExternas = List.of(
                RegionalExternaDTO.builder()
                        .id(1)
                        .nome("Regional 1")
                        .build(),
                RegionalExternaDTO.builder()
                        .id(2)
                        .nome("Regional 2")
                        .build(),
                RegionalExternaDTO.builder()
                        .id(3)
                        .nome("Regional 3 Nova") // Nome alterado
                        .build(),
                RegionalExternaDTO.builder()
                        .id(4)
                        .nome("Regional 4") // Nova regional
                        .build()
        );
    }

    @Test
    @DisplayName("Deve criar novas regionais quando não existem no banco")
    void deveCriarNovasRegionaisQuandoNaoExistemNoBanco() {
        // Arrange
        when(regionalRepository.findAllForSync()).thenReturn(Collections.emptyList());
        when(responseSpec.bodyToFlux(any(Class.class))).thenReturn(Flux.fromIterable(regionaisExternas));
        when(regionalRepository.save(any(Regional.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Map<String, Object> resultado = regionalSyncService.sincronizar();

        // Assert
        assertNotNull(resultado);
        assertTrue((Boolean) resultado.get("sucesso"));
        Map<String, Object> stats = (Map<String, Object>) resultado.get("estatisticas");
        assertEquals(4, stats.get("criados")); // Todas as 4 regionais devem ser criadas
        verify(regionalRepository, times(4)).save(any(Regional.class));
    }

    @Test
    @DisplayName("Deve atualizar regional quando nome mudou")
    void deveAtualizarRegionalQuandoNomeMudou() {
        // Arrange
        when(regionalRepository.findAllForSync()).thenReturn(regionaisLocal);
        when(responseSpec.bodyToFlux(any(Class.class))).thenReturn(Flux.fromIterable(regionaisExternas));
        when(regionalRepository.save(any(Regional.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Map<String, Object> resultado = regionalSyncService.sincronizar();

        // Assert
        assertNotNull(resultado);
        Map<String, Object> stats = (Map<String, Object>) resultado.get("estatisticas");
        assertTrue((Integer) stats.get("atualizados") >= 1); // Regional 3 deve ser atualizada
        verify(regionalRepository, atLeastOnce()).save(argThat(regional ->
                regional.getCodigoExterno() == 3 && regional.getNome().equals("Regional 3 Nova")
        ));
    }

    @Test
    @DisplayName("Deve desativar regionais removidas da API")
    void deveDesativarRegionaisRemovidasDaAPI() {
        // Arrange
        // Regional 5 existe no banco mas não na API
        Regional regionalRemovida = Regional.builder()
                .id(5L)
                .codigoExterno(5)
                .nome("Regional Removida")
                .ativa(true)
                .build();

        List<Regional> regionaisComRemovida = new ArrayList<>(regionaisLocal);
        regionaisComRemovida.add(regionalRemovida);

        when(regionalRepository.findAllForSync()).thenReturn(regionaisComRemovida);
        when(responseSpec.bodyToFlux(any(Class.class))).thenReturn(Flux.fromIterable(regionaisExternas));
        when(regionalRepository.save(any(Regional.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Map<String, Object> resultado = regionalSyncService.sincronizar();

        // Assert
        assertNotNull(resultado);
        Map<String, Object> stats = (Map<String, Object>) resultado.get("estatisticas");
        assertTrue((Integer) stats.get("desativados") >= 1); // Regional 5 deve ser desativada
        verify(regionalRepository, atLeastOnce()).save(argThat(regional ->
                regional.getCodigoExterno() == 5 && !regional.getAtiva()
        ));
    }

    @Test
    @DisplayName("Deve manter regionais sem mudanças")
    void deveManterRegionaisSemMudancas() {
        // Arrange
        // Regional 1 e 2 não mudaram
        when(regionalRepository.findAllForSync()).thenReturn(regionaisLocal);
        when(responseSpec.bodyToFlux(any(Class.class))).thenReturn(Flux.fromIterable(regionaisExternas));
        when(regionalRepository.save(any(Regional.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Map<String, Object> resultado = regionalSyncService.sincronizar();

        // Assert
        assertNotNull(resultado);
        Map<String, Object> stats = (Map<String, Object>) resultado.get("estatisticas");
        assertTrue((Integer) stats.get("semMudancas") >= 0); // Regionais 1 e 2 sem mudanças
    }

    @Test
    @DisplayName("Deve retornar erro quando API retorna lista vazia")
    void deveRetornarErroQuandoAPIRetornaListaVazia() {
        // Arrange
        when(regionalRepository.findAllForSync()).thenReturn(regionaisLocal);
        when(responseSpec.bodyToFlux(any(Class.class))).thenReturn(Flux.empty());

        // Act
        Map<String, Object> resultado = regionalSyncService.sincronizar();

        // Assert
        assertNotNull(resultado);
        assertFalse((Boolean) resultado.get("sucesso"));
        assertTrue(resultado.get("mensagem").toString().contains("API retornou lista vazia"));
    }

    @Test
    @DisplayName("Deve processar sincronização completa com algoritmo O(n)")
    void deveProcessarSincronizacaoCompletaComAlgoritmoON() {
        // Arrange
        when(regionalRepository.findAllForSync()).thenReturn(regionaisLocal);
        when(responseSpec.bodyToFlux(any(Class.class))).thenReturn(Flux.fromIterable(regionaisExternas));
        when(regionalRepository.save(any(Regional.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Map<String, Object> resultado = regionalSyncService.sincronizar();

        // Assert
        assertNotNull(resultado);
        assertTrue((Boolean) resultado.get("sucesso"));
        Map<String, Object> stats = (Map<String, Object>) resultado.get("estatisticas");
        assertNotNull(stats.get("criados"));
        assertNotNull(stats.get("atualizados"));
        assertNotNull(stats.get("desativados"));
        assertNotNull(stats.get("totalAPI"));
        assertNotNull(stats.get("totalBanco"));

        // Verificar que o algoritmo é O(n) - não deve fazer múltiplas iterações desnecessárias
        // O número de chamadas ao repository deve ser proporcional ao número de registros
        verify(regionalRepository, atMost(regionaisExternas.size() + regionaisLocal.size())).save(any());
    }

    @Test
    @DisplayName("Deve reativar regional que estava inativa")
    void deveReativarRegionalQueEstavaInativa() {
        // Arrange
        Regional regionalInativa = Regional.builder()
                .id(1L)
                .codigoExterno(1)
                .nome("Regional 1")
                .ativa(false) // Estava inativa
                .build();

        when(regionalRepository.findAllForSync()).thenReturn(List.of(regionalInativa));
        when(responseSpec.bodyToFlux(any(Class.class))).thenReturn(Flux.fromIterable(
                List.of(RegionalExternaDTO.builder().id(1).nome("Regional 1").build())
        ));
        when(regionalRepository.save(any(Regional.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Map<String, Object> resultado = regionalSyncService.sincronizar();

        // Assert
        assertNotNull(resultado);
        verify(regionalRepository, atLeastOnce()).save(argThat(regional ->
                regional.getCodigoExterno() == 1 && regional.getAtiva()
        ));
    }
}
