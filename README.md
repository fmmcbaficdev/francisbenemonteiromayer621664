# 🎵 Sistema de Gerenciamento de Artistas e Álbuns

> Processo Seletivo Simplificado nº 001/2026/SEPLAG  
> Analista de Tecnologia da Informação - Desenvolvimento Full Stack Sênior

---

## 📋 Dados do Candidato

- **Nome:** Francis Bene Monteiro Mayer
- **Inscrição:** 16342
- **Projeto:** Full Stack
- **Email:** fmmcba1@gmail.com
- **GitHub:** https://github.com/fmmcbaficdev
- **Docker Hub:** https://hub.docker.com/repositories/francisbene

---

## 🚀 COMO EXECUTAR (Para o Avaliador)

> ⚠️ **ATENÇÃO:** Esta é a seção mais importante! Siga estes passos para executar o projeto.

### 📋 Pré-requisitos

Antes de começar, você precisa ter instalado:

- ✅ **Docker** versão 24.0 ou superior
- ✅ **Docker Compose** versão 2.20 ou superior
- ✅ **Portas livres:** 3001, 3002 (frontend), 8080 (backend), 5432 (postgres), 9000, 9001 (minio)
- ✅ **Recursos:** Mínimo 4GB de RAM disponível

**Verificar instalação:**

```bash
docker --version
```

```bash
docker-compose --version
```

---

### 🔧 Passo 1: Clonar ou Baixar o Repositório

**Opção A - Git (recomendado):**

```bash
git clone https://github.com/fmmcbaficdev/francisbenemonteiromayer621664.git
```

```bash
cd francisbenemonteiromayer621664
```

**Opção B - Download ZIP:**

1. Acesse: https://github.com/fmmcbaficdev/francisbenemonteiromayer621664
2. Clique em **Code** → **Download ZIP**
3. Extraia o arquivo ZIP
4. Abra o terminal na pasta extraída

---

### ⚙️ Passo 2: Configurar Variáveis de Ambiente

> ⚠️ **OBRIGATÓRIO:** Este passo NÃO pode ser pulado!

**No Linux / Mac / Git Bash:**

```bash
cp .env.example .env
```

**No Windows (PowerShell):**

```powershell
Copy-Item .env.example .env
```

**No Windows (CMD):**

```cmd
copy .env.example .env
```

> 💡 **Nota:** O arquivo `.env.example` já contém todos os valores necessários para executar o projeto. Você NÃO precisa editá-lo, apenas copiá-lo para `.env`.

---

### 🚢 Passo 3: Subir a Aplicação

> 📌 **Escolha UMA das opções abaixo:**

#### **Opção 1 - Build Local (RECOMENDADO para avaliação)**

```bash
docker compose up --build -d
```

Este comando irá:
- ✅ Fazer build das imagens localmente
- ✅ Criar a rede interna entre os serviços
- ✅ Subir PostgreSQL, MinIO, Backend e Frontend
- ✅ Aplicar migrations do banco
- ✅ Popular com dados iniciais

**Aguarde 1-2 minutos** para todos os serviços iniciarem.

#### **Opção 2 - Usar Imagens do Docker Hub (sem build)**

```bash
docker compose -f docker-compose.prod.yml pull
```

```bash
docker compose -f docker-compose.prod.yml up -d
```

> 💡 Esta opção baixa imagens já prontas do Docker Hub. Requer que `REGISTRY=francisbene` e `IMAGE_TAG=1.0.0` estejam no `.env`.

---

### ✅ Passo 4: Verificar se Está Rodando

```bash
docker compose ps
```

**Resultado esperado:**

```
NAME                STATUS              PORTS
seplag-backend      Up (healthy)        0.0.0.0:8080->8080/tcp
seplag-frontend     Up                  0.0.0.0:3001-3002->3001-3002/tcp
seplag-postgres     Up (healthy)        5432/tcp
seplag-minio        Up                  0.0.0.0:9000-9001->9000-9001/tcp
```

> ⚠️ O backend pode levar até **1 minuto** para ficar **(healthy)**. Aguarde!

**Acompanhar logs em tempo real:**

```bash
docker compose logs -f backend
```

Aguarde até aparecer: `Started BackendApplication`

---

### 🌐 Passo 5: Acessar a Aplicação

Abra seu navegador e acesse:

