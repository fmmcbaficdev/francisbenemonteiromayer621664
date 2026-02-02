# Problemas comuns ao executar o projeto (avaliador)

Este documento lista situações que podem ocorrer quando o avaliador executa o projeto conforme o README ou o guia [COMO_ABRIR_PARA_AVALIADOR.md](COMO_ABRIR_PARA_AVALIADOR.md), e como resolver.

---

## Antes de subir

| Problema | Causa provável | O que fazer |
|----------|----------------|-------------|
| **Esqueceu de criar o `.env`** | Rodou `docker compose up` sem antes fazer `cp .env.example .env` | Crie o arquivo: `cp .env.example .env` e suba de novo: `docker compose up --build -d`. Sem o `.env`, variáveis como `JWT_SECRET` e senhas do Postgres podem ficar vazias ou usar valor padrão e o backend pode falhar. |
| **Portas já em uso** | O frontend usa **3001 e 3002** (duas portas fixas); use a que estiver livre. Se 3001 e 3002 estiverem ocupadas, veja a seção **[Porta em uso](#porta-em-uso)** abaixo. |
| **Docker ou Docker Compose desatualizado** | README pede Docker 24+ e Compose 2.20+ | Atualize o Docker Desktop (ou Engine + Compose). Em máquinas antigas, versões um pouco anteriores podem funcionar; em caso de erro, atualize. |

---

## Porta em uso

O frontend sobe em **duas portas fixas (3001 e 3002)**. O avaliador não precisa editar o `.env`: basta usar a primeira que estiver livre.

- **Padrão:** acesse http://localhost:3001 (ou http://localhost:3002 se 3001 estiver em uso).
- **Se 3001 e 3002 estiverem ocupadas** (raro), use uma das opções abaixo.

### Opção 1 — Liberar uma das portas no Windows

1. Descubra o PID do processo na porta (ex.: 3001):
   ```powershell
   netstat -ano | findstr :3001
   ```
2. Encerre o processo (substitua `PID` pelo número da última coluna):
   ```powershell
   taskkill /PID PID /F
   ```
3. Suba o frontend: `docker compose up -d frontend`.

### Opção 2 — Usar outra porta (ex.: 3003)

Se quiser usar 3003 em vez de 3001/3002:

1. No **docker-compose.yml**, na seção `frontend` → `ports`, adicione uma linha:
   ```yaml
   ports:
     - "3001:80"
     - "3002:80"
     - "3003:80"
   ```
2. No **.env**, adicione 3003 ao CORS:
   ```env
   CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001,http://localhost:3002,http://localhost:3003,http://localhost:80
   ```
3. `docker compose up -d frontend` e `docker compose restart backend`.
4. Acesse http://localhost:3003.

---

## Durante a subida

| Problema | Causa provável | O que fazer |
|----------|----------------|-------------|
| **Backend não fica (healthy)** | Postgres ainda não pronto, ou `JWT_SECRET` ausente/curto | 1) Confira os logs: `docker compose logs backend`. 2) Se for erro de conexão com o banco, aguarde o Postgres ficar healthy e suba de novo o backend. 3) Se for erro de JWT (ex.: "secret key too short"), garanta que o `.env` existe e contém `JWT_SECRET` com o valor do `.env.example` (mínimo recomendado: 32 caracteres). |
| **Frontend build falha (TypeScript/vite)** | Erro no build do front (ex.: vite.config) | Rode o build localmente: `cd frontend && npm run build`. Se passar, o problema pode ser cache do Docker; tente `docker compose build --no-cache frontend` e depois `docker compose up -d`. |
| **Primeira execução muito lenta** | Download de imagens (Postgres, MinIO, Node, etc.), Maven e npm | Normal na primeira vez (pode levar 5–10 minutos). Acompanhe com `docker compose logs -f backend`. Depois que aparecer "Started BackendApplication", aguarde mais um pouco (Flyway, sync de regionais) e teste o front. |
| **minio-init ou backend falham** | MinIO ainda não healthy quando o init/backend sobem | O compose já usa `depends_on` com `condition: service_healthy`. Se ainda assim falhar, suba de novo: `docker compose up -d` (o MinIO já estará healthy na segunda vez). |

---

## Depois de subir

| Problema | Causa provável | O que fazer |
|----------|----------------|-------------|
| **Frontend abre mas não carrega dados / 401** | Backend não está saudável ou front apontando para URL errada | 1) Confira o backend: `curl http://localhost:8080/actuator/health/liveness`. 2) O front é buildado com `VITE_API_URL=http://localhost:8080`; se acessar por outro host/porta, pode precisar rebuildar o front com as URLs corretas no `.env`. |
| **Só aparecem 3 regionais** | API externa de regionais lenta ou indisponível na hora da subida | Na tela **Regionais**, clique em **Sincronizar**. A sincronização também roda ao subir o backend; se a API externa falhou naquele momento, o botão traz os dados depois. |
| **Erro 429 (Too Many Requests)** | Rate limit de 10 req/min por usuário (conforme edital) | Aguarde cerca de 1 minuto para o bucket renovar. O limite é intencional (requisito do edital). |
| **Upload de imagem não funciona / erro MinIO** | Bucket não existe ou credenciais erradas | O `minio-init` cria o bucket; o backend também tenta criar se não existir. Confira no `.env`: `MINIO_ROOT_USER`, `MINIO_ROOT_PASSWORD` e `MINIO_BUCKET_NAME` iguais ao usado pelo backend. Console MinIO: http://localhost:9001 (minioadmin / minioadmin123 se usou o .env.example). |
| **Login não funciona (401 ou erro)** | Credenciais ou JWT | Use **admin** / **admin123** (usuário do seed Flyway). Se o backend tiver iniciado com `JWT_SECRET` fraco ou vazio, reinicie o backend após corrigir o `.env` e criar o `.env` a partir do `.env.example`. |

---

## Ambiente do avaliador

| Situação | Sugestão |
|----------|----------|
| **Windows** | Use PowerShell ou Git Bash. Comando: `docker compose up --build -d` (com espaço). Se tiver só `docker-compose` (com hífen), use esse. |
| **Linux / WSL sem Docker** | Instale Docker e Docker Compose conforme a documentação oficial. |
| **Pouca RAM** | O README cita 4 GB. Se a máquina tiver pouco espaço livre, feche outros programas; em último caso, reduza `JAVA_OPTS` no `.env` (ex.: `-Xms256m -Xmx512m`). |
| **Firewall / rede restrita** | A API de regionais é externa (`https://integrador-argus-api.geia.vip`). Se houver bloqueio, a sincronização de regionais pode falhar; o restante do sistema (artistas, álbuns, login) continua funcionando. Use **Sincronizar** na tela Regionais quando a rede permitir. |

---

## Resumo rápido

1. Sempre criar o `.env`: `cp .env.example .env`.  
2. Garantir portas 3000, 8080, 5432, 9000, 9001 livres.  
3. Após `docker compose up -d`, aguardar 1–2 min e verificar `docker compose ps` (backend **healthy**).  
4. Login: **admin** / **admin123**.  
5. Se algo falhar: `docker compose logs backend` (e, se necessário, `docker compose logs frontend`).  

Para passos mínimos de execução, use o guia [COMO_ABRIR_PARA_AVALIADOR.md](COMO_ABRIR_PARA_AVALIADOR.md).
