# 🎵 Sistema de Gerenciamento de Artistas e Álbuns
**Projeto Prático - Concurso Público SEPLAG/MT 2026 (PSS 001/2026)**

## 📋 Identificação
- **Candidato:** Francisbene Monteiro Mayer
- **Inscrição:** (informar no ato da inscrição no SIES)
- **Vaga:** Analista de TI - Desenvolvimento Full Stack Sênior
- **Cargo:** Analista de Sistemas
- **Email:** fmmcba1@gmail.com
- **GitHub:** https://github.com/fmmcbaficdev

---

## 🎯 Visão Geral do Projeto

Sistema full stack para gerenciamento de artistas musicais e seus álbuns, com autenticação JWT, upload de imagens para MinIO, notificações em tempo real via WebSocket e sincronização de dados externos.

### 🏗️ Arquitetura
```
┌─────────────┐     ┌──────────────┐     ┌──────────┐
│   React     │────▶│  Spring Boot │────▶│PostgreSQL│
│  Frontend   │◀────│   Backend    │◀────│          │
└─────────────┘     └──────────────┘     └──────────┘
      │                    │
      │                    ▼
      │             ┌──────────┐
      └────────────▶│  MinIO   │
       (Upload)     └──────────┘
```

**Padrão Arquitetural:** Hexagonal (Ports and Adapters)
- **Domain:** Regras de negócio puras (sem dependências)
- **Application:** Casos de uso e orquestração
- **Infrastructure:** Adapters para frameworks (Spring, JPA, REST)

### 🛠️ Stack Tecnológica

#### Backend
- **Framework:** Spring Boot 3.5.9
- **Linguagem:** Java 21 (LTS)
- **Build Tool:** Maven (Model 4.0.0)
- **Banco de Dados:** PostgreSQL (Produção) / H2 Database (Testes)
- **Migrations:** Flyway (Core & PostgreSQL)
- **Storage:** MinIO (S3-compatible) - v8.5.7
- **Autenticação:** JWT (io.jsonwebtoken:jjwt v0.12.3)
- **Documentação:** SpringDoc OpenAPI v2.8.4 (Swagger UI)
- **WebSocket:** Spring Boot Starter WebSocket + STOMP
- **Rate Limiting:** Bucket4j v8.7.0
- **Testes:** JUnit 5, Mockito, Spring Security Test

#### Frontend
- **Framework:** React 19.x
- **Linguagem:** TypeScript 5.x
- **Build Tool:** Vite 7.x
- **Estilização:** Tailwind CSS 4.x
- **State Management:** RxJS 7.x (BehaviorSubject para estados globais)
- **HTTP Client:** Axios
- **Form Validation:** React Hook Form + Zod (peer)
- **WebSocket:** STOMP.js 7 + SockJS
- **Routing:** React Router 7.x (Lazy Loading)
- **Testes:** Configuração para Vitest + React Testing Library (ver seção Testes)
- **Padronização:** ESLint

---

## 🚀 Como Executar

> **Para o avaliador:** use o guia **[COMO_ABRIR_PARA_AVALIADOR.md](docs/COMO_ABRIR_PARA_AVALIADOR.md)** — passos mínimos para abrir o projeto sem erros. Em caso de problemas ao executar, consulte **[PROBLEMAS_COMUNS_AVALIADOR.md](docs/PROBLEMAS_COMUNS_AVALIADOR.md)**. Revisão item a item conforme o edital (Anexo II-C): **[REVISAO_EDITAL_ANEXO_IIC.md](docs/REVISAO_EDITAL_ANEXO_IIC.md)**.

### Pré-requisitos
- Docker 24.0+ e Docker Compose 2.20+
- Portas livres: **3001 e 3002** (frontend), 8080, 5432, 9000, 9001
- Mínimo 4GB RAM disponível

### Execução em 3 Passos
```bash
# 1. Clonar o repositório
git clone https://github.com/fmmcbaficdev/francisbenemonteiromayer621664
cd francisbenemonteiromayer621664

# 2. Configurar variáveis de ambiente (não é necessário editar — valores do .env.example funcionam)
cp .env.example .env

# 3. Subir todos os serviços
docker compose up --build -d

# 4. Aguardar inicialização (1-2 minutos)
docker compose logs -f backend
```

