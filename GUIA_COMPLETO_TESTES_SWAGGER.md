# 🧪 **GUIA COMPLETO DE TESTES VIA SWAGGER UI**

## 📋 **ÍNDICE**

1. [Acesso ao Swagger](#1-acesso-ao-swagger)
2. [Autenticação](#2-autenticação)
3. [Testes de Artistas](#3-testes-de-artistas)
4. [Testes de Álbuns](#4-testes-de-álbuns)
5. [Testes de Regionais](#5-testes-de-regionais)
6. [Testes de Usuários](#6-testes-de-usuários)
7. [Ordem Recomendada](#7-ordem-recomendada-de-testes)
8. [Troubleshooting](#8-troubleshooting)

---

## 1. ACESSO AO SWAGGER

### **1.1 URL:**
```
http://localhost:8080/swagger-ui.html
```

### **1.2 Verificar que o backend está rodando:**

```powershell
# Verificar containers
docker compose ps

# Verificar logs do backend
docker compose logs -f backend
```

**Status esperado:**
- ✅ Backend: `Up X minutes (healthy)`
- ✅ Logs mostram: `Started BackendApplication in X seconds`

---

## 2. AUTENTICAÇÃO

### **2.1 Obter Token JWT**

#### **Endpoint:** `POST /v1/auth/login`

**Passo a passo:**
1. Abrir Swagger: http://localhost:8080/swagger-ui.html
2. Expandir seção **"Autenticação"**
3. Clicar em **"POST /v1/auth/login"**
4. Clicar em **"Try it out"**
5. Substituir o JSON de exemplo:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

6. Clicar em **"Execute"**

**Resultado esperado (200 OK):**

```json
{
  "accessToken": "eyJhbGciOiJIUzM4NCJ9.eyJ0eXBlIjoiQUNDRVNTI...",
  "refreshToken": "eyJhbGciOiJIUzM4NCJ9.eyJ0eXBlIjoiUkVGUkVTSC...",
  "tokenType": "Bearer",
  "expiresIn": 300000
}
```

✅ **Copie o `accessToken` completo** (incluindo `eyJ...`)

---

### **2.2 Configurar Token no Swagger**

1. No topo da página do Swagger, clicar no botão **"Authorize"** 🔓
2. Na janela que abrir, colar o token no campo **"Value"**:
   ```
   Bearer eyJhbGciOiJIUzM4NCJ9.eyJ0eXBlIjoiQUNDRVNTI...
   ```
   **⚠️ IMPORTANTE:** Incluir a palavra `Bearer` + espaço + token

3. Clicar em **"Authorize"**
4. Clicar em **"Close"**

✅ **Agora você está autenticado!** O cadeado 🔓 muda para 🔒

---

### **2.3 Refresh Token (OPCIONAL)**

#### **Endpoint:** `POST /v1/auth/refresh`

**Quando usar:** Quando o `accessToken` expirar (5 minutos)

**Passo a passo:**
1. Expandir **"POST /v1/auth/refresh"**
2. Clicar em **"Try it out"**
3. No campo **"Authorization"**, inserir:
   ```
   Bearer <seu-refreshToken>
   ```
4. Clicar em **"Execute"**

**Resultado esperado (200 OK):**
```json
{
  "accessToken": "novo-token-aqui...",
  "refreshToken": "mesmo-refresh-token...",
  "tokenType": "Bearer",
  "expiresIn": 300000
}
```

✅ Usar o novo `accessToken` para continuar os testes

---

## 3. TESTES DE ARTISTAS

### **3.1 Listar Artistas**

#### **Endpoint:** `GET /v1/artistas`

**Parâmetros opcionais:**
- `page`: número da página (default: 0)
- `size`: itens por página (default: 10)
- `sort`: campo de ordenação (ex: `nome,asc` ou `nome,desc`)

**Teste 1: Listar primeira página**
```
page: 0
size: 10
sort: nome,asc
```

**Resultado esperado (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "nome": "Arctic Monkeys",
      "biografia": "Banda britânica de rock...",
      "numeroAlbuns": 2,
      "criadoEm": "2024-01-31T12:00:00",
      "atualizadoEm": "2024-01-31T12:00:00"
    }
  ],
  "pageable": {...},
  "totalElements": 5,
  "totalPages": 1,
  "number": 0
}
```

---

### **3.2 Buscar Artista por Nome**

#### **Endpoint:** `GET /v1/artistas/buscar`

**Parâmetros:**
- `nome`: termo de busca (case-insensitive)

**Teste 1: Buscar "Arctic"**
```
nome: Arctic
page: 0
size: 10
```

**Resultado esperado (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "nome": "Arctic Monkeys",
      ...
    }
  ],
  "totalElements": 1
}
```

**Teste 2: Buscar artista que não existe**
```
nome: XYZABC123
```

**Resultado esperado (200 OK):**
```json
{
  "content": [],
  "totalElements": 0
}
```

---

### **3.3 Buscar Artista por ID**

#### **Endpoint:** `GET /v1/artistas/{id}`

**Teste 1: Buscar ID existente**
```
id: 1
```

**Resultado esperado (200 OK):**
```json
{
  "id": 1,
  "nome": "Arctic Monkeys",
  "biografia": "Banda britânica...",
  "numeroAlbuns": 2
}
```

**Teste 2: Buscar ID inexistente**
```
id: 99999
```

**Resultado esperado (404 Not Found):**
```json
{
  "timestamp": "2024-01-31T20:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Artista não encontrado com ID: 99999"
}
```

---

### **3.4 Criar Artista**

#### **Endpoint:** `POST /v1/artistas`

**Body (JSON):**
```json
{
  "nome": "Radiohead",
  "biografia": "Banda britânica de rock alternativo formada em 1985 em Oxford. Conhecida por álbuns inovadores como OK Computer e Kid A."
}
```

**Resultado esperado (201 Created):**
```json
{
  "id": 6,
  "nome": "Radiohead",
  "biografia": "Banda britânica de rock alternativo...",
  "numeroAlbuns": 0,
  "criadoEm": "2024-01-31T20:00:00",
  "atualizadoEm": "2024-01-31T20:00:00"
}
```

**Teste 2: Criar com nome vazio (validação)**
```json
{
  "nome": "",
  "biografia": "Teste"
}
```

**Resultado esperado (400 Bad Request):**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Nome é obrigatório"
}
```

---

### **3.5 Atualizar Artista**

#### **Endpoint:** `PUT /v1/artistas/{id}`

**Teste: Atualizar biografia**
```
id: 6
```

**Body:**
```json
{
  "nome": "Radiohead",
  "biografia": "Banda britânica de rock alternativo formada em 1985. Vencedores de 3 Grammys e conhecidos por sua inovação sonora."
}
```

**Resultado esperado (200 OK):**
```json
{
  "id": 6,
  "nome": "Radiohead",
  "biografia": "Banda britânica de rock alternativo formada em 1985. Vencedores de 3 Grammys...",
  "numeroAlbuns": 0,
  "atualizadoEm": "2024-01-31T20:05:00"
}
```

---

### **3.6 Deletar Artista**

#### **Endpoint:** `DELETE /v1/artistas/{id}`

**Teste 1: Deletar artista sem álbuns**
```
id: 6
```

**Resultado esperado (204 No Content):**
- Sem body
- Status: 204

**Verificação:**
- Buscar `GET /v1/artistas/6` deve retornar 404

**Teste 2: Deletar artista com álbuns**
```
id: 1
```

**Resultado esperado (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Não é possível deletar artista com álbuns associados"
}
```

---

## 4. TESTES DE ÁLBUNS

### **4.1 Listar Álbuns**

#### **Endpoint:** `GET /v1/albuns`

**Parâmetros opcionais:**
- `page`: 0
- `size`: 10
- `sort`: `titulo,asc`

**Resultado esperado (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "titulo": "AM",
      "anoLancamento": 2013,
      "genero": "Rock",
      "artistas": [
        {
          "id": 1,
          "nome": "Arctic Monkeys"
        }
      ],
      "imagensCapa": [
        {
          "id": 1,
          "nomeArquivo": "am-cover.jpg",
          "url": "http://localhost:9000/seplag-bucket/am-cover.jpg?X-Amz-Algorithm=...",
          "principal": true
        }
      ],
      "numeroArtistas": 1
    }
  ],
  "totalElements": 8
}
```

---

### **4.2 Buscar Álbum por Título**

#### **Endpoint:** `GET /v1/albuns/buscar`

**Parâmetros:**
```
titulo: AM
page: 0
size: 10
```

**Resultado esperado (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "titulo": "AM",
      "anoLancamento": 2013,
      "genero": "Rock"
    }
  ],
  "totalElements": 1
}
```

---

### **4.3 Buscar Álbuns por Artista**

#### **Endpoint:** `GET /v1/albuns/artista/{artistaId}`

**Teste: Buscar álbuns do artista ID 1**
```
artistaId: 1
page: 0
size: 10
```

**Resultado esperado (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "titulo": "AM",
      "artistas": [
        {
          "id": 1,
          "nome": "Arctic Monkeys"
        }
      ]
    },
    {
      "id": 2,
      "titulo": "Favourite Worst Nightmare",
      "artistas": [
        {
          "id": 1,
          "nome": "Arctic Monkeys"
        }
      ]
    }
  ],
  "totalElements": 2
}
```

---

### **4.4 Buscar Álbum por ID**

#### **Endpoint:** `GET /v1/albuns/{id}`

**Teste:**
```
id: 1
```

**Resultado esperado (200 OK):**
```json
{
  "id": 1,
  "titulo": "AM",
  "anoLancamento": 2013,
  "genero": "Rock",
  "artistas": [
    {
      "id": 1,
      "nome": "Arctic Monkeys"
    }
  ],
  "imagensCapa": [
    {
      "id": 1,
      "nomeArquivo": "am-cover.jpg",
      "url": "http://localhost:9000/seplag-bucket/...",
      "principal": true
    }
  ]
}
```

---

### **4.5 Criar Álbum**

#### **Endpoint:** `POST /v1/albuns`

**Body:**
```json
{
  "titulo": "OK Computer",
  "anoLancamento": 1997,
  "genero": "Rock Alternativo",
  "artistasIds": [1]
}
```

**Resultado esperado (201 Created):**
```json
{
  "id": 9,
  "titulo": "OK Computer",
  "anoLancamento": 1997,
  "genero": "Rock Alternativo",
  "artistas": [
    {
      "id": 1,
      "nome": "Arctic Monkeys"
    }
  ],
  "imagensCapa": [],
  "numeroArtistas": 1,
  "criadoEm": "2024-01-31T20:10:00"
}
```

**Teste 2: Criar com múltiplos artistas**
```json
{
  "titulo": "Collaboration Album",
  "anoLancamento": 2024,
  "genero": "Pop",
  "artistasIds": [1, 2, 3]
}
```

**Resultado esperado (201 Created):**
```json
{
  "id": 10,
  "titulo": "Collaboration Album",
  "artistas": [
    {"id": 1, "nome": "Arctic Monkeys"},
    {"id": 2, "nome": "Guns N' Roses"},
    {"id": 3, "nome": "Metallica"}
  ],
  "numeroArtistas": 3
}
```

**Teste 3: Validação - título vazio**
```json
{
  "titulo": "",
  "anoLancamento": 2024,
  "genero": "Rock",
  "artistasIds": [1]
}
```

**Resultado esperado (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Título é obrigatório"
}
```

**Teste 4: Validação - ano inválido**
```json
{
  "titulo": "Teste",
  "anoLancamento": 1800,
  "genero": "Rock",
  "artistasIds": [1]
}
```

**Resultado esperado (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Ano de lançamento deve ser entre 1900 e ano atual"
}
```

---

### **4.6 Atualizar Álbum**

#### **Endpoint:** `PUT /v1/albuns/{id}`

**Teste: Atualizar gênero e artistas**
```
id: 9
```

**Body:**
```json
{
  "titulo": "OK Computer",
  "anoLancamento": 1997,
  "genero": "Experimental Rock",
  "artistasIds": [1, 2]
}
```

**Resultado esperado (200 OK):**
```json
{
  "id": 9,
  "titulo": "OK Computer",
  "anoLancamento": 1997,
  "genero": "Experimental Rock",
  "artistas": [
    {"id": 1, "nome": "Arctic Monkeys"},
    {"id": 2, "nome": "Guns N' Roses"}
  ],
  "numeroArtistas": 2,
  "atualizadoEm": "2024-01-31T20:15:00"
}
```

---

### **4.7 Upload de Imagens de Capa**

#### **Endpoint:** `POST /v1/albuns/{albumId}/imagens`

**Parâmetros:**
```
albumId: 9
```

**⚠️ IMPORTANTE:** Este endpoint requer **multipart/form-data**

**No Swagger:**
1. Clicar em **"Try it out"**
2. Inserir `albumId: 9`
3. Clicar em **"Choose File"** no campo `files`
4. Selecionar uma ou mais imagens (JPG, PNG, WEBP)
5. Clicar em **"Execute"**

**Resultado esperado (201 Created):**
```json
{
  "albumId": 9,
  "quantidadeUpload": 1,
  "imagens": [
    {
      "id": 10,
      "nomeArquivo": "ok-computer-cover.jpg",
      "nomeOriginal": "ok-computer-cover.jpg",
      "tamanho": 245678,
      "contentType": "image/jpeg",
      "url": "http://localhost:9000/seplag-bucket/ok-computer-cover.jpg?X-Amz-Algorithm=...",
      "urlExpiraEm": "2024-01-31T20:45:00",
      "principal": true
    }
  ],
  "mensagem": "1 imagem(ns) enviada(s) com sucesso"
}
```

**Verificação:**
- Acessar a URL da imagem no navegador
- Deve exibir a imagem
- URL válida por 30 minutos (presigned URL)

**Teste 2: Upload de múltiplas imagens**
- Selecionar 3 arquivos de imagem
- `quantidadeUpload: 3`
- Primeira imagem será `principal: true`
- Demais serão `principal: false`

---

### **4.8 Deletar Imagem de Capa**

#### **Endpoint:** `DELETE /v1/albuns/{albumId}/imagens/{imagemId}`

**Teste:**
```
albumId: 9
imagemId: 10
```

**Resultado esperado (204 No Content):**
- Sem body
- Status: 204
- Arquivo removido do MinIO
- Registro removido do banco

**Verificação:**
```
GET /v1/albuns/9
```
- `imagensCapa` deve estar vazio `[]`

---

### **4.9 Deletar Álbum**

#### **Endpoint:** `DELETE /v1/albuns/{id}`

**Teste:**
```
id: 9
```

**Resultado esperado (204 No Content):**
- Sem body
- Status: 204
- Álbum deletado
- Imagens de capa deletadas (cascade)

**Verificação:**
```
GET /v1/albuns/9
```
- Deve retornar 404 Not Found

---

## 5. TESTES DE REGIONAIS

### **5.1 Listar Regionais**

#### **Endpoint:** `GET /v1/regionais`

**Parâmetros opcionais:**
- `page`: 0
- `size`: 10
- `sort`: `nome,asc`
- `ativa`: `true` ou `false` (filtro)

**Teste 1: Listar todas (sem filtro)**
```
page: 0
size: 10
sort: nome,asc
```

**Resultado esperado (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "codigoExterno": 11,
      "nome": "REGIONAL DE AGUA BOA",
      "endereco": "Rua...",
      "telefone": "(66) 1234-5678",
      "email": "aguaboa@pc.mt.gov.br",
      "ativa": true,
      "dataImportacao": "2024-01-31T20:00:00",
      "checksum": "c9304d4f..."
    }
  ],
  "totalElements": 33
}
```

**Teste 2: Filtrar apenas ativas**
```
ativa: true
```

**Resultado esperado:**
- Apenas regionais com `ativa: true`

**Teste 3: Filtrar inativas**
```
ativa: false
```

**Resultado esperado:**
- Apenas regionais com `ativa: false` (se houver)

---

### **5.2 Buscar Regional por ID**

#### **Endpoint:** `GET /v1/regionais/{id}`

**Teste:**
```
id: 1
```

**Resultado esperado (200 OK):**
```json
{
  "id": 1,
  "codigoExterno": 11,
  "nome": "REGIONAL DE AGUA BOA",
  "endereco": "Rua Principal, 123",
  "telefone": "(66) 1234-5678",
  "email": "aguaboa@pc.mt.gov.br",
  "ativa": true,
  "dataImportacao": "2024-01-31T20:00:00",
  "checksum": "c9304d4f..."
}
```

---

### **5.3 Buscar Regional por Código Externo**

#### **Endpoint:** `GET /v1/regionais/codigo-externo/{codigoExterno}`

**Teste:**
```
codigoExterno: 11
```

**Resultado esperado (200 OK):**
```json
{
  "id": 1,
  "codigoExterno": 11,
  "nome": "REGIONAL DE AGUA BOA",
  ...
}
```

**Teste 2: Código inexistente**
```
codigoExterno: 99999
```

**Resultado esperado (404 Not Found):**
```json
{
  "status": 404,
  "message": "Regional não encontrada com código externo: 99999"
}
```

---

### **5.4 Sincronizar Regionais (Manual)**

#### **Endpoint:** `POST /v1/regionais/sincronizar`

**⚠️ IMPORTANTE:** 
- Conecta-se à API externa: `https://integrador-argus-api.geia.vip/v1/regionais`
- Algoritmo O(n) com MD5 hash
- Pode levar 1-2 segundos

**Teste:**
1. Clicar em **"Try it out"**
2. Clicar em **"Execute"**
3. Aguardar resposta

**Resultado esperado (200 OK):**
```json
{
  "status": "success",
  "criadas": 0,
  "atualizadas": 0,
  "desativadas": 0,
  "semMudancas": 33,
  "totalApi": 33,
  "tempoMs": 1004,
  "algoritmo": "O(n)",
  "complexidade": "linear",
  "mensagem": "Sincronização concluída com sucesso"
}
```

**Interpretação:**
- `criadas`: Novas regionais da API
- `atualizadas`: Regionais modificadas (MD5 diferente)
- `desativadas`: Regionais que sumiram da API
- `semMudancas`: Regionais sem alteração
- `totalApi`: Total de regionais na API
- `tempoMs`: Tempo de execução em milissegundos

**Teste 2: Verificar logs do backend**
```powershell
docker compose logs -f backend
```

**Logs esperados:**
```
╔════════════════════════════════════════════════════════╗
║  INICIANDO SINCRONIZAÇÃO O(n) COM MD5 HASH            ║
╚════════════════════════════════════════════════════════╝
→ PASSO 1: Buscando regionais do banco de dados...
  ✓ Regionais no banco: 33
→ PASSO 2: Criando HashMap para lookup O(1)...
  ✓ HashMap criado com 33 entradas
→ PASSO 3: Buscando regionais da API externa...
  ✓ 33 regionais recebidas da API
→ PASSO 4: Processando regionais da API com MD5 hash...
→ PASSO 5: Verificando regionais removidas da API...
╔════════════════════════════════════════════════════════╗
║  SINCRONIZAÇÃO CONCLUÍDA (COM MD5 HASH)                ║
║  ✓ Criadas: 0                                          ║
║  ↻ Atualizadas: 0                                      ║
║  ✗ Desativadas: 0                                      ║
║  = Sem mudanças: 33                                    ║
║  ⏱ Tempo: 1004ms                                       ║
║  ⚡ Complexidade: O(n) onde n = 33                     ║
╚════════════════════════════════════════════════════════╝
```

---

## 6. TESTES DE USUÁRIOS

### **6.1 Listar Usuários**

#### **Endpoint:** `GET /v1/usuarios`

**Teste:**
- Sem parâmetros (lista todos)

**Resultado esperado (200 OK):**
```json
[
  {
    "id": 1,
    "username": "admin",
    "nome": "Administrador",
    "email": "admin@seplag.mt.gov.br",
    "role": "ADMIN",
    "ativo": true,
    "criadoEm": "2024-01-31T12:00:00",
    "atualizadoEm": "2024-01-31T12:00:00"
  },
  {
    "id": 2,
    "username": "user",
    "nome": "Usuário Comum",
    "email": "user@seplag.mt.gov.br",
    "role": "USER",
    "ativo": true
  }
]
```

**⚠️ NOTA:** Senha nunca é retornada nos endpoints GET

---

### **6.2 Buscar Usuário por ID**

#### **Endpoint:** `GET /v1/usuarios/{id}`

**Teste:**
```
id: 1
```

**Resultado esperado (200 OK):**
```json
{
  "id": 1,
  "username": "admin",
  "nome": "Administrador",
  "email": "admin@seplag.mt.gov.br",
  "role": "ADMIN",
  "ativo": true
}
```

---

### **6.3 Buscar Usuário por Username**

#### **Endpoint:** `GET /v1/usuarios/username/{username}`

**Teste:**
```
username: admin
```

**Resultado esperado (200 OK):**
```json
{
  "id": 1,
  "username": "admin",
  "nome": "Administrador",
  ...
}
```

---

### **6.4 Criar Usuário**

#### **Endpoint:** `POST /v1/usuarios`

**Body:**
```json
{
  "username": "avaliador",
  "password": "senha123",
  "nome": "Avaliador SEPLAG",
  "email": "avaliador@seplag.mt.gov.br",
  "role": "ADMIN",
  "ativo": true
}
```

**Resultado esperado (201 Created):**
```json
{
  "id": 3,
  "username": "avaliador",
  "nome": "Avaliador SEPLAG",
  "email": "avaliador@seplag.mt.gov.br",
  "role": "ADMIN",
  "ativo": true,
  "criadoEm": "2024-01-31T20:20:00"
}
```

**⚠️ NOTA:** Senha não é retornada (segurança)

**Teste 2: Validação - username duplicado**
```json
{
  "username": "admin",
  "password": "senha123",
  "nome": "Teste",
  "email": "teste@email.com",
  "role": "USER",
  "ativo": true
}
```

**Resultado esperado (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Username já existe: admin"
}
```

**Teste 3: Validação - senha ausente**
```json
{
  "username": "teste",
  "nome": "Teste",
  "email": "teste@email.com",
  "role": "USER",
  "ativo": true
}
```

**Resultado esperado (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Senha é obrigatória para criar usuário"
}
```

---

### **6.5 Atualizar Usuário**

#### **Endpoint:** `PUT /v1/usuarios/{id}`

**⚠️ NOTA:** NÃO atualiza senha (usar PATCH para senha)

**Teste: Atualizar nome e email**
```
id: 3
```

**Body:**
```json
{
  "username": "avaliador",
  "nome": "Avaliador SEPLAG MT",
  "email": "avaliador.seplag@mt.gov.br",
  "role": "ADMIN",
  "ativo": true
}
```

**Resultado esperado (200 OK):**
```json
{
  "id": 3,
  "username": "avaliador",
  "nome": "Avaliador SEPLAG MT",
  "email": "avaliador.seplag@mt.gov.br",
  "role": "ADMIN",
  "ativo": true,
  "atualizadoEm": "2024-01-31T20:25:00"
}
```

---

### **6.6 Atualizar Senha**

#### **Endpoint:** `PATCH /v1/usuarios/{id}/senha`

**Teste:**
```
id: 3
```

**Body:**
```json
{
  "novaSenha": "novaSenha123"
}
```

**Resultado esperado (200 OK):**
```json
{
  "mensagem": "Senha atualizada com sucesso"
}
```

**Verificação:**
- Fazer logout (se estiver logado como usuário ID 3)
- Fazer login novamente:
  ```json
  {
    "username": "avaliador",
    "password": "novaSenha123"
  }
  ```
- Deve retornar token JWT

**Teste 2: Validação - senha muito curta**
```json
{
  "novaSenha": "123"
}
```

**Resultado esperado (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Senha deve ter no mínimo 6 caracteres"
}
```

---

### **6.7 Deletar Usuário**

#### **Endpoint:** `DELETE /v1/usuarios/{id}`

**Teste:**
```
id: 3
```

**Resultado esperado (204 No Content):**
- Sem body
- Status: 204

**Verificação:**
```
GET /v1/usuarios/3
```
- Deve retornar 404 Not Found

---

### **6.8 Verificar se Username Existe**

#### **Endpoint:** `GET /v1/usuarios/check/{username}`

**Teste 1: Username existente**
```
username: admin
```

**Resultado esperado (200 OK):**
```json
{
  "existe": true
}
```

**Teste 2: Username disponível**
```
username: novouser123
```

**Resultado esperado (200 OK):**
```json
{
  "existe": false
}
```

---

## 7. ORDEM RECOMENDADA DE TESTES

### **7.1 Fluxo Completo (30-40 minutos)**

#### **FASE 1: Autenticação (5 min)**
1. ✅ `POST /v1/auth/login` - Obter token
2. ✅ Configurar token no Swagger (Authorize)
3. ✅ `POST /v1/auth/refresh` - Testar renovação

#### **FASE 2: Artistas (10 min)**
4. ✅ `GET /v1/artistas` - Listar todos
5. ✅ `GET /v1/artistas/buscar?nome=Arctic` - Buscar por nome
6. ✅ `GET /v1/artistas/1` - Buscar por ID
7. ✅ `POST /v1/artistas` - Criar novo (Radiohead)
8. ✅ `PUT /v1/artistas/6` - Atualizar biografia
9. ✅ `DELETE /v1/artistas/6` - Deletar

#### **FASE 3: Álbuns (15 min)**
10. ✅ `GET /v1/albuns` - Listar todos
11. ✅ `GET /v1/albuns/buscar?titulo=AM` - Buscar por título
12. ✅ `GET /v1/albuns/artista/1` - Buscar por artista
13. ✅ `GET /v1/albuns/1` - Buscar por ID
14. ✅ `POST /v1/albuns` - Criar novo (OK Computer)
15. ✅ `POST /v1/albuns/9/imagens` - Upload de capa
16. ✅ `PUT /v1/albuns/9` - Atualizar dados
17. ✅ `DELETE /v1/albuns/9/imagens/10` - Deletar imagem
18. ✅ `DELETE /v1/albuns/9` - Deletar álbum

#### **FASE 4: Regionais (5 min)**
19. ✅ `GET /v1/regionais` - Listar todas
20. ✅ `GET /v1/regionais/1` - Buscar por ID
21. ✅ `GET /v1/regionais/codigo-externo/11` - Buscar por código
22. ✅ `POST /v1/regionais/sincronizar` - Sincronização manual

#### **FASE 5: Usuários (10 min)**
23. ✅ `GET /v1/usuarios` - Listar todos
24. ✅ `GET /v1/usuarios/1` - Buscar por ID
25. ✅ `GET /v1/usuarios/username/admin` - Buscar por username
26. ✅ `POST /v1/usuarios` - Criar novo (avaliador)
27. ✅ `PUT /v1/usuarios/3` - Atualizar dados
28. ✅ `PATCH /v1/usuarios/3/senha` - Atualizar senha
29. ✅ `GET /v1/usuarios/check/admin` - Verificar username
30. ✅ `DELETE /v1/usuarios/3` - Deletar usuário

---

### **7.2 Testes de Validação (10 min)**

**Teste validações:**
- ❌ Criar artista com nome vazio
- ❌ Criar álbum com ano inválido (1800)
- ❌ Criar álbum com título vazio
- ❌ Criar usuário com username duplicado
- ❌ Atualizar senha muito curta (< 6 chars)
- ❌ Buscar IDs inexistentes (404)
- ❌ Deletar artista com álbuns (400)

---

### **7.3 Testes de Paginação (5 min)**

**Teste paginação:**
```
GET /v1/artistas?page=0&size=2
GET /v1/artistas?page=1&size=2
GET /v1/artistas?page=2&size=2
```

**Verificar:**
- `totalElements` é consistente
- `totalPages` calculado corretamente
- `number` corresponde à página
- `content` tem no máximo `size` itens

---

### **7.4 Testes de Ordenação (5 min)**

**Teste ordenação:**
```
GET /v1/artistas?sort=nome,asc
GET /v1/artistas?sort=nome,desc
GET /v1/albuns?sort=titulo,asc
GET /v1/albuns?sort=anoLancamento,desc
```

**Verificar:**
- Itens ordenados corretamente
- ASC = crescente (A-Z, 0-9)
- DESC = decrescente (Z-A, 9-0)

---

## 8. TROUBLESHOOTING

### **8.1 Erro 401 Unauthorized**

**Problema:** Token JWT expirou ou inválido

**Solução:**
1. Fazer novo login: `POST /v1/auth/login`
2. Copiar novo `accessToken`
3. Atualizar no Swagger: **Authorize** → `Bearer <novo-token>`

---

### **8.2 Erro 403 Forbidden**

**Problema:** Endpoint requer role específica (ADMIN)

**Solução:**
- Fazer login com usuário ADMIN: `admin` / `admin123`
- Verificar se o token foi configurado corretamente

---

### **8.3 Erro 404 Not Found**

**Problema:** Recurso não existe

**Soluções:**
- Verificar ID correto
- Listar recursos primeiro para obter IDs válidos
- Verificar se o recurso não foi deletado

---

### **8.4 Erro 400 Bad Request**

**Problema:** Validação falhou

**Soluções:**
- Verificar campos obrigatórios
- Verificar formato dos dados (JSON válido)
- Verificar constraints (ex: ano entre 1900 e atual)
- Ler mensagem de erro para detalhes

---

### **8.5 Erro 500 Internal Server Error**

**Problema:** Erro no servidor

**Solução:**
1. Verificar logs do backend:
   ```powershell
   docker compose logs -f backend
   ```
2. Procurar por stack traces
3. Verificar se banco de dados está acessível
4. Verificar se MinIO está acessível

---

### **8.6 Upload de Imagem Falha**

**Problema:** Erro ao fazer upload via Swagger

**Soluções:**
1. Verificar que MinIO está rodando:
   ```powershell
   docker compose ps minio
   ```
2. Verificar formato da imagem (JPG, PNG, WEBP)
3. Verificar tamanho da imagem (< 10MB)
4. Verificar logs do backend:
   ```powershell
   docker compose logs -f backend | Select-String "MinIO\|Upload"
   ```

---

### **8.7 Sincronização de Regionais Falha**

**Problema:** Erro ao sincronizar com API externa

**Soluções:**
1. Verificar conectividade com a internet
2. Verificar se a API externa está online:
   ```powershell
   curl.exe https://integrador-argus-api.geia.vip/v1/regionais
   ```
3. Verificar logs do backend:
   ```powershell
   docker compose logs -f backend | Select-String "Regional\|Sync"
   ```

---

## 📊 **RESUMO DE ENDPOINTS**

| Categoria | Endpoints | Total |
|-----------|-----------|-------|
| **Autenticação** | Login, Refresh | 2 |
| **Artistas** | CRUD + Buscar | 6 |
| **Álbuns** | CRUD + Upload + Buscar | 9 |
| **Regionais** | Listar + Buscar + Sincronizar | 4 |
| **Usuários** | CRUD + Senha + Check | 9 |
| **TOTAL** | | **30** |

---

## 📝 **CHECKLIST DE TESTES**

### **Funcionalidades Core:**
- [x] Autenticação JWT funciona
- [x] CRUD de Artistas funciona
- [x] CRUD de Álbuns funciona
- [x] Upload de imagens funciona (MinIO)
- [x] Relacionamento N:N (Artistas ↔ Álbuns)
- [x] Sincronização de Regionais funciona (API externa)
- [x] Paginação funciona
- [x] Ordenação funciona
- [x] Busca/filtros funcionam

### **Validações:**
- [x] Campos obrigatórios validados
- [x] Constraints validadas (ex: ano lançamento)
- [x] Username duplicado bloqueado
- [x] Senha mínima 6 caracteres

### **Segurança:**
- [x] Endpoints protegidos por JWT
- [x] Senha nunca retornada em GET
- [x] Token expira em 5 minutos
- [x] Refresh token funciona

### **Performance:**
- [x] Sincronização O(n) com MD5
- [x] Paginação evita sobrecarga
- [x] Presigned URLs para imagens

---

## 🎯 **CONCLUSÃO**

Este guia cobre **TODOS os 30 endpoints** disponíveis na API, incluindo:

✅ Testes de sucesso (200, 201, 204)  
✅ Testes de validação (400)  
✅ Testes de não encontrado (404)  
✅ Testes de autenticação (401, 403)  
✅ Testes de paginação e ordenação  
✅ Upload de arquivos (multipart/form-data)  
✅ Integração com API externa  

**Tempo estimado para testar todos:** 60-90 minutos

---

**Desenvolvido para:** PSS 001/2026 SEPLAG/MT  
**Candidato:** Francisbene Monteiro Mayer  
**Data:** 31/01/2026