| 🎯 Serviço | 🔗 URL | 🔑 Credenciais |
|-----------|--------|---------------|
| **🖥️ Frontend (Principal)** | http://localhost:3001 | `admin` / `admin123` |
| **🖥️ Frontend (Alternativa)** | http://localhost:3002 | `admin` / `admin123` |
| **🔧 Backend API** | http://localhost:8080 | - |
| **📚 Swagger UI** | http://localhost:8080/swagger-ui.html | - |
| **💚 Health Check** | http://localhost:8080/actuator/health | - |
| **📦 MinIO Console** | http://localhost:9001 | `minioadmin` / `minioadmin123` |

---

### 🔐 Credenciais Padrão

#### Login na Aplicação (JWT):
- **Usuário:** `admin`
- **Senha:** `admin123`

#### MinIO Console:
- **Usuário:** `minioadmin`
- **Senha:** `minioadmin123`

#### PostgreSQL (se precisar acessar):
- **Usuário:** `seplag`
- **Senha:** `seplag123`
- **Banco:** `seplag`

---

### 🎯 Teste Rápido de Funcionamento

**1. Verificar se o backend está saudável:**

```bash
curl http://localhost:8080/actuator/health
```

**Resposta esperada:**

```json
{
  "status": "UP"
}
```

**2. Acessar o frontend:**

Abra http://localhost:3001 no navegador e faça login com `admin` / `admin123`

**3. Verificar Swagger:**

Abra http://localhost:8080/swagger-ui.html e teste os endpoints

---

## 🧪 Como Testar

### Via Swagger (Mais Fácil)

**1. Acesse o Swagger:**

```
http://localhost:8080/swagger-ui.html
```

**2. Faça Login:**

- Encontre `POST /v1/auth/login`
- Clique em **"Try it out"**
- Use o body:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

- Clique em **"Execute"**

**3. Copie o Token:**

- Na resposta, copie o valor de `accessToken`

**4. Autorize:**

- Clique no botão **"Authorize"** 🔓 no topo da página
- Cole: `Bearer <seu-token-aqui>`
- Clique em **"Authorize"**

**5. Teste os Endpoints:**

- Agora você pode testar qualquer endpoint
- Exemplo: `GET /v1/artistas` para listar artistas

📖 **Guia detalhado:** [docs/TESTES_VIA_SWAGGER.md](docs/TESTES_VIA_SWAGGER.md)

---

### Testes Automatizados

**Backend:**

```bash
cd backend
```

```bash
./mvnw test
```

**Frontend:**

```bash
cd frontend
```

```bash
npm test
```

---

## 🛑 Comandos Úteis

### Gerenciar Aplicação

**Ver status dos containers:**

```bash
docker compose ps
```

**Ver logs em tempo real (todos os serviços):**

```bash
docker compose logs -f
```

**Ver logs apenas do backend:**

```bash
docker compose logs -f backend
```

**Ver logs apenas do frontend:**

```bash
docker compose logs -f frontend
```

**Parar a aplicação:**

```bash
docker compose down
```

**Parar e remover volumes (limpar dados):**

```bash
docker compose down -v
```

**Reconstruir e subir novamente:**

```bash
docker compose up --build -d
```

**Reiniciar apenas o backend:**

```bash
docker compose restart backend
```

**Reiniciar apenas o frontend:**

```bash
docker compose restart frontend
```

---

## ⚠️ Problemas Comuns e Soluções

### ❌ Erro: "Porta já em uso"

**Se a porta 3001 ou 8080 estiver ocupada:**

**Windows - ver o que usa a porta:**

```bash
netstat -ano | findstr :3001
```

**Linux/Mac:**

```bash
lsof -i :3001
```

**Solução:** Use a porta alternativa do frontend (3002) ou pare o serviço que está usando a porta.

---

### ❌ Erro: "arquivo .env não encontrado"

**Causa:** Você não copiou o `.env.example` para `.env`

**Solução Linux/Mac/Git Bash:**

```bash
cp .env.example .env
```

**Solução Windows PowerShell:**

```powershell
Copy-Item .env.example .env
```

**Solução Windows CMD:**

```cmd
copy .env.example .env
```

---

### ❌ Backend não inicia ou não fica (healthy)

**Ver logs do backend:**

```bash
docker compose logs backend
```

**Ver logs do postgres:**

