# Revisão do projeto conforme Edital — Anexo II-C (Full Stack Sênior)

**Edital:** PSS 001/2026/SEPLAG  
**Anexo:** II-C — Projeto Prático Implementação Full Stack Sênior (Java + Angular/React)  
**Data da revisão:** 02/02/2026  

Este documento confronta **cada requisito do edital** com o estado atual do projeto e aponta eventuais falhas ou observações.

---

## Pré-requisitos

| Requisito | Status | Onde verificar / Observação |
|-----------|--------|-----------------------------|
| **a) Back end em Java (Spring Boot ou Quarkus) e front end em React ou Angular** | ✅ Atendido | Backend: Spring Boot (`backend/`). Frontend: React (`frontend/`, Vite). |
| **b) Aplicação entregue em containers orquestrados via docker-compose (API + MinIO + BD + Front end)** | ✅ Atendido | `docker-compose.yml`: postgres, minio, minio-init, backend, frontend. |

---

## Back end

| Requisito | Status | Onde verificar / Observação |
|-----------|--------|-----------------------------|
| **a) Segurança para não permitir acesso a partir de domínios distintos** | ✅ Atendido | CORS em `SecurityConfig.java` e `WebConfig.java`; `CORS_ALLOWED_ORIGINS` no `.env`. |
| **b) Autenticação JWT com expiração a cada 5 minutos e possibilidade de renovação** | ✅ Atendido | `JwtService`, `AuthController` (login + refresh). `JWT_EXPIRATION=300000` (5 min). |
| **c) Pelo menos POST, PUT, GET** | ✅ Atendido | Artistas e Álbuns: GET, POST, PUT, DELETE. Regionais: GET, POST (sync). Auth: POST. |
| **d) Paginação na consulta dos álbuns** | ✅ Atendido | `AlbumController.listar(Pageable)`, `GET /v1/albuns?page=&size=&sort=`. |
| **e) Expor quais álbuns são/têm os cantores/bandas e consultas parametrizadas** | ✅ Atendido | Relação N:N artista–álbum; `GET /v1/albuns/artista/{artistaId}`; `GET /v1/artistas/{id}` com álbuns. |
| **f) Consultas por nome do artista com ordenação alfabética (asc e desc)** | ✅ Atendido | `GET /v1/artistas/buscar?nome=` e `GET /v1/artistas?sort=nome,asc` ou `sort=nome,desc`. |
| **g) Upload de uma ou mais imagens da capa do álbum** | ✅ Atendido | `POST /v1/albuns/{id}/imagens` (multipart); múltiplos arquivos. |
| **h) Armazenar imagens no MinIO (API S3)** | ✅ Atendido | `MinIOService` (ou equivalente); bucket configurado no docker-compose (minio-init). |
| **i) Recuperar imagens via links pré-assinados (presigned URL) com expiração de 30 minutos** | ✅ Atendido | `minio.presigned-url-expiry-minutes=30` em `application.properties`; geração de URLs no serviço de álbum. |
| **j) Versionar endpoints** | ✅ Atendido | Base path `/v1/` em todos os controllers (`/v1/artistas`, `/v1/albuns`, `/v1/auth`, `/v1/regionais`). |
| **k) Flyway Migrations para criar e popular tabelas** | ✅ Atendido | `db/migration/`: V1 (create), V2 (dados iniciais do edital), V3 (auditoria, etc.). |
| **l) Documentar endpoints com OpenAPI/Swagger** | ✅ Atendido | SpringDoc; `/swagger-ui.html`, `/v3/api-docs`. |

---

## Front end

