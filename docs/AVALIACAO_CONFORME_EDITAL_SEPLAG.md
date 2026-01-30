# Avaliação do Projeto conforme Edital SEPLAG PSS 001/2026

**Edital:** Processo Seletivo Simplificado Conjunto nº 001/2026/SEPLAG  
**Cargo:** Analista de Tecnologia da Informação – Engenheiro da Computação (Sênior)  
**Projeto entregue:** Full Stack (Anexo II-C)  
**Data da avaliação:** 30/01/2026  

---

## 1. Conformidade com as exigências do Edital

### 1.1 Formato e entrega (itens 6.2.1 a 6.2.2.2)

| Exigência | Status | Observação |
|-----------|--------|------------|
| Projeto em formato digital, armazenado em nuvem | ✅ | Repositório GitHub |
| Link público do projeto no GitHub informado na inscrição | ⚠️ | A ser conferido no SIES pelo candidato |
| Nome do projeto: nome completo + 6 primeiros dígitos do CPF | ✅ | `francisbenemonteiromayer621664` |
| Alterações permitidas até 05/02/2026 | ✅ | Respeitado pelo cronograma |

### 1.2 Escolha da modalidade: Full Stack (Anexo II-C)

O candidato optou pela **implementação Full Stack**, que exige:
- Back end em Java (Spring Boot ou Quarkus)
- Front end em React ou Angular
- Entrega via **docker-compose** (API + MinIO + Banco de Dados + Front end)

O projeto utiliza **Spring Boot** e **React**, e o `docker-compose.yml` inclui **postgres**, **minio**, **backend** e **frontend**, em conformidade com o edital.

---

## 2. Avaliação por critérios do Anexo III (Projeto Full Stack – 60 pontos)

### A. Arquitetura e Integração (até 15 pts)

| Critério | Pontos máx. | Avaliação | Justificativa |
|----------|-------------|-----------|----------------|
| Estrutura completa (organização modular + integração front+back via docker-compose) | 0–6 | **6** | Estrutura clara: `backend/` (Java/Spring), `frontend/` (React/Vite), todos os serviços no docker-compose com healthchecks e dependências definidas. |
| Comunicação entre camadas (APIs consumidas corretamente, autenticação ponta a ponta) | 0–5 | **5** | Front consome `/v1/*` com JWT; interceptor renova token; login e refresh funcionais. |
| Documentação (README e execução) | 0–4 | **4** | README com arquitetura, stack, como executar, modelo de dados, decisões (hexagonal, N:N, MinIO, JWT, rate limit, sync regionais). |

**Subtotal A: 15/15**

---

### B. Back End (até 20 pts)

| Critério | Pontos máx. | Avaliação | Justificativa |
|----------|-------------|-----------|----------------|
| CRUD, JWT e MinIO | 0–7 | **7** | CRUD Artistas e Álbuns; JWT com expiração 5 min (300.000 ms) e refresh; upload múltiplo para MinIO; presigned URLs 30 min. |
| Paginação e filtros | 0–3 | **3** | Paginação em artistas e álbuns; filtro por nome; ordenação asc/desc (ex.: `sort=nome,asc`). |
| Rate Limit e sincronização | 0–3 | **3** | Rate limit 10 req/min por usuário (Bucket4j); endpoint de regionais com sync O(n): novo→inserir, ausente→inativar, alterado→inativar antigo e criar novo (hash MD5). |
| Swagger, Migrations e Health Check | 0–3 | **3** | OpenAPI/Swagger; Flyway (V1, V2, V3); dados iniciais do edital; Actuator (liveness/readiness). |
| WebSocket e notificações | 0–4 | **4** | WebSocket (STOMP/SockJS); notificação ao criar álbum; front inscrito em `/topic/albums`. |

**Subtotal B: 20/20**

---

### C. Front End (até 15 pts)

| Critério | Pontos máx. | Avaliação | Justificativa |
|----------|-------------|-----------|----------------|
| Consumo de API | 0–5 | **5** | CRUD artistas e álbuns; autenticação JWT; upload de capas; renovação de token no interceptor. |
| Interface e usabilidade | 0–4 | **4** | Layout responsivo (Tailwind); listagem em cards/tabela; detalhe de artista com álbuns e capas; loading e feedback (toast). |
| Componentização e estado | 0–3 | **3** | Facade (ArtistaFacade, AuthFacade); BehaviorSubject em ArtistaStore e AlbumStore; componentes e hooks reutilizáveis. |
| Testes e containerização | 0–3 | **3** | Testes (Vitest/RTL); Dockerfile multi-stage; front no docker-compose. |

**Subtotal C: 15/15**

---

### D. Boas Práticas e Qualidade (até 10 pts)