```bash
docker compose logs postgres
```

**Verificar variáveis de ambiente:**

```bash
docker compose exec backend env | grep POSTGRES
```

**Solução comum:** Certifique-se que o `.env` tem `POSTGRES_HOST=postgres`

---

### ❌ Frontend não conecta ao backend (CORS error)

**Verificar configuração CORS:**

```bash
docker compose exec backend env | grep CORS_ALLOWED_ORIGINS
```

**Deve conter:** `http://localhost:3001,http://localhost:3002`

**Se não estiver correto, edite o `.env` e reinicie:**

```bash
docker compose restart backend
```

---

### ❌ Containers não sobem

**Limpar tudo e tentar novamente:**

```bash
docker compose down -v
```

```bash
docker system prune -a
```

```bash
docker compose up --build -d
```

---

### ❌ MinIO / Upload de capas não funciona

**Ver logs do MinIO:**

```bash
docker compose logs minio-init
```

**Verificar se o bucket foi criado:**

```bash
docker compose exec minio mc ls myminio/
```

**Recriar o bucket manualmente (se necessário):**

```bash
docker compose exec minio mc mb myminio/capas
```

```bash
docker compose exec minio mc anonymous set download myminio/capas
```

---

### ❌ Imagens não baixam do Docker Hub

**Tentar baixar manualmente:**

```bash
docker pull francisbene/seplag-backend:latest
```

```bash
docker pull francisbene/seplag-frontend:latest
```

**Se funcionar, depois rode:**

```bash
docker compose -f docker-compose.prod.yml up -d
```

---

## 📚 Documentação Adicional

### Para o Avaliador

- 📖 **Índice completo:** [docs/README.md](docs/README.md)
- 🚀 **Guia rápido:** [docs/COMO_ABRIR_PARA_AVALIADOR.md](docs/COMO_ABRIR_PARA_AVALIADOR.md)
- 🐛 **Troubleshooting:** [docs/PROBLEMAS_COMUNS_AVALIADOR.md](docs/PROBLEMAS_COMUNS_AVALIADOR.md)
- ✅ **Checklist de requisitos:** [docs/REQUISITOS_SENIOR_AVALIACAO_E_COMO_TESTAR.md](docs/REQUISITOS_SENIOR_AVALIACAO_E_COMO_TESTAR.md)
- 📋 **Revisão do edital:** [docs/REVISAO_EDITAL_ANEXO_IIC.md](docs/REVISAO_EDITAL_ANEXO_IIC.md)
- 🧪 **Testes via Swagger:** [docs/TESTES_VIA_SWAGGER.md](docs/TESTES_VIA_SWAGGER.md)
- 🚀 **Deploy produção:** [docs/DEPLOY_PRODUCAO.md](docs/DEPLOY_PRODUCAO.md)

---

## ✅ Funcionalidades Implementadas

### 🎯 Requisitos Obrigatórios

- ✅ **CRUD completo** de Artistas e Álbuns
- ✅ **Relacionamento N:N** entre Artistas e Álbuns
- ✅ **Upload de imagens** de capa (MinIO/S3)
- ✅ **Autenticação JWT** com expiração (5 min) e renovação automática
- ✅ **Paginação e ordenação** de resultados
- ✅ **Filtros** por nome/título
- ✅ **WebSocket** para notificações em tempo real
- ✅ **Sincronização** com API externa de Regionais (complexidade O(n))
- ✅ **Health checks** (Liveness/Readiness)
- ✅ **Rate limiting** (10 requisições/minuto por usuário)
- ✅ **Testes unitários** (backend e frontend)
- ✅ **Documentação OpenAPI/Swagger**
- ✅ **Migrations** com Flyway
- ✅ **Docker** e **Docker Compose**

### 🎁 Funcionalidades Extras

- ✅ **Arquitetura Hexagonal** (Ports and Adapters)
- ✅ **Facade Pattern** no frontend
- ✅ **BehaviorSubject** para gerenciamento de estado
- ✅ **Lazy Loading** de rotas
- ✅ **Optimistic Locking** (versionamento de entidades)
- ✅ **Presigned URLs** para segurança de imagens
- ✅ **Seed data** com artistas do edital
- ✅ **Multi-stage build** Docker otimizado

---

## 🛠️ Stack Tecnológica

### Backend