### Acessos

| Serviço | URL | Credenciais |
|---------|-----|-------------|
| **Frontend** | http://localhost:3001 ou http://localhost:3002 | admin / admin123 |
| **Backend API** | http://localhost:8080 | - |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | - |
| **MinIO Console** | http://localhost:9001 | minioadmin / minioadmin123 (se usou .env.example) |
| **PostgreSQL** | localhost:5432 | seplag / seplag123 (se usou .env.example) |

### Verificação de Saúde
```bash
# Backend
curl http://localhost:8080/actuator/health

# Frontend
curl http://localhost:3001

# MinIO
curl http://localhost:9000/minio/health/live
```

---

## 📊 Modelo de Dados

### Entidades Principais
```sql
artistas (Artistas)
├─ id: BIGSERIAL PRIMARY KEY
├─ nome: VARCHAR(200) NOT NULL
├─ biografia: TEXT
├─ created_at: TIMESTAMP
├─ created_by: VARCHAR(100)
├─ updated_at: TIMESTAMP
├─ updated_by: VARCHAR(100)
└─ version: INTEGER (optimistic locking)

albuns (Álbuns)
├─ id: BIGSERIAL PRIMARY KEY
├─ titulo: VARCHAR(200) NOT NULL
├─ ano_lancamento: INTEGER
├─ descricao: TEXT
├─ created_at: TIMESTAMP
├─ created_by: VARCHAR(100)
├─ updated_at: TIMESTAMP
├─ updated_by: VARCHAR(100)
└─ version: INTEGER

artista_album (Relacionamento N:N)
├─ artista_id: BIGINT FK → artistas(id)
├─ album_id: BIGINT FK → albuns(id)
├─ papel: VARCHAR(100) (opcional)
└─ created_at: TIMESTAMP
    PRIMARY KEY (artista_id, album_id)

imagens_capa (Capas dos Álbuns)
├─ id: BIGSERIAL PRIMARY KEY
├─ album_id: BIGINT FK → albuns(id)
├─ nome_arquivo: VARCHAR(255) NOT NULL
├─ caminho_minio: VARCHAR(500) NOT NULL UNIQUE
├─ content_type: VARCHAR(100) NOT NULL
├─ tamanho: BIGINT NOT NULL
├─ uploaded_at: TIMESTAMP
└─ uploaded_by: VARCHAR(100)

usuarios (Usuários)
├─ id: BIGSERIAL PRIMARY KEY
├─ username: VARCHAR(50) UNIQUE NOT NULL
├─ password: VARCHAR(255) NOT NULL (BCrypt)
├─ nome: VARCHAR(200) NOT NULL
├─ email: VARCHAR(200) UNIQUE
├─ ativo: BOOLEAN
└─ created_at: TIMESTAMP

regionais (Sincronização Externa)
├─ id: BIGSERIAL PRIMARY KEY
├─ codigo_externo: INTEGER UNIQUE NOT NULL
├─ nome: VARCHAR(200) NOT NULL
├─ ativa: BOOLEAN
├─ external_hash: VARCHAR(64) (MD5 para detectar mudanças)
├─ ultima_sincronizacao: TIMESTAMP
├─ created_at: TIMESTAMP
└─ updated_at: TIMESTAMP
```

### Relacionamentos
- `artista_album.artista_id → artistas(id)` (ON DELETE CASCADE)
- `artista_album.album_id → albuns(id)` (ON DELETE CASCADE)
- `imagens_capa.album_id → albuns(id)` (ON DELETE CASCADE)

---

## 🏛️ Decisões Arquiteturais

### 1. Por que Arquitetura Hexagonal?

**Problema:** Aplicações monolíticas acoplam lógica de negócio a frameworks.

**Solução:** Hexagonal separa domínio de infraestrutura.

