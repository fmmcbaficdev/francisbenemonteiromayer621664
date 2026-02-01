# ==========================================
# VERIFICAÇÃO PRÉ-COMMIT
# ==========================================
# Execute ANTES de fazer commit para garantir que está tudo OK

Write-Host "╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  VERIFICAÇÃO PRÉ-COMMIT                                ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

$erros = 0

# ==========================================
# 1. VERIFICAR JWT_SECRET
# ==========================================
Write-Host "→ 1. Verificando JWT_SECRET..." -ForegroundColor Yellow

$jwt = Get-Content .env.example | Select-String "JWT_SECRET=" | Out-String

if ($jwt -match "JWT_SECRET=.*_.*") {
    Write-Host "  ✗ ERRO: JWT_SECRET contém underscore!" -ForegroundColor Red
    Write-Host "    O avaliador terá erro 500 no login!" -ForegroundColor Red
    $erros++
} elseif ($jwt -match "JWT_SECRET=QXZhbGlhZG9y") {
    Write-Host "  ✓ JWT_SECRET está correto (Base64 válido)" -ForegroundColor Green
} else {
    Write-Host "  ⚠ AVISO: JWT_SECRET diferente do esperado" -ForegroundColor Yellow
    Write-Host "    Valor: $jwt" -ForegroundColor Gray
}

# ==========================================
# 2. VERIFICAR ARQUIVOS CRÍTICOS EXISTEM
# ==========================================
Write-Host "`n→ 2. Verificando arquivos críticos..." -ForegroundColor Yellow

$arquivosCriticos = @(
    ".env.example",
    "docker-compose.yml",
    "README.md",
    "backend/Dockerfile",
    "backend/pom.xml",
    "frontend/Dockerfile",
    "frontend/package.json",
    "backend/src/main/resources/db/migration/V1__create_tables.sql",
    "backend/src/main/resources/db/migration/V2__insert_initial_data.sql",
    "backend/src/main/resources/db/migration/V3__implementa_arquitetura_dados_e_auditoria.sql"
)

foreach ($arquivo in $arquivosCriticos) {
    if (Test-Path $arquivo) {
        Write-Host "  ✓ $arquivo" -ForegroundColor Green
    } else {
        Write-Host "  ✗ FALTA: $arquivo" -ForegroundColor Red
        $erros++
    }
}

# ==========================================
# 3. VERIFICAR .gitignore NÃO BLOQUEIA CRÍTICOS
# ==========================================
Write-Host "`n→ 3. Verificando .gitignore..." -ForegroundColor Yellow

if (Test-Path ".gitignore") {
    $gitignoreContent = Get-Content .gitignore -Raw
    
    $bloqueios = @()
    if ($gitignoreContent -match "^\.env\.example$") { $bloqueios += ".env.example" }
    if ($gitignoreContent -match "^docker-compose\.yml$") { $bloqueios += "docker-compose.yml" }
    if ($gitignoreContent -match "^Dockerfile$") { $bloqueios += "Dockerfile" }
    
    if ($bloqueios.Count -gt 0) {
        Write-Host "  ✗ ERRO: .gitignore está bloqueando arquivos críticos:" -ForegroundColor Red
        $bloqueios | ForEach-Object { Write-Host "    - $_" -ForegroundColor Red }
        $erros++
    } else {
        Write-Host "  ✓ .gitignore OK (não bloqueia arquivos críticos)" -ForegroundColor Green
    }
} else {
    Write-Host "  ⚠ AVISO: .gitignore não encontrado" -ForegroundColor Yellow
}

# ==========================================
# 4. VERIFICAR STAGED FILES
# ==========================================
Write-Host "`n→ 4. Verificando arquivos staged..." -ForegroundColor Yellow

$staged = git diff --cached --name-only