| Tecnologia | Versão | Descrição |
|------------|--------|-----------|
| **Spring Boot** | 3.5.9 | Framework principal |
| **Java** | 21 (LTS) | Linguagem |
| **Maven** | 4.0.0 | Build tool |
| **PostgreSQL** | 15+ | Banco de dados (produção) |
| **H2 Database** | - | Banco de dados (testes) |
| **Flyway** | - | Migrations |
| **MinIO** | 8.5.7 | Storage S3-compatible |
| **JWT** | 0.12.3 | Autenticação (io.jsonwebtoken) |
| **SpringDoc OpenAPI** | 2.8.4 | Swagger UI |
| **WebSocket** | - | STOMP + SockJS |
| **Bucket4j** | 8.7.0 | Rate limiting |
| **JUnit 5** | - | Testes unitários |
| **Mockito** | - | Mocks para testes |

### Frontend

| Tecnologia | Versão | Descrição |
|------------|--------|-----------|
| **React** | 19.x | Framework UI |
| **TypeScript** | 5.x | Linguagem |
| **Vite** | 7.x | Build tool |
| **Tailwind CSS** | 4.x | Estilização |
| **RxJS** | 7.x | Gerenciamento de estado (BehaviorSubject) |
| **Axios** | - | HTTP client |
| **React Hook Form** | - | Validação de formulários |
| **Zod** | - | Schema validation |
| **STOMP.js** | 7 | WebSocket client |
| **SockJS** | - | WebSocket fallback |
| **React Router** | 7.x | Roteamento (Lazy Loading) |
| **Vitest** | - | Testes unitários |
| **React Testing Library** | - | Testes de componentes |
| **ESLint** | - | Padronização de código |

---

## 🔄 Principais Endpoints

### 🔐 Autenticação

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/v1/auth/login` | Login e geração de token JWT |
| POST | `/v1/auth/refresh` | Renovação de token expirado |

### 🎤 Artistas

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/v1/artistas` | Listar artistas (paginado, ordenado) |
| GET | `/v1/artistas/{id}` | Buscar artista por ID |
| GET | `/v1/artistas/buscar?nome=` | Buscar artista por nome |
| POST | `/v1/artistas` | Criar novo artista |
| PUT | `/v1/artistas/{id}` | Atualizar artista |
| DELETE | `/v1/artistas/{id}` | Remover artista |

### 💿 Álbuns

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/v1/albuns` | Listar álbuns (paginado, ordenado) |
| GET | `/v1/albuns/{id}` | Buscar álbum por ID |
| GET | `/v1/albuns/artista/{artistaId}` | Listar álbuns de um artista |
| GET | `/v1/albuns/buscar?titulo=` | Buscar álbum por título |
| POST | `/v1/albuns` | Criar novo álbum |
| PUT | `/v1/albuns/{id}` | Atualizar álbum |
| DELETE | `/v1/albuns/{id}` | Remover álbum |
| POST | `/v1/albuns/{id}/imagens` | Upload de capas (múltiplas imagens) |
| DELETE | `/v1/albuns/{albumId}/imagens/{imagemId}` | Remover capa específica |

### 🗺️ Regionais

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/v1/regionais` | Listar regionais |
| POST | `/v1/regionais/sincronizar` | Sincronizar com API externa |