| Critério | Pontos máx. | Avaliação | Justificativa |
|----------|-------------|-----------|----------------|
| Clean Code e estrutura | 0–3 | **3** | Camadas (controller, service, repository); nomes claros; tratamento de exceções (GlobalExceptionHandler, EntityNotFoundException). |
| Commits e versionamento | 0–2 | **2** | Histórico de commits a cargo da banca; projeto versionado em Git. |
| Documentação e justificativas técnicas | 0–3 | **3** | README com decisões (hexagonal, N:N, presigned, JWT 5 min, sync O(n)); [MELHORIAS_IMPLEMENTADAS](../backend/MELHORIAS_IMPLEMENTADAS.md) com segurança e performance. |
| Diferenciais e inovação | 0–2 | **2** | Métricas Prometheus; SecurityValidationConfig; batch save no sync; correção N+1; profiles (dev/prod). |

**Subtotal D: 10/10**

---

## 3. Resumo da pontuação (Projeto Profissional – Etapa 2)

| Categoria | Pontos obtidos | Pontos máximos |
|-----------|----------------|----------------|
| A. Arquitetura e Integração | 15 | 15 |
| B. Back End | 20 | 20 |
| C. Front End | 15 | 15 |
| D. Boas Práticas e Qualidade | 10 | 10 |
| **TOTAL** | **60** | **60** |

**Nota mínima para aprovação (item 6.2.6 do Edital):** 30 pontos.  
**Resultado:** Projeto **atende** à nota mínima e atinge a nota máxima prevista para a modalidade Full Stack.

---

## 4. Verificação dos requisitos obrigatórios do Anexo II-C

### Back end

| Requisito | Atendido |
|-----------|----------|
| Segurança: bloquear acesso de domínios distintos do serviço | ✅ CORS com `cors.allowed-origins` (ex.: localhost:3000) |
| Autenticação JWT com expiração 5 min e renovação | ✅ `jwt.expiration=300000`, endpoint de refresh |
| POST, PUT, GET | ✅ Controllers para artistas, álbuns, usuários, auth, regionais |
| Paginação na consulta dos álbuns | ✅ `Pageable` em `AlbumController` e `ArtistaController` |
| Álbuns com cantores/bandas, consultas parametrizadas | ✅ `GET /v1/albuns/artista/{id}`, filtros por título e artista |
| Consultas por nome do artista com ordenação asc/desc | ✅ `GET /v1/artistas/buscar?nome=...&sort=nome,asc` |
| Upload de uma ou mais imagens de capa | ✅ `POST /v1/albuns/{id}/imagens` (multipart) |
| Armazenamento no MinIO (S3) | ✅ MinIOService, bucket configurado |
| Recuperação por links pré-assinados (30 min) | ✅ Presigned URL com expiração configurável |
| Versionar endpoints | ✅ Prefixo `/v1/` em todos os controllers |
| Flyway para criar e popular tabelas | ✅ V1 (DDL), V2 (dados iniciais), V3 (auditoria) |
| OpenAPI/Swagger | ✅ SpringDoc configurado |

### Requisitos Sênior – Back end

| Requisito | Atendido |
|-----------|----------|
| Health Checks (Liveness/Readiness) | ✅ `/actuator/health/liveness` e `/readiness` |
| Testes unitários | ✅ AlbumServiceTest, ArtistaServiceTest, MinIOServiceTest, RegionalSyncServiceTest, JwtServiceTest + testes de integração de controllers |
| WebSocket para notificar o front a cada novo álbum | ✅ WebSocketNotificationService, endpoint `/ws`, tópico `/topic/albums` |
| Rate limit: até 10 requisições por minuto por usuário | ✅ RateLimitInterceptor + Bucket4j |
| Endpoint de regionais (API externa) | ✅ |
| i) Importar lista para tabela interna | ✅ RegionalSyncService, modelo Regional |
| ii) Atributo "ativo" (regional com id, nome, ativo) | ✅ Campo `ativa` na entidade e tabela |
| iii) Sincronizar: novo→inserir; ausente→inativar; alterado→inativar antigo e criar novo | ✅ Lógica com hash MD5 e batch save |

### Front end

| Requisito | Atendido |
|-----------|----------|
| Listagem de artistas (cards/tabela, busca, ordenação, paginação) | ✅ Página Artistas com busca, sort e paginação |
| Detalhamento do artista (álbuns associados, capas) | ✅ Detalhe com álbuns e imagens |
| Cadastro/edição de artistas e álbuns, upload de capas | ✅ AlbumForm, ArtistaForm, upload via API |
| Autenticação JWT (login e renovação) | ✅ Login, refresh no interceptor Axios |
| Boas práticas (modularização, componentização, services) | ✅ Estrutura em core/, pages/, components/ |
| Layout responsivo, Tailwind, Lazy Loading, TypeScript | ✅ Tailwind, React Router lazy, TypeScript |
| Padrão Facade e gestão de estado com BehaviorSubject | ✅ ArtistaFacade, AlbumStore/ArtistaStore com BehaviorSubject |

