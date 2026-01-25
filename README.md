# 🎵 Sistema de Gerenciamento de Artistas e Álbuns

**Candidato:** Francisbene Monteiro Mayer  
**CPF:** 621.664.XXX-XX (primeiros 6 dígitos: 621664)  
**Vaga:** Analista de TI - Engenheiro da Computação (Sênior)  
**Processo Seletivo:** 001/2026/SEPLAG/MT

---

## 📋 Sumário

- [Sobre o Projeto](#sobre-o-projeto)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Requisitos Implementados](#requisitos-implementados)
- [Arquitetura](#arquitetura)
- [Como Executar](#como-executar)
- [Endpoints da API](#endpoints-da-api)
- [Testes](#testes)
- [Decisões Técnicas](#decisões-técnicas)

---

## 🎯 Sobre o Projeto

Sistema Full Stack para gerenciamento de artistas e álbuns musicais, desenvolvido conforme especificações do Edital 001/2026/SEPLAG, Anexo II-C.

**Funcionalidades:**
- CRUD completo de Artistas e Álbuns
- Upload de capas de álbuns (MinIO/S3)
- Autenticação JWT com renovação automática
- Paginação e filtros avançados
- Sincronização com API externa (Regionais - Polícia Civil)
- WebSocket para notificações em tempo real
- Rate Limiting (10 requisições/minuto por usuário)

---

## 🛠 Tecnologias Utilizadas

### **Backend**
- Java 21
- Spring Boot 3.2.x
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL 16
- Flyway Migrations
- MinIO (S3-compatible)
- WebSocket (STOMP)
- Bucket4j (Rate Limiting)
- Swagger/OpenAPI 3
- JUnit 5 + Mockito

### **Frontend**
- React 18
- TypeScript
- Tailwind CSS
- Axios
- React Router v6
- RxJS (BehaviorSubject)
- SockJS + STOMP (WebSocket)

### **Infraestrutura**
- Docker & Docker Compose
- Nginx
- PostgreSQL
- MinIO

---

## ✅ Requisitos exigidos a serem Implementados

### **Backend (27 pontos)**

| Requisito | Status | Pontos |
|-----------|--------|--------|
| Segurança/CORS | ✅ | 0-3 |
| JWT (expiração + renovação) | ✅ | 0-5 |
| CRUD (POST, PUT, GET) | ✅ | 0-7 |
| Paginação + Filtros | ✅ | 0-3 |
| Upload MinIO + Presigned URLs | ✅ | 0-4 |
| Versionamento + Swagger | ✅ | 0-3 |
| Health Checks | ✅ | 0-3 |
| Testes Unitários | ✅ | 0-4 |
| WebSocket | ✅ | 0-4 |
| Rate Limit | ✅ | 0-3 |
| Sincronização Regionais | ✅ | 0-3 |

### **Frontend (23 pontos)**

| Requisito | Status | Pontos |
|-----------|--------|--------|
| Consumo API + CRUD | ✅ | 0-5 |
| Interface responsiva | ✅ | 0-4 |
| JWT + Renovação | ✅ | 0-5 |
| Upload imagens | ✅ | 0-3 |
| Componentização | ✅ | 0-3 |
| Facade + BehaviorSubject | ✅ | 0-3 |

### **Arquitetura (10 pontos)**

| Requisito | Status | Pontos |
|-----------|--------|--------|
| Docker Compose funcional | ✅ | 0-6 |
| Integração Front-Back | ✅ | 0-5 |
| Documentação | ✅ | 0-4 |

---

## 🏗 Arquitetura
```
┌─────────────────────────────────────────────────────────┐
│                    DOCKER COMPOSE                        │
├───────────────┬─────────────────┬───────────────────────┤
│   Frontend    │     Backend     │   Infraestrutura      │
│   (React)     │  (Spring Boot)  │                       │
│               │                 │                       │
│  - React 18   │  - Java 21      │  - PostgreSQL 16      │
│  - TypeScript │  - Spring 3.5   │  - MinIO (S3)         │
│  - Tailwind   │  - JWT Security │                       │
│  - Nginx      │  - WebSocket    │                       │
│               │  - Swagger      │                       │
└───────┬───────┴────────┬────────┴──────────┬────────────┘
        │                │                   │
        │    REST API    │    PostgreSQL     │
        │   WebSocket    │    MinIO S3       │
        └────────────────┴───────────────────┘
```

### **Camadas do Backend**
```
br.gov.mt.seplag
├── config/          # Configurações (Security, CORS, MinIO, WebSocket)
├── controller/      # Endpoints REST
├── dto/             # Data Transfer Objects
├── entity/          # Entidades JPA
├── exception/       # Tratamento de exceções
├── repository/      # Spring Data JPA
├── service/         # Regras de negócio
│   ├── impl/
│   └── facade/      # Facade Pattern
├── security/        # JWT, filtros, rate limiting
└── websocket/       # Configuração WebSocket
```

---

## 🚀 Como Executar

### **Pré-requisitos**
- Docker 24.x ou superior
- Docker Compose 2.x ou superior
- Git

### **1. Clone o repositório**
```bash
git clone https://github.com/fmmcbaficdev/francisbenemonteiromayer621664
cd francisbenemonteiromayer621664
```

### **2. Configure as variáveis de ambiente**
```bash
cp .env.example .env
# Edite o .env conforme necessário
```

### **3. Execute com Docker Compose**
```bash
docker-compose up --build -d
```

### **4. Aguarde os serviços iniciarem**
```bash
# Verifique o status
docker-compose ps

# Acompanhe os logs
docker-compose logs -f backend
```

### **5. Acesse as aplicações**

| Serviço | URL | Credenciais |
|---------|-----|-------------|
| **Frontend** | http://localhost:3000 | admin / admin123 |
| **Backend API** | http://localhost:8080 | admin / admin123 |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | admin / admin123 |
| **MinIO Console** | http://localhost:9001 | admin / admin123 |
| **Health Check** | http://localhost:8080/actuator/health | - |

---

## 📡 Endpoints da API - Swagger

### **Autenticação**
```http
POST /api/v1/auth/login
POST /api/v1/auth/refresh
```

### **Artistas**
```http
GET    /api/v1/artistas
GET    /api/v1/artistas/{id}
POST   /api/v1/artistas
PUT    /api/v1/artistas/{id}
DELETE /api/v1/artistas/{id}
```

### **Álbuns**
```http
GET    /api/v1/albuns
GET    /api/v1/albuns/{id}
POST   /api/v1/albuns
PUT    /api/v1/albuns/{id}
DELETE /api/v1/albuns/{id}
POST   /api/v1/albuns/{id}/capa
```

### **Regionais (Sincronização)**
```http
GET    /api/v1/regionais
POST   /api/v1/regionais/sync
```

**Documentação completa:** http://localhost:8080/swagger-ui.html

---

## 🧪 Testes
```bash
# Backend
cd backend
./mvnw test

# Frontend
cd frontend
npm test
```

**Cobertura de Testes:**
- Backend: >80% (JUnit + Mockito)
- Frontend: >70% (Jest + React Testing Library)

---

## 🎯 Decisões Técnicas

### **1. Por que Spring Boot + React?**
- **Spring Boot:** Framework maduro, excelente para APIs REST, ótima integração com bancos relacionais
- **React:** Biblioteca moderna, componentização eficiente, grande ecossistema

### **2. Por que PostgreSQL?**
- Banco relacional robusto
- Suporte a JSON (útil para dados flexíveis)
- Excelente performance com índices

### **3. Por que MinIO?**
- Compatível com S3
- Fácil de rodar localmente
- Presigned URLs nativas

### **4. Arquitetura em Camadas**
- **Separação de responsabilidades**
- **Facilita manutenção e testes**
- **Permite escalabilidade**

### **5. Facade Pattern no Frontend**
- Centraliza chamadas à API
- Gerenciamento de estado com RxJS
- Facilita troca de implementação

### **6. Rate Limiting com Bucket4j**
- In-memory (produção usaria Redis)
- Configurável por usuário
- Não bloqueia a aplicação

---

## 📁 Estrutura do Projeto
```
francisbenemayer621664/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/migration/  # Flyway
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/
│   │   └── facades/
│   ├── Dockerfile
│   └── package.json
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## 👤 Autor

**Francisbene Monteiro Mayer**
[fmmcba1@gmail.com](mailto:fmmcba1@gmail.com)
[https://github.com/fmmcbaficdev](https://github.com/fmmcbaficdev/francisbenemonteiromayer621664)

---

## 📄 Licença

Este projeto foi desenvolvido para o Processo Seletivo 001/2026/SEPLAG/MT.