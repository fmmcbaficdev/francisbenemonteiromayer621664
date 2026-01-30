# Requisitos Sênior — Reavaliação e Como Testar (Avaliador)

**Edital:** PSS 001/2026/SEPLAG — Anexo II-C Full Stack Sênior  

Este documento reavalia o desenvolvimento de cada requisito **apenas para Sênior** e descreve **como o avaliador pode testar** cada um.

---

## a) Health Checks e Liveness/Readiness

### Desenvolvimento

| Onde está | Descrição |
|-----------|-----------|
| **Backend** | Spring Boot Actuator com `management.health.livenessstate.enabled=true` e `readinessstate.enabled=true`. Endpoints: `/actuator/health`, `/actuator/health/liveness`, `/actuator/health/readiness`. |
| **application.properties** | `management.endpoint.health.probes.enabled=true`. |
| **Docker** | Healthcheck do container backend usa `curl -f http://localhost:8080/actuator/health/liveness`. |
| **Frontend** | Container com healthcheck `curl -f http://localhost/` (Nginx). |

**Status:** ✅ Atendido (backend e front com verificação de saúde).

### Como o avaliador pode testar

1. Subir o projeto: `docker compose up -d` e aguardar ~1–2 min.
2. **Liveness (backend):**
   ```bash
   curl -s http://localhost:8080/actuator/health/liveness
   ```
   **Esperado:** JSON com `"status":"UP"` (ou `"livenessState":"CORRECT"`).
3. **Readiness (backend):**
   ```bash
   curl -s http://localhost:8080/actuator/health/readiness
   ```
   **Esperado:** JSON com status UP/CORRECT.
4. **Health completo:**
   ```bash
   curl -s http://localhost:8080/actuator/health
   ```
   **Esperado:** Inclui `liveness` e `readiness` (e, se configurado, db, disk, etc.).
5. **Frontend:** Acessar http://localhost:3000 — se a página carregar, o healthcheck do container está ok (ou conferir `docker compose ps` e ver se o frontend está "healthy").

---

## b) Testes unitários

### Desenvolvimento

| Camada | Onde está | Descrição |
|--------|-----------|-----------|
| **Backend** | `backend/src/test/java/` | JUnit 5 + Mockito: `AlbumServiceTest`, `ArtistaServiceTest`, `MinIOServiceTest`, `RegionalSyncServiceTest`, `JwtServiceTest`; integração: `AlbumControllerIntegrationTest`, `ArtistaControllerIntegrationTest`, `AuthControllerIntegrationTest`. |
| **Frontend** | `frontend/src/` | Vitest + React Testing Library: `Loading.test.tsx` (3 testes), `Login.test.tsx` (2 testes). Scripts: `npm test`, `npm run test:coverage`. |

**Status:** ✅ Atendido (testes unitários e de integração no backend; testes unitários no frontend).

### Como o avaliador pode testar

1. **Backend:**
   ```bash
   cd backend
   ./mvnw test
   ```
   **Esperado:** Todos os testes passando (incluindo serviços e controllers).
2. **Cobertura backend (opcional):**
   ```bash
   ./mvnw test jacoco:report
   ```
   Relatório em `target/site/jacoco/index.html`.
3. **Frontend:**
   ```bash
   cd frontend
   npm install
   npm test
   ```
   **Esperado:** 5 testes passando (Loading + Login).
4. **Cobertura frontend (opcional):**
   ```bash
   npm run test:coverage
   ```
   Relatório em `coverage/index.html` (ou no terminal).

---

## c) WebSocket na API e exibir no front notificações a cada novo álbum cadastrado

### Desenvolvimento

| Camada | Onde está | Descrição |
|--------|-----------|-----------|
| **Backend** | `WebSocketConfig.java` | STOMP sobre SockJS; endpoint `/ws`; tópico `/topic/albums`. |
| **Backend** | `WebSocketNotificationService.java` | `notificarNovoAlbum(Album)` — envia DTO com tipo `NOVO_ALBUM`, título e id. |
| **Backend** | `AlbumService.java` | Após criar (e atualizar/remover) álbum, chama `webSocketService.notificarNovoAlbum(album)`. |
| **Frontend** | `websocket.ts` | Cliente STOMP/SockJS; subscribe em `/topic/albums`; parse do payload e callback. |
| **Frontend** | `useWebSocket.ts` | Hook que conecta e repassa notificações; handler padrão mostra toast e atualiza `AlbumStore`. |
| **Frontend** | `Layout.tsx` | Usa `useWebSocket()` para conectar ao entrar nas telas autenticadas. |

**Status:** ✅ Atendido (notificação a cada novo álbum cadastrado e exibição no front).

### Como o avaliador pode testar

