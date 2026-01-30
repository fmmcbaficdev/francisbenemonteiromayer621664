# Revisão do Projeto e Ajustes Necessários

**Data:** 30/01/2026  
**Objetivo:** Apontar inconsistências, erros e melhorias para alinhar o projeto ao edital e à documentação.

---

## 1. Ajustes críticos (recomendado corrigir)

### 1.1 Frontend – Testes unitários (edital Sênior exige)

- **Problema:** O edital (Anexo II-C e III) exige "Testes unitários" no front. No projeto não há:
  - Nenhum arquivo `*.test.tsx` ou `*.spec.tsx`
  - Nenhum script `test` no `frontend/package.json`
  - Nenhuma dependência Vitest/Jest ou React Testing Library no `package.json`
- **README:** Diz "Testes: Vitest + React Testing Library" e "npm test" / "npm run test:coverage", o que não corresponde à realidade.
- **Ação:**  
  - **Opção A:** Incluir Vitest + RTL, criar pelo menos 1–2 testes (ex.: componente de login ou ArtistaFacade/store) e scripts `test` e `test:coverage` no `package.json`.  
  - **Opção B:** Se não for possível entregar testes no prazo, alterar o README para não afirmar que há testes no front (e, no README, explicar o que foi priorizado e o que ficou pendente).

### 1.2 Docker – Variáveis do frontend (Vite)

- **Problema:** O front usa `VITE_API_URL` e `VITE_WS_URL` (em `api.ts` e `websocket.ts`), mas o `docker-compose` e o `Dockerfile` do frontend usam `REACT_APP_API_URL`. No build com Vite, apenas variáveis `VITE_*` são injetadas; `REACT_APP_*` são ignoradas. O build em Docker acaba usando o fallback `http://localhost:8080`, que funciona em ambiente local, mas:
  - Em outro host/porta a API quebraria.
  - A documentação fica inconsistente.
- **Ação:**  
  - No `docker-compose`, trocar `REACT_APP_API_URL` por `VITE_API_URL` (e, se o front usar URL do WS em tempo de build, `VITE_WS_URL`).  
  - No `Dockerfile` do frontend, receber e usar `ARG VITE_API_URL` (e `VITE_WS_URL` se necessário) e repassar no `npm run build`.

### 1.3 README – Credenciais e tabela de acessos

- **Problema:** A tabela "Acessos" indica usuário do banco como "postgres / postgres123", mas no `.env.example` (raiz) o usuário é `POSTGRES_USER=seplag` e a senha é `your_postgres_password_here`. Quem seguir só o README pode tentar acessar com usuário errado.
- **Ação:** Ajustar a tabela para refletir o que está no `.env.example`: usuário `seplag` e informar que a senha é a definida em `POSTGRES_PASSWORD` no `.env` (ex.: "senha definida no .env").

### 1.4 README – Ano do edital

- **Problema:** O README cita "Concurso Público SEPLAG/MT **2025**"; o edital é o **001/2026** (publicado em 13/01/2026).
- **Ação:** Substituir por "SEPLAG/MT **2026**" (ou "PSS 001/2026").

### 1.5 README – Versões da stack (frontend)

- **Problema:**  
  - README: "React 18.2" → no `package.json` está "react": "^19.2.0".  
  - README: "Vite 5.0" → no `package.json` está "vite": "^7.2.4".  
- **Ação:** Atualizar o README para as versões reais (React 19.x e Vite 7.x), ou usar "React 19" / "Vite 7" de forma genérica.

### 1.6 README – TanStack Query

- **Problema:** O README lista "Data Fetching & Cache: TanStack Query v5 (React Query)", mas não há dependência `@tanstack/react-query` no `frontend/package.json`. O projeto usa chamadas diretas (Axios + Facade/Store).
- **Ação:** Remover a linha do TanStack Query do README ou adicionar a lib e usá-la; do contrário a documentação fica incorreta.

### 1.7 `.env.example` (raiz) – URL da API de regionais

- **Problema:** Consta `EXTERNAL_REGIONAIS_API_URL=https://api.example.com/regionais`. O edital e o `application.properties` usam `https://integrador-argus-api.geia.vip/v1/regionais`.
- **Ação:** Colocar no `.env.example` a URL correta (ou um comentário indicando que o default já está em `application.properties`).

### 1.8 `application.properties` – Encoding

- **Problema:** Comentários com caracteres especiais aparecem quebrados: "Remover em produo" e "Padro de log" (deveria ser "produção" e "Padrão").
- **Ação:** Garantir que o arquivo está em UTF-8 e reescrever os comentários com acentuação correta.

---

## 2. Ajustes recomendados (qualidade / consistência)

### 2.1 README – Placeholder "Número de Inscrição"