| Requisito | Status | Onde verificar / Observação |
|-----------|--------|-----------------------------|
| **Consumir a API e prover interface intuitiva** | ✅ Atendido | `api.ts`, facades, stores; telas de listagem, detalhe, formulários, login. |
| **a) Tela Inicial — Listagem de Artistas** | | |
| ● Consultar e exibir lista de artistas | ✅ | `Artistas.tsx`, `GET /v1/artistas`. |
| ● Cards ou tabela responsiva (nome e nº de álbuns) | ✅ | Cards/tabela em `Artistas.tsx`. |
| ● Campo de busca por nome, ordenação asc/desc | ✅ | Busca e `sort` nos parâmetros da API. |
| ● Paginação | ✅ | Componente `Pagination` e parâmetros `page`, `size`. |
| **b) Tela de Detalhamento do Artista** | | |
| ● Ao clicar em artista, exibir álbuns associados | ✅ | `ArtistaDetalhes.tsx`, rota `/artistas/:id`. |
| ● Exibir informações completas, incluindo capas | ✅ | Álbuns e imagens (URLs) no detalhe. |
| ● Se não houver álbuns, exibir mensagem | ✅ | Tratamento de lista vazia no detalhe. |
| **c) Tela de Cadastro/Edição** | | |
| ● Formulário para inserir artistas | ✅ | `ArtistaForm.tsx`, rota `/artistas/novo`. |
| ● Formulário para adicionar álbuns a um artista | ✅ | `AlbumForm.tsx` (álbum com artistas selecionados). |
| ● Edição de registros | ✅ | Rotas `/artistas/:id/editar`, `/albuns/:id/editar`. |
| ● Upload de capas (via endpoints com MinIO) | ✅ | `AlbumForm.tsx`: upload para `POST /v1/albuns/{id}/imagens`. |
| **d) Autenticação** | | |
| ● Acesso ao front exige login | ✅ | `PrivateRoute`, rotas protegidas. |
| ● Autenticação JWT consumindo o endpoint | ✅ | `AuthContext`, `AuthFacade`, login e refresh. |
| ● Gerenciar expiração e renovação do token | ✅ | Interceptor em `api.ts` (renovação antes de expirar e em 401). |
| **e) Arquitetura** | | |
| ● Boas práticas (modularização, componentização, services) | ✅ | `components/`, `pages/`, `core/` (facade, state, services). |
| ● Layout responsivo | ✅ | Tailwind CSS; layout adaptável. |
| ● Se usar framework CSS, priorize Tailwind | ✅ | Tailwind utilizado. |
| ● Lazy Loading Routes para módulos distintos | ✅ | `App.tsx`: `lazy()` para Login, Artistas, ArtistaForm, ArtistaDetalhes, Albuns, AlbumForm, Regionais; `Suspense` com fallback. |
| ● Paginação ou scroll infinito | ✅ | Paginação implementada (não scroll infinito; edital aceita “ou”). |
| ● Utilizar TypeScript | ✅ | Projeto em TypeScript (`.ts`, `.tsx`). |

---

## Requisitos apenas para Sênior

| Requisito | Status | Onde verificar / Observação |
|-----------|--------|-----------------------------|
| **a) Health Checks e Liveness/Readiness** | ✅ Atendido | Actuator: `/actuator/health`, `/actuator/health/liveness`, `/actuator/health/readiness`. Healthcheck do backend e do frontend no docker-compose. |
| **b) Testes unitários** | ✅ Atendido | Backend: JUnit 5 + Mockito — serviços (Album, Artista, RegionalSync), JwtService, integração (Album, Artista, Auth). Frontend: Vitest + React Testing Library (Loading, Login). Não há teste unitário dedicado ao MinIOService (opcional). |
| **c) WebSocket na API e exibir no front notificações a cada novo álbum cadastrado** | ✅ Atendido | STOMP/SockJS; `WebSocketNotificationService`; front inscrito em `/topic/albums`; toast ao receber notificação. |
| **d) Rate limit: máximo 10 requisições por minuto por usuário na API** | ✅ Atendido | `RateLimitInterceptor` (Bucket4j); 10 req/min; configurável por `RATE_LIMIT_REQUESTS_PER_MINUTE`; resposta 429 e headers X-RateLimit-*. |
| **e) No Front end, padrão Facade e gestão de estado com BehaviorSubject** | ✅ Atendido | `ArtistaFacade`, `AuthFacade`; `ArtistaStore`, `AlbumStore`, `AuthStore` com RxJS `BehaviorSubject`. |
| **f) Na API, endpoint de regionais da Polícia Civil** | | |
| **f-i) Importar estrutura para tabela interna** | ✅ | `RegionalSyncService`; GET em `https://integrador-argus-api.geia.vip/v1/regionais`; tabela `regionais`. |
| **f-ii) Atributo “ativo” (tabela: regional id integer, nome varchar(200), ativo boolean)** | ✅ | Tabela `regionais` com `ativa BOOLEAN` (mesmo conceito que “ativo”; edital cita “ativo”, implementação usa “ativa” por coerência com o domínio). |
| **f-iii) Sincronizar com menor complexidade algorítmica possível** | ✅ | Algoritmo O(n): novo → inserir; não disponível → inativar; atributo alterado → inativar antigo e criar novo (hash MD5). Documentado em `RegionalSyncService` e README. |