1. Abrir o frontend em **duas abas** (ou dois navegadores): http://localhost:3000.
2. Fazer login em ambas (admin / admin123).
3. Em uma aba: ir em **Álbuns** → **Novo álbum** → preencher e salvar.
4. Na **outra aba** (sem recarregar): deve aparecer um **toast** tipo "Novo álbum: [título]" e a lista de álbuns pode atualizar sozinha (se a tela consumir o store atualizado).
5. **Opcional:** abrir DevTools (F12) → Console e ver mensagens de "WebSocket conectado" e, ao salvar álbum, mensagem relacionada à notificação.
6. **Opcional:** no backend, ao criar álbum, deve aparecer log tipo "Enviando notificação WebSocket: NOVO_ALBUM".

---

## d) Rate limit: máximo 10 requisições por minuto por usuário na API

### Desenvolvimento

| Onde está | Descrição |
|-----------|-----------|
| **RateLimitInterceptor.java** | Bucket4j; bucket por usuário autenticado (`user:username`) ou por IP (não autenticado); 10 tokens/minuto (configurável por `rate-limit.requests-per-minute`). |
| **WebConfig.java** | Registra o interceptor em `/v1/**`, exceto `/v1/auth/**`. |
| **application.properties** | `rate-limit.requests-per-minute=10`. Endpoints públicos (auth, actuator/health, swagger) não contam. |
| **Resposta ao exceder** | HTTP 429; headers `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`. |

**Status:** ✅ Atendido (10 req/min por usuário, excluindo auth e health).

### Como o avaliador pode testar

1. Fazer login via Swagger ou frontend e obter o token JWT (ex.: POST `/v1/auth/login` com `{"username":"admin","password":"admin123"}`).
2. Enviar **mais de 10 requisições autenticadas** em menos de 1 minuto para um endpoint protegido (ex.: GET `/v1/artistas?page=0&size=10`):
   ```bash
   # Substituir TOKEN pelo JWT retornado no login
   for i in {1..12}; do curl -s -o /dev/null -w "%{http_code}\n" -H "Authorization: Bearer TOKEN" http://localhost:8080/v1/artistas?page=0&size=10; done
   ```
   **Esperado:** As primeiras 10 retornam 200; a 11ª e 12ª retornam **429** (Too Many Requests).
3. **Headers:** Em qualquer resposta 200 antes do limite, verificar headers `X-RateLimit-Limit`, `X-RateLimit-Remaining` (diminui a cada request).
4. Após ~1 minuto, novas requisições devem voltar a retornar 200 (bucket renovado).

---

## e) No Front end, padrão Facade e gestão de estado com BehaviorSubject

### Desenvolvimento

| Onde está | Descrição |
|-----------|-----------|
| **Facade** | `ArtistaFacade.ts`, `AuthFacade.ts` | Interface única para operações de artistas e autenticação; delegam para stores e API. |
| **Estado (BehaviorSubject)** | `ArtistaStore.ts`, `AlbumStore.ts`, `AuthStore.ts` | RxJS `BehaviorSubject`; estado exposto como Observables (`artistas$`, `loading$`, etc.); componentes subscrevem. |
| **Uso** | Páginas Artistas, Álbuns, Login, etc. | Chamam `ArtistaFacade.listar()`, `ArtistaStore.artistas$`, etc., em vez de acessar API ou estado disperso. |

**Status:** ✅ Atendido (Facade e BehaviorSubject no frontend).

### Como o avaliador pode testar

1. **Facade:** Abrir no editor:
   - `frontend/src/core/facade/ArtistaFacade.ts` — deve expor métodos estáticos (`listar`, `criar`, `atualizar`, etc.) e delegar para `ArtistaStore` e `artistasApi`.
   - `frontend/src/core/facade/AuthFacade.ts` — mesmo padrão para login/refresh/logout.
2. **BehaviorSubject:** Abrir:
   - `frontend/src/core/state/ArtistaStore.ts` — deve ter `private static state$ = new BehaviorSubject<ArtistaState>(...)` e getters como `artistas$`, `loading$` (pipe em `state$`).
   - `frontend/src/core/state/AlbumStore.ts` e `AuthStore.ts` — mesma ideia.
3. **Uso na UI:** Em `frontend/src/pages/Artistas.tsx` (ou equivalente), deve haver import de `ArtistaFacade` e/ou `ArtistaStore` e uso de `ArtistaFacade.listar()` e subscribe em `ArtistaStore.artistas$` (ou similar). Navegar em Artistas e Álbuns no front e confirmar que listagem e formulários funcionam (validando que o fluxo Facade + Store está em uso).

---

## f) Na API, endpoint de regionais da Polícia Civil — importar, atributo "ativo", sincronizar O(n)

