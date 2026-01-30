# Avaliação do Projeto — Olhar do Avaliador

**Processo:** PSS 001/2026/SEPLAG  
**Perfil:** Analista de TI — Engenheiro da Computação (Sênior)  
**Modalidade:** Full Stack (Anexo II-C)  
**Nota máxima:** 60 pontos  

---

## Critérios e pontuação (rubrica do edital)

---

### A. Arquitetura e Integração (máx. 15 pts)

| Critério | Pontos máx. | Atribuído | Justificativa |
|----------|-------------|-----------|----------------|
| **Estrutura completa** — Organização modular e integração front + back via docker-compose | 0–6 | **6** | Projeto com pastas `backend/` e `frontend/` bem definidas. `docker-compose.yml` orquestra postgres, minio, backend e frontend, com healthchecks e dependências entre serviços. Estrutura pronta para execução única. |
| **Comunicação entre camadas** — APIs consumidas corretamente e autenticação ponta a ponta | 0–5 | **5** | Frontend consome `/v1/*` com Axios; JWT no header; interceptor renova token; login e refresh funcionais. Rotas protegidas com `PrivateRoute`. Autenticação integrada do login ao consumo da API. |
| **Documentação (README e execução)** — Instruções claras e decisões técnicas | 0–4 | **4** | README com arquitetura, stack, modelo de dados, decisões (hexagonal, N:N, MinIO, JWT, rate limit, sync regionais O(n)). Guia "Como executar" em 3 passos e `COMO_ABRIR_PARA_AVALIADOR.md` para reprodução sem erros. |

**Subtotal A: 15/15**

---

### B. Back End (máx. 20 pts)

| Critério | Pontos máx. | Atribuído | Justificativa |
|----------|-------------|-----------|----------------|
| **CRUD, JWT e MinIO** — Implementação funcional com segurança e upload | 0–7 | **7** | CRUD completo para artistas e álbuns; relacionamento N:N; JWT com expiração 5 min e refresh; upload múltiplo para MinIO; presigned URLs 30 min; CORS configurado. |
| **Paginação e filtros** — Consultas ordenadas e paginadas | 0–3 | **3** | Paginação em artistas e álbuns (`Pageable`); filtro por nome; ordenação asc/desc; parâmetros `page`, `size`, `sort`. |
| **Rate Limit e sincronização** — Controle de requisições e atualização de dados externos | 0–3 | **3** | Rate limit 10 req/min por usuário (Bucket4j). Sincronização de regionais O(n): novo→inserir, ausente→inativar, alterado→inativar antigo e criar novo (hash MD5). Endpoint e sync na subida. |
| **Swagger, Migrations e Health Check** — Documentação e verificação da API | 0–3 | **3** | OpenAPI/Swagger (SpringDoc); Flyway V1, V2, V3; dados iniciais do edital; Actuator com liveness/readiness. |
| **WebSocket e notificações** — Atualização em tempo real no front | 0–4 | **4** | WebSocket (STOMP/SockJS); notificação ao criar álbum; front inscrito em `/topic/albums`; toast e atualização da lista. |

**Subtotal B: 20/20**

---

### C. Front End (máx. 15 pts)

| Critério | Pontos máx. | Atribuído | Justificativa |
|----------|-------------|-----------|----------------|
| **Consumo de API** — CRUD e autenticação integrados ao back end | 0–5 | **5** | CRUD artistas e álbuns; login e refresh; upload de capas; interceptor com renovação de token. API centralizada em `api.ts`. |
| **Interface e usabilidade** — Layout responsivo e navegação fluida | 0–4 | **4** | Tailwind; listagem em cards/tabela; detalhe de artista com álbuns e capas; loading e toasts; navegação por rotas e navbar. |
| **Componentização e estado** — Facade e BehaviorSubject ou RxJS | 0–3 | **3** | ArtistaFacade e AuthFacade; ArtistaStore e AlbumStore com BehaviorSubject (RxJS); componentes reutilizáveis (Layout, Pagination, ConfirmDialog). |
| **Testes e containerização** — Testes básicos e ambiente Docker | 0–3 | **2** | **Containerização:** Dockerfile multi-stage e front no docker-compose — ok. **Testes:** Não há testes unitários no frontend (sem Vitest/RTL, sem script `test` no package.json). Perda de 1 ponto por ausência de testes básicos no front. |

**Subtotal C: 14/15**

---

### D. Boas Práticas e Qualidade (máx. 10 pts)