---

## Instruções do edital

| Requisito | Status | Observação |
|-----------|--------|------------|
| Disponibilizar solução com docker-compose (BD, MinIO, API, Front end) | ✅ | docker-compose com postgres, minio, minio-init, backend, frontend. |
| README.md com documentação da arquitetura, dados de inscrição, vaga e como executar/testar | ✅ | README com visão geral, stack, modelo de dados, decisões, como executar, acessos, testes. |
| Repositório com todos os arquivos e scripts | ✅ | Estrutura do projeto completa. |

---

## O que o edital espera / não espera / avalia

| Aspecto | Status / Observação |
|---------|---------------------|
| Como executar aplicação e testes | ✅ README e `docs/COMO_ABRIR_PARA_AVALIADOR.md`, `docs/TESTES_VIA_SWAGGER.md`, `docs/REQUISITOS_SENIOR_AVALIACAO_E_COMO_TESTAR.md`. |
| Legibilidade, escalabilidade, Clean Code, soluções simples | ✅ Camadas definidas, nomes claros, tratamento de exceções, documentação de decisões no README. |
| Implementação dos requisitos | ✅ Itens do Anexo II-C e requisitos Sênior cobertos conforme tabelas acima. |

---

## Resumo — falhas e observações

- **Nenhuma falha que impeça o atendimento ao edital.** Os itens obrigatórios do Anexo II-C (geral e Sênior) estão implementados e documentados.
- **Observações pontuais:**
  1. **Regional “ativo”:** O edital menciona atributo “ativo”; no projeto a coluna é `ativa` (boolean). Mesmo conceito (regional ativa/inativa); sem prejuízo à avaliação.
  2. **Teste unitário MinIO:** Não existe `MinIOServiceTest`; há testes de serviços que usam MinIO indiretamente (ex.: AlbumService). Para reforçar cobertura, pode-se adicionar teste unitário do MinIOService (opcional).
  3. **Rate limit 10 req/min:** Exatamente como no edital; em uso intenso (muitas requisições seguidas) pode retornar 429. Está documentado em `docs/PROBLEMAS_COMUNS_AVALIADOR.md`.

---

## Referência rápida para o avaliador

- **Executar:** `cp .env.example .env` e `docker compose up --build -d`; acessar front em http://localhost:3001 (ou 3002).  
- **Login:** admin / admin123.  
- **Testes backend:** `cd backend && ./mvnw test`.  
- **Testes frontend:** `cd frontend && npm test`.  
- **Swagger:** http://localhost:8080/swagger-ui.html.  
- **Detalhes por requisito:** `docs/REQUISITOS_SENIOR_AVALIACAO_E_COMO_TESTAR.md` e `docs/AVALIACAO_CONFORME_EDITAL_SEPLAG.md`.

Documento elaborado com base no **Edital PSS 001/2026/SEPLAG** e no **Anexo II-C** (Projeto Full Stack Sênior).
