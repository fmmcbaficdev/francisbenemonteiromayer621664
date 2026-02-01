# Correções Críticas - Build e Runtime

## Data: 31/01/2026

## 🐛 Bugs Críticos Corrigidos

### 1. ❌ **Erro de Compilação: Nome de Classe vs Arquivo**

**Problema:**
```
ERROR: class MinIOConfig is public, should be declared in a file named MinIOConfig.java
ERROR: class MinIOService is public, should be declared in a file named MinIOService.java
```

**Causa:** Nomes de classe não correspondiam aos nomes de arquivo (Java é case-sensitive).

**Solução:**
- ✅ Renomeado `MinIOConfig` → `MinioConfig` 
- ✅ Renomeado `MinIOService` → `MinioService`
- ✅ Atualizado arquivo de teste: `MinIOServiceTest.java` → `MinioServiceTest.java`
- ✅ Atualizado todas as referências em:
  - `AlbumService.java`
  - `AlbumServiceTest.java`
  - `MinioServiceTest.java`

**Status:** ✅ **RESOLVIDO** - Build passa com sucesso

---

### 2. ❌ **Erro de Runtime: JWT Secret com Base64 Inválido**

**Problema:**
```
java.lang.IllegalArgumentException: Illegal base64 character 5f
    at br.gov.mt.seplag.backend.security.JwtService.getSignInKey(JwtService.java:126)
```

**Causa:** 
- JWT_SECRET no `.env.example` continha underscores `_` (caractere `5f` em hex)
- Underscores não são válidos em Base64 padrão
- Valor antigo: `AVALIADOR_DEMO_SECRET_SEPLAG_PSS_2026_MINIMO_32_CHARS`

**Solução:**
- ✅ Gerado novo JWT_SECRET em Base64 válido
- ✅ Novo valor: `QXZhbGlhZG9yRGVtb1NlY3JldFNlcGxhZ1BzczIwMjZNaW5pbW8zMkNoYXJzQmFzZTY0VmFsaWRv`
- ✅ Atualizado em:
  - `.env.example`
  - `.env`

**Decodifica para:** `AvaliadorDemoSecretSeplagPss2026Minimo32CharsBase64Valido`

**Base64 Válido contém apenas:** A-Z, a-z, 0-9, +, /, =

**Status:** ✅ **RESOLVIDO** - Login deve funcionar agora

---

## 📋 Checklist de Testes Pós-Correção

### Backend
- [x] Build compila sem erros
- [x] Containers sobem com status `healthy`
- [x] Flyway migrations executam
- [x] Sincronização de regionais funciona
- [ ] Login com admin/admin123 funciona ← **TESTAR AGORA**

### Frontend
- [x] Build compila
- [x] Container sobe
- [ ] Login funciona ← **TESTAR AGORA**

---

## 🚀 Como Testar

### 1. Parar e reconstruir
```bash
cd C:\seletivo\francisbenemonteiromayer621664
docker compose down
docker compose up --build -d
```

### 2. Aguardar backend inicializar (~30s)
```bash
docker compose logs -f backend
# Aguarde: "Started BackendApplication"
```

### 3. Testar login
```bash
# Via curl
curl -X POST http://localhost:8080/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Via frontend
# http://localhost:3000
# Login: admin / admin123
```

### 4. Verificar resposta esperada
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 300000
}
```

---

## 📝 Arquivos Modificados

| Arquivo | Tipo de Mudança | Motivo |
|---------|-----------------|--------|
| `backend/src/main/java/.../config/MinioConfig.java` | Renomeado classe | Corrigir erro de compilação |
| `backend/src/main/java/.../service/MinioService.java` | Renomeado classe | Corrigir erro de compilação |
| `backend/src/main/java/.../service/AlbumService.java` | Atualizar referência | Após renomeação |
| `backend/src/test/java/.../service/AlbumServiceTest.java` | Atualizar referência | Após renomeação |
| `backend/src/test/java/.../service/MinioServiceTest.java` | Renomear e atualizar | Após renomeação |
| `.env.example` | Atualizar JWT_SECRET | Corrigir erro Base64 |
| `.env` | Atualizar JWT_SECRET | Corrigir erro Base64 |

---

## ⚠️ IMPORTANTE para o Avaliador

**Após fazer `git pull` ou clonar o repositório:**

1. **SEMPRE** copie o `.env.example`:
   ```bash
   cp .env.example .env
   ```

2. **NÃO** edite o `.env` - os valores padrão funcionam perfeitamente

3. **Execute:**
   ```bash
   docker compose up --build -d
   ```

4. **Aguarde** ~1-2 minutos até todos os serviços ficarem `healthy`

5. **Acesse:** http://localhost:3000 (admin / admin123)

---

## 🎯 Resumo

- ✅ **Build:** Agora compila sem erros
- ✅ **Runtime:** JWT_SECRET corrigido para Base64 válido
- ✅ **Reproduzibilidade:** `.env.example` funciona sem edições
- ⏳ **Próximo passo:** Testar login no frontend

---

**Desenvolvido para:** Concurso Público SEPLAG/MT PSS 001/2026  
**Candidato:** Francisbene Monteiro Mayer  
**Data da Correção:** 31/01/2026 21:00 GMT-4