- **Problema:** Na identificação há "[Número de Inscrição]"; no edital o link do projeto deve ser informado no SIES.
- **Ação:** Preencher com o número real após inscrição ou trocar por texto do tipo "a ser preenchido no ato da inscrição".

### 2.2 MinIO – Credenciais no README

- **Problema:** README indica "minioadmin / minioadmin123". O `.env.example` usa `MINIO_ROOT_PASSWORD=your_minio_password_here`. Quem copiar só o `.env.example` terá senha diferente.
- **Ação:** No README, deixar explícito que as credenciais do MinIO são as definidas no `.env` (e que o exemplo usa "minioadmin" como usuário e que a senha deve ser configurada).

### 2.3 MELHORIAS_IMPLEMENTADAS.md – `application-dev.properties`

- **Problema:** O documento cita criação do arquivo `application-dev.properties`; na pasta `backend/src/main/resources` existem apenas `application.properties` e `application-prod.properties`.
- **Ação:** Ou criar `application-dev.properties` com as configurações descritas no doc, ou remover a menção a esse arquivo e indicar que o perfil "dev" usa apenas `application.properties` (e variáveis de ambiente).

### 2.4 README – Seção "Testes" do frontend

- **Problema:** Há instruções "npm test" e "npm run test:coverage" para o frontend, mas não há script nem testes.
- **Ação:** Alinhar à decisão do item 1.1: ou implementar testes e scripts, ou remover/reescrever a seção informando que os testes do frontend não foram entregues e por quê (priorização).

### 2.5 CORS – Origens no docker-compose / .env

- **Problema:** Em produção (container), o frontend é acessado em `http://localhost:3000` (ou porta configurada). O backend precisa aceitar essa origem no CORS. O `.env.example` já tem `CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:80`. O front no Docker escuta na porta 80 internamente; o usuário acessa na porta 3000. Está coerente; apenas garantir que em outros ambientes (ex.: outro domínio) o CORS seja ajustado.

---

## 3. Resumo das ações por arquivo

| Arquivo | Ação |
|---------|------|
| `frontend/package.json` | Incluir script `test` (e opcionalmente `test:coverage`) **se** forem adicionados Vitest + testes; ou não mencionar testes no README. |
| `docker-compose.yml` | Trocar `REACT_APP_API_URL` por `VITE_API_URL` (e `VITE_WS_URL` se aplicável) no build e no `environment` do frontend. |
| `frontend/Dockerfile` | Usar `ARG VITE_API_URL` (e `VITE_WS_URL` se necessário) e expor no build. |
| `README.md` | Corrigir ano (2026), versões (React 19, Vite 7), remover TanStack Query, corrigir tabela de acessos (PostgreSQL: seplag + senha do .env), alinhar seção de testes ao que existe, ajustar MinIO. |
| `.env.example` (raiz) | Ajustar `EXTERNAL_REGIONAIS_API_URL` para a URL do edital ou comentário. |
| `backend/.../application.properties` | Corrigir encoding e comentários "produção" e "Padrão". |
| `MELHORIAS_IMPLEMENTADAS.md` | Remover ou corrigir referência a `application-dev.properties`. |
| `AVALIACAO_CONFORME_EDITAL_SEPLAG.md` | Incluir breve seção "Revisão e ajustes" referenciando este documento. |

---

## 4. Checklist pós-ajustes

- [ ] Frontend: ou testes + scripts implementados, ou README corrigido (sem afirmar testes).
- [ ] Docker: build do frontend usando `VITE_API_URL` (e `VITE_WS_URL` se necessário).
- [ ] README: ano 2026, versões corretas, sem TanStack Query, tabela de acessos e MinIO corretos.
- [ ] `.env.example`: URL de regionais correta ou comentada.
- [ ] `application.properties`: comentários em UTF-8.
- [ ] MELHORIAS_IMPLEMENTADAS: referência a `application-dev.properties` corrigida ou removida.

---

## 5. Ajustes já aplicados (30/01/2026)

- **Docker:** `docker-compose.yml` e `frontend/Dockerfile` passam a usar `VITE_API_URL` e `VITE_WS_URL`.
- **README:** Ano 2026; versões React 19, Vite 7; tabela de acessos (PostgreSQL e MinIO conforme `.env`); remoção de TanStack Query; seção de testes do frontend alinhada à realidade.
- **.env.example (raiz):** URL de regionais corrigida; variáveis do frontend trocadas para `VITE_*`.
- **MELHORIAS_IMPLEMENTADAS.md:** Referência a `application-dev.properties` removida/corrigida.
- **application.properties:** Comentários com acentuação podem exibir caracteres incorretos (encoding); se necessário, abrir o arquivo em UTF-8 e corrigir manualmente "produção" e "Padrão".

---

*Este documento serve como guia para revisão antes da entrega final ou da avaliação pela banca.*