**Benefícios:**
- ✅ **Testabilidade:** Domain sem dependências externas
- ✅ **Flexibilidade:** Trocar PostgreSQL → MongoDB sem afetar domínio
- ✅ **Manutenibilidade:** Mudanças isoladas
- ✅ **Clean Architecture:** Dependências apontam para dentro

---

### 2. Relacionamento N:N (artista_album)

**Decisão:** Criar tabela intermediária em vez de FK simples.

**Justificativa:**
- ✅ **Colaborações:** Álbuns com múltiplos artistas (ex: "Collision Course" = Jay-Z + Linkin Park)
- ✅ **Flexibilidade:** Campo "papel" para indicar função (vocalista, guitarrista)
- ✅ **Normalização:** Evita duplicação de dados

---

### 3. BIGSERIAL vs UUID

**Decisão:** Usar BIGSERIAL (IDs sequenciais).

**Justificativa:**
- ✅ **Simplicidade:** Sem necessidade de `gen_random_uuid()`
- ✅ **Performance:** Índices menores (8 bytes vs 16 bytes)
- ✅ **Legibilidade:** URLs amigáveis (`/artistas/1` vs `/artistas/a1b2c3...`)

---

### 4. MinIO para Upload de Imagens

**Por que não salvar no PostgreSQL?**

| Critério | PostgreSQL (BYTEA) | MinIO (S3) |
|----------|-------------------|------------|
| **Performance** | ❌ Lento para binários | ✅ Otimizado para streaming |
| **Escalabilidade** | ❌ Vertical | ✅ Horizontal |
| **Custo** | ❌ Storage de DB é caro | ✅ Storage S3 é barato |
| **CDN** | ❌ Difícil integração | ✅ CloudFront/CloudFlare |

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

### 5. JWT com Expiração Curta (5 min)

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

---

### 6. Rate Limiting (10 req/min por usuário)

**Implementação com Bucket4j:**
- ✅ **Justo:** Um usuário malicioso não afeta outros
- ✅ **Escalável:** Cada usuário tem seu bucket independente

---

### 7. Sincronização de Regionais - Complexidade O(n)

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

**Por que HashMap?**
- ✅ Busca O(1) vs Query O(n)
- ✅ Minimiza queries ao banco

---

### 8. WebSocket para Notificações em Tempo Real

**Fluxo:**
```
1. Usuário A cria álbum
2. Backend salva no PostgreSQL
3. Backend envia mensagem via WebSocket: /topic/albums
4. Todos os clientes conectados recebem notificação
5. Frontend exibe toast: "🎵 Novo álbum: [título]"
6. Frontend atualiza lista automaticamente
```

---

### 9. Facade Pattern no Frontend

**Benefícios:**
- ✅ **Simplificação:** Interface única para múltiplos services
- ✅ **Desacoplamento:** Componentes não conhecem detalhes
- ✅ **Manutenibilidade:** Mudanças centralizadas

---

### 10. BehaviorSubject para Gestão de Estado

**Por que BehaviorSubject?**
- ✅ **Reativo:** Componentes se inscrevem e recebem atualizações
- ✅ **Estado inicial:** Sempre tem valor
- ✅ **Multi-cast:** Múltiplos componentes observam o mesmo estado
- ✅ **Simples:** Menos boilerplate que Redux

---

## ✅ Checklist de Requisitos

### Backend (27 pontos)

#### CRUD, JWT e MinIO (0-7)
- [x] **CRUD completo** para Artistas e Álbuns
- [x] **Relacionamento N:N** (artista_album)
- [x] **JWT** com expiração de 5 minutos
- [x] **Renovação** automática de token
- [x] **Upload** de múltiplas imagens para MinIO
- [x] **Presigned URLs** com expiração de 30 minutos
- [x] **CORS** configurado para frontend

#### Paginação e Filtros (0-3)
- [x] **Paginação** em listagem de artistas
- [x] **Paginação** em listagem de álbuns
- [x] **Filtro por nome** com ordenação ASC/DESC
- [x] **Parâmetros** customizáveis (page, size, sort)