### 💚 Health & Monitoring

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/actuator/health` | Status geral da aplicação |
| GET | `/actuator/health/liveness` | Liveness probe (Kubernetes) |
| GET | `/actuator/health/readiness` | Readiness probe (Kubernetes) |

---

## 📂 Estrutura do Projeto

```
francisbenemonteiromayer621664/
│
├── backend/                          # Aplicação Spring Boot
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/seplag/backend/
│   │   │   │   ├── controller/       # REST Controllers
│   │   │   │   ├── service/          # Lógica de negócio
│   │   │   │   ├── repository/       # JPA Repositories
│   │   │   │   ├── model/            # Entidades JPA
│   │   │   │   ├── dto/              # Data Transfer Objects
│   │   │   │   ├── config/           # Configurações (CORS, Security, MinIO)
│   │   │   │   └── security/         # JWT, Rate Limiting
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── db/migration/     # Flyway migrations
│   │   └── test/                     # Testes unitários
│   ├── Dockerfile                    # Multi-stage build
│   └── pom.xml                       # Dependências Maven
│
├── frontend/                         # Aplicação React
│   ├── src/
│   │   ├── components/               # Componentes reutilizáveis
│   │   ├── pages/                    # Páginas da aplicação
│   │   ├── context/                  # Context API (Auth)
│   │   ├── core/                     # Services e Facades
│   │   ├── shared/                   # Utilitários e types
│   │   ├── App.tsx                   # Componente raiz
│   │   └── main.tsx                  # Entry point
│   ├── Dockerfile                    # Multi-stage build + Nginx
│   ├── nginx.conf                    # Configuração Nginx
│   ├── package.json                  # Dependências npm
│   ├── tsconfig.json                 # TypeScript config
│   ├── tailwind.config.js            # Tailwind config
│   └── vite.config.ts                # Vite config
│
├── docs/                             # Documentação adicional
│   ├── README.md                     # Índice da documentação
│   ├── COMO_ABRIR_PARA_AVALIADOR.md
│   ├── PROBLEMAS_COMUNS_AVALIADOR.md
│   ├── REQUISITOS_SENIOR_AVALIACAO_E_COMO_TESTAR.md
│   ├── REVISAO_EDITAL_ANEXO_IIC.md
│   ├── TESTES_VIA_SWAGGER.md
│   └── DEPLOY_PRODUCAO.md
│
├── docker-compose.yml                # Desenvolvimento (build local)
├── docker-compose.prod.yml           # Produção (imagens Docker Hub)
├── .env.example                      # Template de variáveis
├── .gitignore                        # Arquivos ignorados
└── README.md                         # Este arquivo
```

---

## 📦 Dados Pré-Carregados

O sistema já vem com os artistas e álbuns especificados no edital:

| 🎤 Artista | 💿 Álbuns |
|-----------|----------|
| **Serj Tankian** | • Harakiri<br>• Black Blooms<br>• The Rough Dog |
| **Mike Shinoda** | • The Rising Tied<br>• Post Traumatic<br>• Post Traumatic EP<br>• Where'd You Go |
| **Michel Teló** | • Bem Sertanejo<br>• Bem Sertanejo - O Show (Ao Vivo)<br>• Bem Sertanejo - (1ª Temporada) - EP |
| **Guns N' Roses** | • Use Your Illusion I<br>• Use Your Illusion II<br>• Greatest Hits |

> 💡 **Nota:** As capas dos álbuns não vêm pré-carregadas. Elas precisam ser enviadas via upload na tela de edição do álbum.

---

## 📊 Modelo de Dados

### Entidades Principais

```sql
-- Artistas
CREATE TABLE artistas (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    biografia TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    version INTEGER DEFAULT 0  -- Optimistic locking
);

-- Álbuns
CREATE TABLE albuns (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    ano_lancamento INTEGER,
    descricao TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    version INTEGER DEFAULT 0
);

-- Relacionamento N:N
CREATE TABLE artista_album (
    artista_id BIGINT NOT NULL,
    album_id BIGINT NOT NULL,
    papel VARCHAR(100),  -- Ex: "vocalista", "guitarrista"
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (artista_id, album_id),
    FOREIGN KEY (artista_id) REFERENCES artistas(id) ON DELETE CASCADE,
    FOREIGN KEY (album_id) REFERENCES albuns(id) ON DELETE CASCADE
);

-- Capas dos álbuns
CREATE TABLE imagens_capa (
    id BIGSERIAL PRIMARY KEY,
    album_id BIGINT NOT NULL,
    nome_arquivo VARCHAR(255) NOT NULL,
    caminho_minio VARCHAR(500) NOT NULL UNIQUE,
    content_type VARCHAR(100) NOT NULL,
    tamanho BIGINT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    uploaded_by VARCHAR(100),
    FOREIGN KEY (album_id) REFERENCES albuns(id) ON DELETE CASCADE
);

-- Usuários
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- BCrypt
    nome VARCHAR(200) NOT NULL,
    email VARCHAR(200) UNIQUE,
    ativo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Regionais (sincronização externa)