### Desenvolvimento

| Subitem | Onde está | Descrição |
|---------|-----------|-----------|
| **f-i) Importar estrutura** | `RegionalSyncService`, `RegionalExternaDTO`, `Regional` (entity) | GET em `https://integrador-argus-api.geia.vip/v1/regionais`; DTO com `id`, `nome`; mapeamento para entidade `Regional` (id interno, codigoExterno, nome, ativa, externalHash, etc.); persistência em tabela `regionais`. |
| **f-ii) Atributo "ativo"** | `Regional.java`, V1 migration | Campo `ativa BOOLEAN NOT NULL DEFAULT TRUE` (edital fala "ativo"; implementação usa "ativa" para a regional). Tabela: id (BIGSERIAL), codigo_externo (integer), nome (varchar 200), ativa (boolean). |
| **f-iii) Sincronizar** | `RegionalSyncService.sincronizar()` | 1) **Novo no endpoint** → inserir na tabela local. 2) **Não disponível no endpoint** → inativar na tabela local (set ativa=false). 3) **Atributo alterado** → inativar registro anterior e criar novo com nova denominação (hash MD5 para detectar mudança). Algoritmo O(n) com HashMap por codigoExterno e batch save. |
| **Endpoint manual** | `POST /v1/regionais/sincronizar` | Dispara a sincronização; retorna estatísticas (criados, atualizados, desativados, totalAPI, etc.). |
| **Startup** | `RegionalSyncStartupRunner` | Executa sincronização uma vez ao subir a aplicação. |

**Status:** ✅ Atendido (importar, ativo/ativa, sincronização com as três regras e complexidade O(n)).

### Como o avaliador pode testar

1. **Importar e estrutura:**
   - Chamar sincronização: POST `http://localhost:8080/v1/regionais/sincronizar` com header `Authorization: Bearer <token>`.
   - Resposta deve trazer `sucesso: true` e `estatisticas` com `totalAPI` (ex.: 33) e `totalBanco` aumentando após a primeira sync.
2. **Atributo ativo:**
   - GET `http://localhost:8080/v1/regionais?size=100` (autenticado) — cada item deve ter campo `ativa` (true/false).
   - Ou consultar o banco: tabela `regionais` com coluna `ativa` (boolean).
3. **Regras de sincronização:**
   - **Novo no endpoint:** A primeira execução da sync insere todas as regionais da API; resposta com `criados > 0` (ou `totalBanco` = total da API).
   - **Não disponível no endpoint:** Só é testável se a API externa remover algum id; após nova sync, a regional correspondente deve aparecer com `ativa: false` na listagem.
   - **Alterado (inativar antigo + criar novo):** Se a API externa mudar o nome de uma regional (mesmo id), na próxima sync o serviço deve inativar o registro antigo e criar um novo com o nome novo (documentado em `RegionalSyncService` com hash MD5). O avaliador pode conferir o código e os comentários em `RegionalSyncService.java` e, se tiver ambiente controlado, alterar um nome na API e rodar a sync de novo.
4. **Complexidade O(n):** No README e em comentários do `RegionalSyncService` deve constar a explicação do uso de HashMap e hash para O(n). A resposta de `POST /v1/regionais/sincronizar` inclui `analiseComplexidade` com a descrição do algoritmo.
5. **Frontend:** Tela **Regionais** (http://localhost:3000/regionais) deve listar as regionais (código, nome, status Ativa/Inativa) e ter botão **Sincronizar** que chama o endpoint acima e atualiza a lista.

---

## Resumo para o avaliador

| Requisito | Status | Como testar (resumo) |
|-----------|--------|----------------------|
| **a) Health Checks** | ✅ | `curl` em `/actuator/health/liveness` e `/readiness`; `docker compose ps` para health dos containers. |
| **b) Testes unitários** | ✅ | Backend: `./mvnw test`. Frontend: `npm test` (5 testes). |
| **c) WebSocket / notificação novo álbum** | ✅ | Duas abas logadas; criar álbum em uma; ver toast na outra. |
| **d) Rate limit 10 req/min** | ✅ | >10 requests autenticadas em 1 min → 429; ver headers X-RateLimit-*. |
| **e) Facade + BehaviorSubject** | ✅ | Inspeção em `ArtistaFacade`, `ArtistaStore`, `AlbumStore`, `AuthStore` e uso nas páginas. |
| **f) Regionais (i, ii, iii)** | ✅ | POST `/v1/regionais/sincronizar`; GET `/v1/regionais` (campo ativa); README/código para regras e O(n); tela Regionais no front. |

Todos os requisitos Sênior listados estão implementados e podem ser verificados conforme os passos acima.