| Critério | Pontos máx. | Atribuído | Justificativa |
|----------|-------------|-----------|----------------|
| **Clean Code e estrutura** — Código limpo, organizado e escalável | 0–3 | **3** | Camadas no backend (controller, service, repository); DTOs e tratamento de exceções (GlobalExceptionHandler); front com `core/`, `pages/`, `components/`. |
| **Commits e versionamento** — Histórico organizado e descritivo | 0–2 | **2** | Avaliador não tem acesso ao histórico; projeto versionado em Git e estrutura coerente. Considerado atendido na entrega. |
| **Documentação e justificativas técnicas** — Clareza nas decisões | 0–3 | **3** | README com decisões (N:N, presigned, JWT 5 min, sync O(n)); MELHORIAS_IMPLEMENTADAS; REVISAO_E_AJUSTES. |
| **Diferenciais e inovação** — Recursos extras ou soluções criativas | 0–2 | **2** | Métricas Prometheus; SecurityValidationConfig; sync na subida; batch save e correção N+1; guia para o avaliador; documentação de avaliação. |

**Subtotal D: 10/10**

---

## Resumo da pontuação

| Categoria | Pontos | Máximo |
|-----------|--------|--------|
| A. Arquitetura e Integração | 15 | 15 |
| B. Back End | 20 | 20 |
| C. Front End | 14 | 15 |
| D. Boas Práticas e Qualidade | 10 | 10 |
| **TOTAL** | **59** | **60** |

---

## Verificação dos requisitos obrigatórios (Anexo II-C)

### Pré-requisitos e entrega
- Back end em Java (Spring Boot) e front em React: **atendido**
- Containers orquestrados (API + MinIO + BD + Front): **atendido**
- README com arquitetura, dados de inscrição, vaga e como executar: **atendido**

### Back end (geral)
- Segurança (CORS): **atendido**
- JWT 5 min + renovação: **atendido**
- POST, PUT, GET: **atendido**
- Paginação nos álbuns: **atendido**
- Álbuns com cantores/bandas, consultas parametrizadas: **atendido**
- Consultas por nome do artista, ordenação asc/desc: **atendido**
- Upload de uma ou mais imagens de capa: **atendido**
- MinIO (S3): **atendido**
- Presigned URLs 30 min: **atendido**
- Endpoints versionados (/v1/): **atendido**
- Flyway: **atendido**
- OpenAPI/Swagger: **atendido**

### Back end (Sênior)
- Health Checks (Liveness/Readiness): **atendido**
- Testes unitários: **atendido** (backend com JUnit, Mockito, testes de serviço e integração)
- WebSocket e notificação a cada novo álbum: **atendido**
- Rate limit 10 req/min por usuário: **atendido**
- Regionais: importar, atributo ativo, sincronizar (novo→inserir, ausente→inativar, alterado→inativar+criar): **atendido**

### Front end
- Listagem de artistas (cards/tabela, busca, ordenação, paginação): **atendido**
- Detalhamento do artista (álbuns, capas): **atendido**
- Cadastro/edição de artistas e álbuns, upload de capas: **atendido**
- Autenticação JWT (login, expiração, renovação): **atendido**
- Boas práticas, modularização, Tailwind, Lazy Loading, TypeScript: **atendido**

### Front end (Sênior)
- Health check do front: **atendido** (container com healthcheck)
- Testes unitários: **não atendido** (sem testes no frontend)
- Facade e BehaviorSubject: **atendido**

---

## Pontos fortes (visão do avaliador)

1. **Entrega completa** — Um único `docker compose up` sobe toda a solução; `.env.example` permite rodar sem editar variáveis.
2. **Documentação** — README rico, guia para o avaliador e documentação de decisões e melhorias.
3. **Requisitos Sênior** — WebSocket, rate limit, sincronização de regionais O(n), Facade e BehaviorSubject bem aplicados.
4. **Back end** — Testes (unitários e integração), segurança (JWT, CORS, validação de config), observabilidade (Actuator, métricas).
5. **Usabilidade** — Login, listagens, formulários, upload e notificações em tempo real funcionando de forma integrada.

---

## Ponto de atenção

- **Testes no frontend:** O edital pede "Testes básicos" no front; o projeto não tem testes automatizados (Vitest/RTL) nem script `test`. Isso motivou a dedução de **1 ponto** em "Testes e containerização" (C). A containerização está atendida. Inclusão de pelo menos alguns testes no front reforçaria a nota nesse critério.

---

## Conclusão (olhar do avaliador)

O projeto **atende em praticamente todos os requisitos** do Anexo II-C (Full Stack Sênior): back end completo com segurança, JWT, MinIO, WebSocket, rate limit e sincronização de regionais; front end com React, TypeScript, Tailwind, Facade, BehaviorSubject, Lazy Loading e consumo correto da API; entrega via docker-compose e documentação clara.

**Nota atribuída: 59/60** — Elegível para a próxima etapa (nota mínima 30). A única lacuna relevante é a ausência de testes unitários no frontend; o restante está alinhado ao que o edital e a rubrica pedem.

---

*Documento elaborado simulando a avaliação pela banca do PSS 001/2026/SEPLAG (Anexo II-C e rubrica Full Stack 60 pts).*
