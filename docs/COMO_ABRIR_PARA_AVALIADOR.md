# Como abrir o projeto para avaliação (sem erros)

Este guia garante que o avaliador consiga subir e testar o projeto em poucos passos.

---

## Pré-requisitos

- **Docker** e **Docker Compose** instalados (versão recente)
- **Portas livres:** 3000, 8080, 5432, 9000, 9001
- **~4 GB RAM** disponível

---

## Passos (avaliador)

### 1. Clonar e entrar na pasta

```bash
git clone <URL_DO_REPOSITORIO>
cd francisbenemonteiromayer621664
```

### 2. Criar o arquivo `.env`

```bash
cp .env.example .env
```

**Não é necessário editar o `.env`** — o `.env.example` já vem com valores que funcionam para avaliação (JWT, senhas do banco e do MinIO, URL da API de regionais).

### 3. Subir os serviços

```bash
docker compose up --build -d
```

### 4. Aguardar (1 a 2 minutos)

O backend demora mais (Spring Boot + Flyway + sincronização inicial de regionais). Acompanhe:

```bash
docker compose logs -f backend
```

Espere aparecer algo como: `Started BackendApplication` e, em seguida, as linhas da sincronização de regionais. Aí pode dar **Ctrl+C** para sair dos logs.

### 5. Verificar se está tudo no ar

```bash
docker compose ps
```

Todos os serviços devem estar **Up** e, após ~1 minuto, o backend deve aparecer como **(healthy)**.

Teste rápido:

```bash
curl -s http://localhost:8080/actuator/health/liveness
# Deve retornar {"status":"UP"} ou similar
```

### 6. Acessar a aplicação

- **Frontend:** http://localhost:3000  
- **Login:** `admin` / `admin123`  
- **Swagger:** http://localhost:8080/swagger-ui.html — ou use o link **API (Swagger)** no menu do frontend (abre em nova aba). Para passo a passo de testes via Swagger (login, Authorize com JWT, endpoints), veja [TESTES_VIA_SWAGGER.md](TESTES_VIA_SWAGGER.md).

---

## Se algo der errado

| Problema | O que fazer |
|----------|-------------|
| Porta em uso | Verifique se 3000, 8080, 5432, 9000 ou 9001 estão livres. Feche outros containers ou aplicações que usem essas portas. |
| Backend não fica (healthy) | Veja os logs: `docker compose logs backend`. Erros comuns: falha ao conectar no Postgres (confira POSTGRES_* no .env) ou JWT_SECRET muito curto (use o valor do .env.example). |
| Frontend não carrega | Confirme se o backend está healthy e se acessa http://localhost:8080. O front chama a API em 8080. |
| Só 3 regionais na tela | Clique em **Sincronizar** na tela Regionais. A sincronização também roda na subida do backend; se a API externa estiver lenta ou indisponível na hora, pode ter ficado só o seed. |

---

## Resumo para o avaliador

1. `cp .env.example .env`  
2. `docker compose up --build -d`  
3. Aguardar 1–2 min (logs do backend até "Started")  
4. Abrir http://localhost:3000 → login **admin** / **admin123**  

Com isso o projeto deve abrir e rodar sem erros para avaliação.