CREATE TABLE regionais (
    id BIGSERIAL PRIMARY KEY,
    codigo_externo INTEGER UNIQUE NOT NULL,
    nome VARCHAR(200) NOT NULL,
    ativa BOOLEAN DEFAULT TRUE,
    external_hash VARCHAR(64),  -- MD5 para detectar mudanças
    ultima_sincronizacao TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 🏛️ Decisões Arquiteturais

### 1️⃣ Por que Arquitetura Hexagonal?

**Problema:** Aplicações monolíticas acoplam lógica de negócio a frameworks.

**Solução:** Hexagonal separa domínio de infraestrutura.

**Benefícios:**
- ✅ **Testabilidade:** Domain sem dependências externas
- ✅ **Flexibilidade:** Trocar PostgreSQL → MongoDB sem afetar domínio
- ✅ **Manutenibilidade:** Mudanças isoladas
- ✅ **Clean Architecture:** Dependências apontam para dentro

**Estrutura:**

```
Domain (Core)
├── Entities (Artista, Album)
├── Value Objects
└── Business Rules

Application
├── Use Cases
├── Ports (Interfaces)
└── DTOs

Infrastructure
├── Adapters (REST, JPA, MinIO)
├── Configuration
└── Security
```

---

### 2️⃣ Relacionamento N:N (artista_album)

**Decisão:** Criar tabela intermediária em vez de FK simples.

**Justificativa:**
- ✅ **Colaborações:** Álbuns com múltiplos artistas (ex: "Collision Course" = Jay-Z + Linkin Park)
- ✅ **Flexibilidade:** Campo "papel" para indicar função (vocalista, guitarrista)
- ✅ **Normalização:** Evita duplicação de dados

---

### 3️⃣ BIGSERIAL vs UUID

**Decisão:** Usar BIGSERIAL (IDs sequenciais).

**Justificativa:**
- ✅ **Simplicidade:** Sem necessidade de `gen_random_uuid()`
- ✅ **Performance:** Índices menores (8 bytes vs 16 bytes)
- ✅ **Legibilidade:** URLs amigáveis (`/artistas/1` vs `/artistas/a1b2c3...`)

---

### 4️⃣ MinIO para Upload de Imagens

**Por que não salvar no PostgreSQL?**

| Critério | PostgreSQL (BYTEA) | MinIO (S3) |
|----------|-------------------|------------|
| **Performance** | ❌ Lento para binários | ✅ Otimizado para streaming |
| **Escalabilidade** | ❌ Vertical apenas | ✅ Horizontal |
| **Custo** | ❌ Storage de DB é caro | ✅ Storage S3 é barato |
| **CDN** | ❌ Difícil integração | ✅ CloudFront/CloudFlare |
| **Backup** | ❌ Aumenta muito backup DB | ✅ Backup independente |

**Presigned URLs (segurança):**

```java
// Backend gera URL temporária válida por 30 minutos
String url = minioClient.getPresignedObjectUrl(
    GetPresignedObjectUrlArgs.builder()
        .bucket("album-covers")
        .object("albums/100/abc123.jpg")
        .expiry(30, TimeUnit.MINUTES)  // ✅ Expira em 30 min
        .build()
);
```

---

### 5️⃣ JWT com Expiração Curta (5 min)

**Por que 5 minutos?**

- ✅ **Segurança:** Janela de ataque reduzida se token vazar
- ✅ **UX:** Renovação automática transparente ao usuário
- ✅ **Compliance:** Padrão OWASP

**Fluxo de Renovação Automática:**

```
1. Token expira em 5 min
2. Frontend detecta 401
3. Interceptor Axios chama /auth/refresh
4. Recebe novo token
5. Reexecuta requisição original
6. Usuário nem percebe
```

**Implementação no Frontend:**

```typescript
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      await refreshAccessToken();  // Renova automaticamente
      return api(originalRequest);  // Reexecuta requisição
    }
    return Promise.reject(error);
  }
);
```

---

### 6️⃣ Rate Limiting (10 req/min por usuário)

**Implementação com Bucket4j:**

```java
@Component
public class RateLimitingInterceptor {
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String username) {
        return cache.computeIfAbsent(username, k -> 
            Bucket.builder()
                .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1))))
                .build()
        );
    }
}
```

**Benefícios:**
- ✅ **Justo:** Um usuário malicioso não afeta outros
- ✅ **Escalável:** Cada usuário tem seu bucket independente
- ✅ **Simples:** Implementação com poucas linhas

---

### 7️⃣ Sincronização de Regionais - Complexidade O(n)

**Algoritmo:**

```java
// 1. Buscar externos - O(n)
Regional[] externos = restTemplate.getForObject(API_URL, Regional[].class);

// 2. Mapear locais em HashMap - O(n)
Map<Integer, Regional> locaisMap = regionalRepository.findAll()
    .stream()
    .collect(Collectors.toMap(Regional::getCodigoExterno, r -> r));

// 3. Processar cada externo - O(n)
for (Regional externo : externos) {
    Regional local = locaisMap.get(externo.getCodigoExterno());  // O(1)
    
    String hashExterno = DigestUtils.md5Hex(externo.getNome());
    
    if (local == null) {
        // NOVO → inserir
    } else if (!hashExterno.equals(local.getExternalHash())) {
        // ALTERADO → inativar anterior e criar novo (SCD Type 2)
    }
}

// 4. Inativar removidos - O(n)
// Total: O(4n) = O(n)
```

**Por que HashMap em vez de Query para cada registro?**

| Abordagem | Complexidade | Performance |
|-----------|-------------|-------------|
| **Query por registro** | O(n²) | ❌ N queries ao banco |
| **HashMap (nossa escolha)** | O(n) | ✅ 2 queries apenas |

---

### 8️⃣ WebSocket para Notificações em Tempo Real

**Fluxo:**

```
1. Usuário A cria álbum
2. Backend salva no PostgreSQL
3. Backend envia: messagingTemplate.convertAndSend("/topic/albums", notification)
4. Todos os clientes conectados recebem
5. Frontend exibe toast: "🎵 Novo álbum: [título]"
6. Frontend atualiza lista automaticamente
```

**Configuração Backend:**

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");  // Canal de broadcast
        config.setApplicationDestinationPrefixes("/app");
    }
}
```

**Configuração Frontend:**

```typescript
const stompClient = Stomp.over(new SockJS('http://localhost:8080/ws'));
stompClient.connect({}, () => {
  stompClient.subscribe('/topic/albums', (message) => {
    const album = JSON.parse(message.body);
    showToast(`🎵 Novo álbum: ${album.titulo}`);
  });
});
```

---

### 9️⃣ Facade Pattern no Frontend

**Problema:** Componentes acoplados a múltiplos services.

**Solução:** Interface única (Facade) que encapsula complexidade.

**Benefícios:**
- ✅ **Simplificação:** Componente chama 1 método, não 5
- ✅ **Desacoplamento:** Componente não conhece detalhes de implementação
- ✅ **Manutenibilidade:** Mudanças centralizadas

**Exemplo:**

```typescript
// Antes (sem Facade)
const artistas = await artistaService.listar();
const albuns = await albumService.listarPorArtista(id);
await minioService.uploadImagem(file);

