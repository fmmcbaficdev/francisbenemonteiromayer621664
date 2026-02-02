# ✅ **CHECKLIST RÁPIDO - TESTES SWAGGER**

## 🚀 **INÍCIO RÁPIDO (5 minutos)**

### **1. Acessar Swagger**
```
http://localhost:8080/swagger-ui.html
```

### **2. Login**
```
POST /v1/auth/login
{
  "username": "admin",
  "password": "admin123"
}
```
✅ Copiar `accessToken`

### **3. Autorizar**
- Clicar em **"Authorize"** 🔓
- Inserir: `Bearer <seu-token>`
- Clicar em **"Authorize"**
- Clicar em **"Close"**

---

## 📋 **TESTES ESSENCIAIS (30 minutos)**

### **AUTENTICAÇÃO (2 endpoints)**
- [ ] `POST /v1/auth/login` → Status 200, retorna token
- [ ] `POST /v1/auth/refresh` → Status 200, renova token

### **ARTISTAS (6 endpoints)**
- [ ] `GET /v1/artistas` → Lista todos
- [ ] `GET /v1/artistas/buscar?nome=Arctic` → Busca por nome
- [ ] `GET /v1/artistas/1` → Busca por ID
- [ ] `POST /v1/artistas` → Cria novo
- [ ] `PUT /v1/artistas/{id}` → Atualiza
- [ ] `DELETE /v1/artistas/{id}` → Deleta

### **ÁLBUNS (9 endpoints)**
- [ ] `GET /v1/albuns` → Lista todos
- [ ] `GET /v1/albuns/buscar?titulo=AM` → Busca por título
- [ ] `GET /v1/albuns/artista/1` → Busca por artista
- [ ] `GET /v1/albuns/1` → Busca por ID
- [ ] `POST /v1/albuns` → Cria novo
- [ ] `POST /v1/albuns/{id}/imagens` → Upload de capa
- [ ] `PUT /v1/albuns/{id}` → Atualiza
- [ ] `DELETE /v1/albuns/{id}/imagens/{imagemId}` → Deleta imagem
- [ ] `DELETE /v1/albuns/{id}` → Deleta álbum

### **REGIONAIS (4 endpoints)**
- [ ] `GET /v1/regionais` → Lista todas
- [ ] `GET /v1/regionais/1` → Busca por ID
- [ ] `GET /v1/regionais/codigo-externo/11` → Busca por código
- [ ] `POST /v1/regionais/sincronizar` → Sincroniza (API externa)

### **USUÁRIOS (9 endpoints)**
- [ ] `GET /v1/usuarios` → Lista todos
- [ ] `GET /v1/usuarios/1` → Busca por ID
- [ ] `GET /v1/usuarios/username/admin` → Busca por username
- [ ] `POST /v1/usuarios` → Cria novo
- [ ] `PUT /v1/usuarios/{id}` → Atualiza dados
- [ ] `PATCH /v1/usuarios/{id}/senha` → Atualiza senha
- [ ] `GET /v1/usuarios/check/{username}` → Verifica username
- [ ] `DELETE /v1/usuarios/{id}` → Deleta

---

## 🧪 **TESTES DE VALIDAÇÃO**

### **Deve FALHAR (400 Bad Request):**
- [ ] Criar artista com nome vazio
- [ ] Criar álbum com ano < 1900
- [ ] Criar álbum com título vazio
- [ ] Criar usuário com username duplicado
- [ ] Senha com < 6 caracteres

### **Deve FALHAR (404 Not Found):**
- [ ] Buscar artista ID 99999
- [ ] Buscar álbum ID 99999
- [ ] Buscar regional código 99999
- [ ] Buscar usuário ID 99999

---

## 📊 **RESUMO**

**Total de endpoints:** 30  
**Tempo estimado:** 30-40 minutos  
**Cobertura:** 100%

---

## 🎯 **COMANDOS ÚTEIS**

### **Verificar containers:**
```powershell
docker compose ps
```

### **Ver logs do backend:**
```powershell
docker compose logs -f backend
```

### **Testar login via curl:**
```powershell
curl.exe -X POST http://localhost:8080/v1/auth/login -H "Content-Type: application/json" -d '{\"username\":\"admin\",\"password\":\"admin123\"}'
```

---

**Para guia completo com exemplos detalhados:**  
📖 Ver arquivo: `GUIA_COMPLETO_TESTES_SWAGGER.md`

---

**Desenvolvido para:** PSS 001/2026 SEPLAG/MT  
**Candidato:** Francisbene Monteiro Mayer