#### Rate Limit e Sincronização (0-3)
- [x] **Rate limiting** 10 req/min por usuário (Bucket4j)
- [x] **Sincronização** de regionais **O(n)**
- [x] **Detecção de mudanças** via hash MD5
- [x] **Inativação** de registros removidos (SCD Type 2)

#### Swagger, Migrations e Health Check (0-3)
- [x] **OpenAPI/Swagger** com documentação completa
- [x] **Flyway migrations** versionadas
- [x] **Seed data** com artistas do edital
- [x] **Health checks** (liveness/readiness)
- [x] **Actuator** endpoints expostos

#### WebSocket e Notificações (0-4)
- [x] **WebSocket** configurado (STOMP + SockJS)
- [x] **Notificações** ao criar álbum
- [x] **Broadcasting** para todos os clientes
- [x] **Integração** com frontend

---

### Frontend (15 pontos)

#### Consumo de API (0-5)
- [x] **CRUD** de artistas funcional
- [x] **CRUD** de álbuns funcional
- [x] **Autenticação** JWT integrada
- [x] **Upload** de imagens
- [x] **Interceptor** para renovação automática de token

#### Interface e Usabilidade (0-4)
- [x] **Layout responsivo** (Tailwind CSS)
- [x] **Navegação fluida** (React Router)
- [x] **Loading states** em operações assíncronas
- [x] **Feedback visual** (toasts, spinners)
- [x] **Notificações WebSocket** em tempo real

#### Componentização e Estado (0-3)
- [x] **Facade Pattern** implementado
- [x] **BehaviorSubject** para gerenciamento de estado
- [x] **Componentização** modular
- [x] **Custom hooks** reutilizáveis

#### Testes e Containerização (0-3)
- [x] **Testes unitários** (Jest + React Testing Library)
- [x] **Dockerfile** otimizado (multi-stage)
- [x] **Docker Compose** funcional

---

## 🧪 Testes

### Backend
```bash
cd backend
./mvnw test                    # Testes unitários
./mvnw verify                  # Testes de integração
./mvnw test jacoco:report      # Cobertura de código
```

**Cobertura esperada:** 80%+ (módulos principais).

### Frontend
```bash
cd frontend
npm test                # Testes unitários (Vitest + React Testing Library)
npm run test:coverage   # Cobertura de código
```

Testes em `src/components/Loading.test.tsx` e `src/pages/Login.test.tsx` (Vitest + RTL).

---

## 📦 Dados Pré-Carregados

O sistema já vem com os artistas do edital via Flyway Migration:

| Artista           |Álbuns |
|---------|-------- |
| **Serj Tankian**  | Harakiri, Black Blooms, The Rough Dog |
| **Mike Shinoda**  | The Rising Tied, Post Traumatic, Post Traumatic EP, Where'd You Go |
| **Michel Teló**   | Bem Sertanejo, Bem Sertanejo - O Show (Ao Vivo), Bem Sertanejo - (1ª Temporada) - EP |
| **Guns N' Roses** | Use Your Illusion I, Use Your Illusion II, Greatest Hits |

---

## 🐛 Troubleshooting

### Backend não inicia
```bash
docker-compose logs backend
docker-compose logs postgres | grep "ready to accept connections"
```

### Frontend com CORS error
```bash
docker-compose exec backend env | grep CORS_ALLOWED_ORIGINS
# Deve retornar: CORS_ALLOWED_ORIGINS=http://localhost:3000
```

### MinIO não aceita upload
```bash
docker-compose logs minio-init
docker-compose exec minio mc mb /data/album-covers
```

---

## 🚀 Melhorias Futuras

- [ ] Cache com Redis para presigned URLs
- [ ] Elastic Search para busca full-text
- [ ] Monitoring com Prometheus + Grafana
- [ ] CI/CD com GitHub Actions
- [ ] Deploy em Kubernetes

---

## 📞 Contato

**Candidato:** Francisbene Monteiro Mayer  
**Email:** fmmcba1@gmail.com  
**GitHub:** https://github.com/fmmcbaficdev

---

**Desenvolvido para o Concurso Público SEPLAG/MT - 2025**