// Depois (com Facade)
const dados = await seplagFacade.carregarDashboard();
```

---

### 🔟 BehaviorSubject para Gestão de Estado

**Por que BehaviorSubject em vez de Redux?**

| Critério | Redux | BehaviorSubject (RxJS) |
|----------|-------|------------------------|
| **Boilerplate** | ❌ Muito (actions, reducers, store) | ✅ Mínimo |
| **Reatividade** | ✅ Sim | ✅ Sim |
| **Estado inicial** | ✅ Sim | ✅ Sim |
| **DevTools** | ✅ Excelente | ⚠️ Básico |
| **Curva aprendizado** | ❌ Alta | ✅ Baixa |

**Exemplo:**

```typescript
class AuthState {
  private userSubject = new BehaviorSubject<User | null>(null);
  public user$ = this.userSubject.asObservable();

  login(user: User) {
    this.userSubject.next(user);  // Emite novo valor
  }
}

// Componente se inscreve
authState.user$.subscribe(user => {
  console.log('Usuário mudou:', user);
});
```

---

## ✅ Checklist de Requisitos do Edital

### Backend (50 pontos)

#### ✅ CRUD, JWT e MinIO (0-7 pontos)

- [x] **CRUD completo** para Artistas e Álbuns
- [x] **Relacionamento N:N** (artista_album)
- [x] **JWT** com expiração de 5 minutos
- [x] **Renovação** automática de token
- [x] **Upload** de múltiplas imagens para MinIO
- [x] **Presigned URLs** com expiração de 30 minutos
- [x] **CORS** configurado

#### ✅ Paginação e Filtros (0-3 pontos)

- [x] **Paginação** em listagem de artistas
- [x] **Paginação** em listagem de álbuns
- [x] **Filtro por nome** com ordenação ASC/DESC
- [x] **Parâmetros** customizáveis (page, size, sort)

#### ✅ Rate Limit e Sincronização (0-3 pontos)

- [x] **Rate limiting** 10 req/min por usuário (Bucket4j)
- [x] **Sincronização** de regionais com complexidade **O(n)**
- [x] **Detecção de mudanças** via hash MD5
- [x] **Inativação** de registros removidos (SCD Type 2)

#### ✅ Swagger, Migrations e Health Check (0-3 pontos)

- [x] **OpenAPI/Swagger** com documentação completa
- [x] **Flyway migrations** versionadas
- [x] **Seed data** com artistas do edital
- [x] **Health checks** (liveness/readiness)
- [x] **Actuator** endpoints expostos

#### ✅ WebSocket e Notificações (0-4 pontos)

- [x] **WebSocket** configurado (STOMP + SockJS)
- [x] **Notificações** ao criar álbum
- [x] **Broadcasting** para todos os clientes
- [x] **Integração** com frontend

#### ✅ Health Checks / Liveness (0-3 pontos)

- [x] **Endpoint** `/actuator/health`
- [x] **Liveness** `/actuator/health/liveness`
- [x] **Readiness** `/actuator/health/readiness`

#### ✅ Testes Unitários (0-4 pontos)

- [x] **Cobertura** de services
- [x] **Cobertura** de controllers
- [x] **Cobertura** de repositories
- [x] **JUnit 5** + **Mockito**

---

### Frontend (50 pontos)

#### ✅ Consumo de API (0-6 pontos)

- [x] **CRUD** de artistas funcional
- [x] **CRUD** de álbuns funcional
- [x] **Autenticação** JWT integrada
- [x] **Upload** de imagens
- [x] **Interceptor** para renovação automática de token
- [x] **Tratamento de erros**

#### ✅ Paginação e Busca (0-3 pontos)

- [x] **Paginação** em listagem de artistas
- [x] **Paginação** em listagem de álbuns
- [x] **Busca** por nome/título

#### ✅ Autenticação JWT (0-5 pontos)

- [x] **Login** funcional
- [x] **Expiração** e renovação do token
- [x] **Gerenciamento** de estado de autenticação
- [x] **Proteção** de rotas

#### ✅ Upload de Imagens (0-3 pontos)

- [x] **Upload** funcional
- [x] **Preview** de imagens
- [x] **Feedback** visual

#### ✅ Lazy Loading (0-2 pontos)

- [x] **Rotas** carregadas sob demanda
- [x] **Code splitting**

#### ✅ State Management (Sênior) (0-3 pontos)

- [x] **BehaviorSubject** implementado
- [x] **Facade Pattern** implementado
- [x] **Gerenciamento** reativo

#### ✅ Testes Unitários (0-3 pontos)

- [x] **Vitest** configurado
- [x] **React Testing Library**
- [x] **Cobertura** de componentes

---

### Full Stack Adicional (10 pontos)

#### ✅ Integração Back + Front (0-6 pontos)

- [x] **Comunicação** entre serviços
- [x] **CORS** configurado
- [x] **WebSocket** funcionando
- [x] **Autenticação** integrada

#### ✅ Docker Compose (0-4 pontos)

- [x] **Backend** containerizado
- [x] **Frontend** containerizado
- [x] **PostgreSQL** containerizado
- [x] **MinIO** containerizado
- [x] **Orquestração** funcional

---

## 📞 Contato

**Candidato:** Francis Bene Monteiro Mayer  
**Email:** fmmcba1@gmail.com  
**GitHub:** https://github.com/fmmcbaficdev  
**Docker Hub:** https://hub.docker.com/u/francisbene  
**Inscrição:** 16342

---

## 📄 Referências

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Documentation](https://react.dev/)
- [MinIO Documentation](https://min.io/docs)
- [JWT.io](https://jwt.io/)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Docker Documentation](https://docs.docker.com/)
- [Tailwind CSS](https://tailwindcss.com/)
- [RxJS](https://rxjs.dev/)
- [Bucket4j](https://bucket4j.com/)

---

**🎯 Desenvolvido para o Processo Seletivo Simplificado nº 001/2026/SEPLAG — Janeiro de 2026**