if ($staged) {
    Write-Host "  Arquivos staged para commit:" -ForegroundColor Gray
    $staged | ForEach-Object { Write-Host "    - $_" -ForegroundColor Gray }
    Write-Host "  ✓ $(($staged | Measure-Object).Count) arquivo(s) pronto(s) para commit" -ForegroundColor Green
} else {
    Write-Host "  ℹ Nenhum arquivo staged (working tree limpo)" -ForegroundColor Cyan
}

# ==========================================
# 5. VERIFICAR SE .env.example ESTÁ COMMITADO
# ==========================================
Write-Host "`n→ 5. Verificando se .env.example está no Git..." -ForegroundColor Yellow

$envExampleCommitted = git ls-files | Select-String "^\.env\.example$"

if ($envExampleCommitted) {
    Write-Host "  ✓ .env.example está commitado" -ForegroundColor Green
} else {
    Write-Host "  ✗ ERRO: .env.example NÃO está commitado!" -ForegroundColor Red
    Write-Host "    Execute: git add .env.example" -ForegroundColor Yellow
    $erros++
}

# ==========================================
# 6. VERIFICAR ÚLTIMO COMMIT
# ==========================================
Write-Host "`n→ 6. Último commit..." -ForegroundColor Yellow
git log -1 --oneline

# ==========================================
# 7. VERIFICAR BRANCH
# ==========================================
Write-Host "`n→ 7. Branch atual..." -ForegroundColor Yellow
$branch = git branch --show-current
Write-Host "  Branch: $branch" -ForegroundColor Gray

if ($branch -ne "main") {
    Write-Host "  ⚠ AVISO: Não está na branch 'main'" -ForegroundColor Yellow
    Write-Host "    O avaliador clonará a branch main!" -ForegroundColor Yellow
}

# ==========================================
# 8. VERIFICAR SE TEM PUSH PENDENTE
# ==========================================
Write-Host "`n→ 8. Verificando push pendente..." -ForegroundColor Yellow

try {
    $status = git status | Out-String
    if ($status -match "Your branch is ahead") {
        Write-Host "  ⚠ AVISO: Há commits locais não enviados ao GitHub!" -ForegroundColor Yellow
        Write-Host "    Execute: git push origin main" -ForegroundColor Yellow
    } elseif ($status -match "Your branch is up to date") {
        Write-Host "  ✓ Branch está sincronizada com GitHub" -ForegroundColor Green
    } else {
        Write-Host "  ℹ Status: $(($status -split '\n')[0])" -ForegroundColor Cyan
    }
} catch {
    Write-Host "  ⚠ Não foi possível verificar status do Git" -ForegroundColor Yellow
}

# ==========================================
# RESULTADO FINAL
# ==========================================
Write-Host "`n╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan

if ($erros -eq 0) {
    Write-Host "║                                                        ║" -ForegroundColor Green
    Write-Host "║  ✅ TUDO OK! PRONTO PARA COMMIT E SUBMISSÃO            ║" -ForegroundColor Green
    Write-Host "║                                                        ║" -ForegroundColor Green
} else {
    Write-Host "║                                                        ║" -ForegroundColor Red
    Write-Host "║  ⚠️ $erros ERRO(S) ENCONTRADO(S)!                           ║" -ForegroundColor Red
    Write-Host "║                                                        ║" -ForegroundColor Red
    Write-Host "║  CORRIJA ANTES DE FAZER COMMIT!                        ║" -ForegroundColor Red
    Write-Host "║                                                        ║" -ForegroundColor Red
}

Write-Host "╚════════════════════════════════════════════════════════╝" -ForegroundColor Cyan

if ($erros -gt 0) {
    exit 1
}

Write-Host "`n📋 PRÓXIMOS PASSOS:" -ForegroundColor Cyan
Write-Host "  1. git commit -m 'mensagem'" -ForegroundColor White
Write-Host "  2. git push origin main" -ForegroundColor White
Write-Host "  3. .\teste-avaliador.ps1 (simular avaliador)" -ForegroundColor White
Write-Host ""
