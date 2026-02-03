# Deploy em produção com Docker Compose

Este documento explica como o **Docker Compose** é usado para produção e como fazer o deploy usando o arquivo `docker-compose.prod.yml`.

---

## Como o Compose funciona em produção

- **Um único comando** sobe todos os serviços (Postgres, MinIO, backend, frontend) na mesma máquina.
- **Ordem de subida:** o Compose respeita `depends_on` e healthchecks: Postgres e MinIO ficam saudáveis antes do backend; o backend sobe antes do frontend.
- **Rede interna:** os containers se comunicam pelo nome do serviço (`postgres`, `backend`, `minio`) na rede `seplag-network`.
- **Variáveis de ambiente:** vêm do arquivo `.env` no servidor (nunca commitar senhas).

Diferença principal em relação ao `docker-compose.yml` (dev):

| Aspecto | docker-compose.yml (dev) | docker-compose.prod.yml (prod) |
|--------|---------------------------|---------------------------------|
| Backend / Frontend | `build: .` (build no host) | `image: REGISTRY/...` (só pull) |
| Restart | `unless-stopped` | `always` |
| Uso no servidor | Pode fazer build lá | Apenas pull + up (imagens já no registry) |

---

## Fluxo de deploy

### 1. Build e push das imagens (CI ou sua máquina)

As imagens devem ser buildadas com as URLs da **produção** (frontend) e enviadas a um registry (Docker Hub, GitHub Container Registry, etc.):

```bash
# Docker Hub: https://hub.docker.com/repositories/francisbene
export REGISTRY=francisbene
export IMAGE_TAG=1.0.0

# Backend
docker build -t $REGISTRY/seplag-backend:$IMAGE_TAG ./backend
docker push $REGISTRY/seplag-backend:$IMAGE_TAG

# Frontend (use VITE_API_URL e VITE_WS_URL da produção no build)
docker build -t $REGISTRY/seplag-frontend:$IMAGE_TAG \
  --build-arg VITE_API_URL=https://api.seudominio.gov.br \
  --build-arg VITE_WS_URL=https://api.seudominio.gov.br/ws \
  ./frontend
docker push $REGISTRY/seplag-frontend:$IMAGE_TAG
```

### 2. No servidor de produção

1. Copie para o servidor (ou clone do repositório):
   - `docker-compose.prod.yml`
   - `.env` preenchido com valores de **produção** (banco, JWT, MinIO, CORS, etc.).

2. No `.env` do servidor, defina também:
   - `REGISTRY=francisbene` (Docker Hub: https://hub.docker.com/repositories/francisbene)
   - `IMAGE_TAG=1.0.0`
   - `POSTGRES_HOST=postgres` (nome do serviço no Compose)

3. Suba a pilha:

```bash
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d
```

4. Verifique:

```bash
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f backend
```

---

## Atualizando a aplicação (novo deploy)

1. Gere novas imagens com nova tag (ex.: `1.0.1`) e faça push para o registry.
2. No servidor:

```bash
export IMAGE_TAG=1.0.1
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d
```

O Compose recria apenas os containers cuja imagem mudou (backend e frontend); Postgres e MinIO seguem com os mesmos volumes.

---

## Resumo

- **docker-compose.prod.yml** = orquestração para produção usando **imagens do registry** (sem build no servidor).
- **Garantir funcionamento** continua dependendo de: `.env` correto, migrações do banco, rede (e, em produção real, proxy reverso, HTTPS e backups). O Compose só garante que os serviços sobem na ordem certa e com a configuração que você passou.