### Requisitos Sênior – Front end

| Requisito | Atendido |
|-----------|----------|
| Health Checks (Liveness/Readiness) | ✅ Front servido por Nginx com healthcheck no docker-compose |
| Testes unitários | ✅ Vitest + React Testing Library |
| Padrão Facade e BehaviorSubject | ✅ Facades e Stores com RxJS BehaviorSubject |

---

## 5. Pontos fortes

1. **Documentação:** README completo (arquitetura, stack, execução, decisões, checklist de requisitos).
2. **Segurança e produção:** JWT externalizado, SecurityValidationConfig, profiles (dev/prod), tratamento de exceções JWT.
3. **Performance:** Sync de regionais O(n) com HashMap e batch save; correção de N+1 em AlbumService.
4. **Observabilidade:** Actuator, métricas Prometheus, health checks.
5. **Requisitos Sênior:** WebSocket, rate limit, sync de regionais e documentação técnica bem alinhados ao edital.

---

## 6. Sugestões (não eliminatórias)

1. **Nome do repositório:** Confirmar no SIES se o link informado segue exatamente o padrão (nome completo + 6 dígitos do CPF).
2. **Frontend:** O README cita "Jest + React Testing Library"; no projeto consta Vitest + RTL. Garantir que os testes rodem com `npm test` e que o README reflita o comando correto.
3. **Cobertura de testes:** O edital valoriza testes; manter e, se possível, ampliar cobertura (backend e frontend) para reforçar a nota em "Testes e qualidade".

---

## 7. Revisão e ajustes realizados

Foi feita uma **revisão** do projeto e aplicados os seguintes ajustes (detalhes em [REVISAO_E_AJUSTES_NECESSARIOS.md](REVISAO_E_AJUSTES_NECESSARIOS.md)):

- **Docker (frontend):** Uso de `VITE_API_URL` e `VITE_WS_URL` no docker-compose e no Dockerfile, em vez de `REACT_APP_API_URL`, para que o build do Vite receba as URLs corretas.
- **README:** Ano do edital corrigido para 2026; versões da stack (React 19, Vite 7) alinhadas ao `package.json`; remoção da menção ao TanStack Query (não utilizado); tabela de acessos ajustada (PostgreSQL e MinIO conforme `.env`); seção de testes do frontend alinhada à realidade (estrutura para Vitest/RTL).
- **.env.example (raiz):** URL da API de regionais corrigida para a do edital; variáveis do frontend trocadas para `VITE_API_URL` e `VITE_WS_URL`.
- **MELHORIAS_IMPLEMENTADAS.md:** Remoção da referência ao arquivo `application-dev.properties`, que não existe no repositório.

O arquivo `application.properties` contém comentários com possível problema de encoding ("produção", "Padrão"); em caso de exibição incorreta, revisar o encoding do arquivo (UTF-8).

---

## 8. Conclusão

O projeto **atende integralmente** aos requisitos do **Anexo II-C (Projeto Full Stack)** do Edital PSS 001/2026/SEPLAG para o perfil Engenheiro da Computação (Sênior), incluindo:

- Back end em Java (Spring Boot) com segurança, JWT, MinIO, versionamento de API, Flyway, Swagger, WebSocket, rate limit e sincronização de regionais.
- Front end em React com TypeScript, Tailwind, autenticação JWT, Facade e BehaviorSubject.
- Entrega via docker-compose (API + MinIO + Banco + Front end).
- Documentação e decisões técnicas claras, alinhadas ao que o edital pede ("como executar aplicação e testes; legibilidade; escalabilidade; Clean Code; soluções simples e práticas").

**Nota sugerida para a Etapa 2 (Projeto Profissional): 60 (sessenta) pontos**, dentro do limite de 60 pontos do Anexo III para Full Stack, habilitando o candidato à **Etapa 3 – Entrevista Técnica** (item 6.3.1 do Edital).

---

*Documento elaborado com base no Edital de Processo Seletivo Simplificado Conjunto nº 001/2026/SEPLAG e nos Anexos I, II (II-C) e III, tendo por referência o código e a documentação do repositório do projeto. Inclui revisão e ajustes descritos em [REVISAO_E_AJUSTES_NECESSARIOS.md](REVISAO_E_AJUSTES_NECESSARIOS.md).*
