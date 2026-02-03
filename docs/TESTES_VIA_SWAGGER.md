# Testes da API via Swagger

Este guia descreve como testar a API do backend usando o **Swagger UI**, seja a partir do navegador ou em conjunto com o frontend.

---

## Acesso ao Swagger

- **Com backend rodando localmente:** http://localhost:8080/swagger-ui.html  
- **Com Docker:** http://localhost:8080/swagger-ui.html (o frontend também pode abrir esse link pela opção **API (Swagger)** no menu.)

Documentação OpenAPI (JSON): http://localhost:8080/v3/api-docs  

---

## Passo a passo para testar endpoints protegidos

Quase todos os endpoints (exceto login e refresh) exigem **JWT**. No Swagger é preciso informar o token uma vez; depois ele é enviado em todas as requisições.

### 1. Fazer login e obter o token

1. No Swagger UI, abra a seção **Autenticação**.
2. Use o endpoint **POST /v1/auth/login**.
3. Clique em **Try it out**.
4. No corpo da requisição, use por exemplo:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

5. Clique em **Execute**.
6. Na resposta (código 200), copie o valor de **accessToken** (sem as aspas).

### 2. Autorizar no Swagger (Bearer JWT)

1. No topo da página do Swagger UI, clique em **Authorize**.
2. No campo **Bearer Authentication**, cole o token no formato:

```
Bearer <seu_accessToken_copiado>
```

Ou apenas o token (sem a palavra "Bearer"); algumas versões do Swagger já adicionam "Bearer".
3. Clique em **Authorize** e depois em **Close**.

A partir daí, todas as chamadas feitas pelo Swagger incluirão o header `Authorization: Bearer <token>`.

### 3. Testar os endpoints

| Recurso    | Endpoints principais | Observação |
|------------|----------------------|------------|
| **Artistas** | GET /v1/artistas, GET /v1/artistas/{id}, POST, PUT, DELETE | Paginação: `page`, `size`, `sort`. Busca: GET /v1/artistas/buscar?nome=... |
| **Álbuns**   | GET /v1/albuns, GET /v1/albuns/{id}, POST, PUT, DELETE | Paginação e busca por título. Upload de imagens: POST /v1/albuns/{id}/imagens (multipart) |
| **Regionais**| GET /v1/regionais, POST /v1/regionais/sincronizar | Sincronizar chama a API externa e atualiza o banco. |
| **Auth**     | POST /v1/auth/login, POST /v1/auth/refresh | Públicos; refresh usa header Authorization com refreshToken. |

- Para **GET** com paginação: use por exemplo `page=0`, `size=10`, `sort=nome,asc`.
- Para **upload de imagens** em um álbum: use **POST /v1/albuns/{id}/imagens**, tipo **multipart/form-data**, e anexe um ou mais arquivos no parâmetro **files**.

---

## Fluxo sugerido (Frontend + Swagger)

1. Subir o projeto: `docker compose up -d` (ou backend e frontend separados).
2. **Frontend:** abrir http://localhost:3001 (ou 3002), fazer login (admin / admin123) e usar a aplicação normalmente.
3. **Swagger:** abrir http://localhost:8080/swagger-ui.html (ou usar o link **API (Swagger)** no menu do frontend), fazer login no Swagger, clicar em **Authorize** e colar o token.
4. No Swagger: testar listagens, criação, edição e exclusão de artistas e álbuns, sincronização de regionais e upload de imagens.

Assim você cobre tanto o uso pela interface (frontend) quanto as chamadas diretas à API (Swagger).

---

## Resposta de erro e códigos úteis

| Código | Significado |
|--------|-------------|
| 200    | Sucesso (GET, PUT e geralmente POST). |
| 201    | Recurso criado (alguns POST). |
| 400    | Dados inválidos (validação). |
| 401    | Não autenticado ou token inválido/expirado — fazer login de novo e **Authorize** de novo. |
| 403    | Sem permissão. |
| 404    | Recurso não encontrado (ex.: id inexistente). |
| 429    | Rate limit (máx. 10 requisições/minuto por usuário). |

Se receber **401** no Swagger, refaça o login (POST /v1/auth/login), copie o novo **accessToken** e use **Authorize** novamente com esse token.

---

## Checklist rápido (avaliador)

- [ ] Acessar http://localhost:8080/swagger-ui.html
- [ ] **POST /v1/auth/login** com `{"username":"admin","password":"admin123"}` → copiar `accessToken`
- [ ] Clicar em **Authorize** → colar `Bearer <token>` → Authorize → Close
- [ ] **GET /v1/artistas** e **GET /v1/albuns** (listagem paginada)
- [ ] **POST /v1/artistas** e **POST /v1/albuns** (criar)
- [ ] **POST /v1/albuns/{id}/imagens** (upload de capa, multipart)
- [ ] **POST /v1/regionais/sincronizar** (sincronização externa)
- [ ] **POST /v1/auth/refresh** (renovação de token)
