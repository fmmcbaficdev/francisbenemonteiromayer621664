# Como abrir o projeto para avaliação (sem erros)

Este guia garante que o avaliador consiga subir e testar o projeto em poucos passos.

---

## Pré-requisitos

- **Docker** e **Docker Compose** instalados (versão recente)
- **Portas livres:** 3001 e 3002 (frontend), 8080, 5432, 9000, 9001
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

- **Frontend:** http://localhost:3001 (ou http://localhost:3002 se 3001 estiver em uso)  
- **Login:** `admin` / `admin123`  
- **Swagger:** http://localhost:8080/swagger-ui.html — ou use o link **API (Swagger)** no menu do frontend (abre em nova aba). Para passo a passo de testes via Swagger (login, Authorize com JWT, endpoints), veja [TESTES_VIA_SWAGGER.md](TESTES_VIA_SWAGGER.md).

---

## Se algo der errado

| Problema | O que fazer |
|----------|-------------|
| Porta em uso | O frontend usa **3001 e 3002** (duas portas); use a que estiver livre. Se 3001 e 3002 estiverem ocupadas, veja [PROBLEMAS_COMUNS_AVALIADOR.md](PROBLEMAS_COMUNS_AVALIADOR.md). |
| Backend não fica (healthy) | Veja os logs: `docker compose logs backend`. Erros comuns: falha ao conectar no Postgres (confira POSTGRES_* no .env) ou JWT_SECRET muito curto (use o valor do .env.example). |
| Frontend não carrega | Confirme se o backend está healthy e se acessa http://localhost:8080. O front chama a API em 8080. |
| Só 3 regionais na tela | Clique em **Sincronizar** na tela Regionais. A sincronização também roda na subida do backend; se a API externa estiver lenta ou indisponível na hora, pode ter ficado só o seed. |

**Mais situações e soluções:** [PROBLEMAS_COMUNS_AVALIADOR.md](PROBLEMAS_COMUNS_AVALIADOR.md) — esqueceu o `.env`, primeira subida lenta, 429, upload, Windows, etc.

---

## Resumo para o avaliador

1. `cp .env.example .env`  
2. `docker compose up --build -d`  
3. Aguardar 1–2 min (logs do backend até "Started")  
4. Abrir http://localhost:3001 (ou 3002) → login **admin** / **admin123**  

Com isso o projeto deve abrir e rodar sem erros para avaliação.

---

## Sobre as imagens de capa dos álbuns

As **capas dos álbuns** são armazenadas no **MinIO** (objeto por objeto), não no banco. O banco guarda só o **caminho** da imagem (tabela `imagens_capa`). Por isso:

- Em **outra máquina** ou em um **novo clone** do repositório, o MinIO sobe **vazio** (volume novo). Os artistas e álbuns vêm do seed (Flyway), mas **não há imagens de capa** até alguém fazer upload.
- Isso é esperado: o edital pede “upload de uma ou mais imagens” e “recuperar via presigned URL”; a persistência é no MinIO por ambiente. Em uma máquina nova, o avaliador **pode testar o upload** na tela de edição de um álbum (adicionar capa, salvar) e as imagens passam a aparecer e a persistir enquanto o ambiente (Docker/volumes) for o mesmo.

**Resumo:** No primeiro acesso (ou em outra máquina), não haverá capas pré-carregadas. O avaliador pode verificar o requisito de upload indo em **Álbuns → Editar um álbum → enviar uma ou mais imagens**; após o upload, as capas ficam no MinIO e aparecem na listagem e no detalhe.
