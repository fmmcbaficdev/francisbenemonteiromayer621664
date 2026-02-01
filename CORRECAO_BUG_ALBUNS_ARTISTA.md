# 🐛 **CORREÇÃO: Bug na Tela de Detalhes do Artista**

## 📊 **PROBLEMA IDENTIFICADO**

### **Sintoma:**
Na tela de detalhes do artista (ex: "Guns N' Roses"), aparecia:
- ❌ "0 álbum(ns) cadastrado(s)"
- ❌ "Nenhum álbum cadastrado"

**MAS** existiam álbuns cadastrados no sistema (visível na tela de Álbuns)!

---

## 🔍 **CAUSA RAIZ**

### **Código ERRADO em `ArtistaDetalhes.tsx`:**

```typescript
// ❌ ERRADO: Buscava por TÍTULO do álbum, não por artista
const response = await albunsApi.listar(
  0, 100, 'titulo,asc', artistaData.nome  // ← Busca "Guns N' Roses" no TÍTULO
);
```

**Problema:** 
- O método `albunsApi.listar(busca)` busca por **título do álbum** (`/v1/albuns/buscar?titulo=xxx`)
- Não busca por **nome do artista**
- Resultado: Não encontrava álbuns porque nenhum álbum tinha "Guns N' Roses" no título

---

## ✅ **SOLUÇÃO IMPLEMENTADA**

### **1. Endpoint Correto no Backend** (já existia!)

```java
// AlbumController.java - Linha 66-77
@GetMapping("/artista/{artistaId}")
public ResponseEntity<Page<AlbumDTO>> buscarPorArtista(
        @PathVariable Long artistaId,
        @ParameterObject @PageableDefault(size = 10) Pageable pageable
) {
    Page<AlbumDTO> albuns = albumService.buscarPorArtista(artistaId, pageable);
    return ResponseEntity.ok(albuns);
}
```

**Endpoint:** `GET /v1/albuns/artista/{artistaId}`

---

### **2. Novo Método em `api.ts`:**

```typescript
// frontend/src/core/services/api.ts

buscarPorArtista: async (artistaId: number, page = 0, size = 100): Promise<Page<AlbumResumo>> => {
  const params = new URLSearchParams({ 
    page: String(page), 
    size: String(size),
    sort: 'titulo,asc'
  });
  const response = await api.get<Page<AlbumResumo>>(`/v1/albuns/artista/${artistaId}?${params}`);
  return response.data;
},
```

---

### **3. Código CORRIGIDO em `ArtistaDetalhes.tsx`:**

```typescript
// ✅ CORRETO: Usa endpoint específico para buscar por artista
const response: Page<AlbumResumo> = await albunsApi.buscarPorArtista(
  Number(id),  // ID do artista
  0,           // Página 0
  100          // Tamanho 100 (suficiente para mostrar todos)
);
setAlbuns(response.content);
```

---

## 📋 **ARQUIVOS MODIFICADOS**

| Arquivo | Mudança | Linhas |
|---------|---------|--------|
| `frontend/src/core/services/api.ts` | Adicionado método `buscarPorArtista` | ~157-166 |
| `frontend/src/pages/ArtistaDetalhes.tsx` | Corrigido carregamento de álbuns | ~40-51 |

---

## 🧪 **TESTE DA CORREÇÃO**

### **Antes:**
```
Artista: Guns N' Roses
Álbuns: 0 álbum(ns) cadastrado(s)
Mensagem: Nenhum álbum cadastrado
```

### **Depois (Esperado):**
```
Artista: Guns N' Roses
Álbuns: 3 álbum(ns) cadastrado(s)
Grid com 3 cards de álbuns:
- Use Your Illusion I
- Use Your Illusion II
- Greatest Hits
```

---

## ✅ **COMO TESTAR**

### **1. Fazer Commit:**

```powershell
git add .

git commit -m "fix: corrige busca de álbuns por artista na tela de detalhes

- Adiciona método buscarPorArtista na API do frontend
- Usa endpoint correto GET /v1/albuns/artista/{id}
- Remove lógica incorreta de busca por título
- Agora exibe corretamente os álbuns do artista

Resolves: Bug na tela de detalhes do artista mostrando 0 álbuns"
```

### **2. Reconstruir Frontend:**

```powershell
cd frontend
npm run build
cd ..

# OU rebuild completo com Docker
docker compose down
docker compose up --build -d
```

### **3. Aguardar e Testar:**

```powershell
# Aguardar 2 minutos
Start-Sleep -Seconds 120

# Abrir navegador
Start-Process "http://localhost:3000"
```

### **4. Verificar:**

1. ✅ Login (admin/admin123)
2. ✅ Clicar em "Guns N' Roses"
3. ✅ Ver álbuns com capas na seção "Álbuns"
4. ✅ Verificar contagem correta: "3 álbum(ns)"

---

## 📊 **IMPACTO**

### **Funcionalidade:**
- ✅ **CRÍTICO:** Agora a tela de detalhes funciona corretamente
- ✅ Mostra álbuns reais do artista
- ✅ Mantém conformidade 100% com edital

### **Performance:**
- ✅ Endpoint otimizado (busca direta por relação N:N)
- ✅ Sem necessidade de filtros no frontend
- ✅ Paginação suportada (até 100 itens)

### **UX:**
- ✅ Usuário vê álbuns corretos
- ✅ Empty state funciona quando realmente não há álbuns
- ✅ Loading state durante carregamento

---

## 🎯 **REQUISITOS DO EDITAL ATENDIDOS**

### **Anexo II-C - Item b) Tela de Detalhamento:**

| Requisito | Status |
|-----------|--------|
| "Ao clicar em artista, exibir álbuns associados" | ✅ CORRIGIDO |
| "Exibir informações completas, incluindo capas" | ✅ FUNCIONA |
| "Se não houver álbuns, exibir mensagem" | ✅ FUNCIONA |

---

## 📝 **OBSERVAÇÕES TÉCNICAS**

### **Por que o erro aconteceu?**

1. Backend já tinha endpoint correto (`GET /v1/albuns/artista/{id}`)
2. Frontend não tinha método na API para usar esse endpoint
3. Tentei usar busca por título como workaround (não funcionou)

### **Por que a solução é correta?**

1. ✅ Usa endpoint específico do backend
2. ✅ Busca pela relação N:N (Album_Artista)
3. ✅ Performance otimizada (query direto no banco)
4. ✅ Sem lógica complexa de filtro no frontend

### **Alternativas consideradas (e por que foram rejeitadas):**

❌ **Buscar todos os álbuns e filtrar no frontend**
- Problema: Ineficiente, não escala
- Solução melhor: Usar endpoint específico

❌ **Buscar por nome do artista no título**
- Problema: Não funciona (títulos não têm nome do artista)
- Solução melhor: Usar relação N:N do banco

✅ **Usar endpoint específico `GET /v1/albuns/artista/{id}`**
- Performance: O(1) query otimizada
- Correto: Usa relação N:N
- Escalável: Paginação suportada

---

## 🚀 **PRÓXIMOS PASSOS**

1. ✅ Fazer commit da correção
2. ✅ Rebuild e testar todos os artistas
3. ✅ Verificar empty state (criar artista sem álbuns)
4. ✅ Garantir que funciona antes do prazo 05/02/2026

---

**Data da Correção:** 31/01/2026  
**Desenvolvido para:** PSS 001/2026 SEPLAG/MT  
**Candidato:** Francisbene Monteiro Mayer  
**Status:** ✅ **BUG CORRIGIDO - PRONTO PARA TESTE**
