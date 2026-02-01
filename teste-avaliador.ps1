# ==========================================
# SCRIPT DE TESTE - SIMULAR AVALIADOR
# ==========================================
# Este script simula exatamente o que o avaliador vai fazer
# Execute em PowerShell COMO ADMINISTRADOR

Write-Host "╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  TESTE DE REPRODUTIBILIDADE - SIMULANDO AVALIADOR     ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

$ErrorActionPreference = "Stop"
$testeDir = "C:\temp\teste-avaliador-seplag"

# ==========================================
# PASSO 1: LIMPAR AMBIENTE
# ==========================================
Write-Host "→ PASSO 1: Limpando ambiente de teste..." -ForegroundColor Yellow

if (Test-Path $testeDir) {
    Write-Host "  Removendo diretório anterior..." -ForegroundColor Gray
    Remove-Item -Path $testeDir -Recurse -Force
}

# ==========================================
# PASSO 2: CLONAR REPOSITÓRIO
# ==========================================
Write-Host "`n→ PASSO 2: Clonando repositório (como avaliador)..." -ForegroundColor Yellow
Write-Host "  git clone https://github.com/fmmcbaficdev/francisbenemonteiromayer621664" -ForegroundColor Gray

try {
    git clone https://github.com/fmmcbaficdev/francisbenemonteiromayer621664 $testeDir
    Write-Host "  ✓ Repositório clonado com sucesso!" -ForegroundColor Green
} catch {
    Write-Host "  ✗ ERRO ao clonar repositório!" -ForegroundColor Red
    Write-Host "    Verifique se o push foi feito: git push origin main" -ForegroundColor Yellow
    exit 1
}

Set-Location $testeDir

# ==========================================
# PASSO 3: CONFIGURAR .env
# ==========================================
Write-Host "`n→ PASSO 3: Configurando .env (cp .env.example .env)..." -ForegroundColor Yellow

if (!(Test-Path ".env.example")) {
    Write-Host "  ✗ ERRO: .env.example não encontrado!" -ForegroundColor Red
    exit 1
}

Copy-Item ".env.example" ".env" -Force
Write-Host "  ✓ .env criado!" -ForegroundColor Green

# Verificar JWT_SECRET
$jwtSecret = Get-Content .env | Select-String "JWT_SECRET=" | Out-String
Write-Host "`n  Verificando JWT_SECRET..." -ForegroundColor Gray
Write-Host "  $jwtSecret" -ForegroundColor Gray

if ($jwtSecret -match "_") {
    Write-Host "  ✗ ERRO: JWT_SECRET contém underscore (_)!" -ForegroundColor Red
    Write-Host "    O .env.example no GitHub está DESATUALIZADO!" -ForegroundColor Red
    Write-Host "    SOLUÇÃO: Faça git add .env.example && git commit && git push" -ForegroundColor Yellow
    exit 1
} else {
    Write-Host "  ✓ JWT_SECRET parece válido (sem underscores)!" -ForegroundColor Green
}

# ==========================================
# PASSO 4: SUBIR DOCKER COMPOSE
# ==========================================
Write-Host "`n→ PASSO 4: Subindo serviços (docker compose up --build -d)..." -ForegroundColor Yellow
Write-Host "  Isso pode levar 2-3 minutos..." -ForegroundColor Gray

try {
    docker compose up --build -d
    Write-Host "  ✓ Containers iniciados!" -ForegroundColor Green
} catch {
    Write-Host "  ✗ ERRO ao subir containers!" -ForegroundColor Red
    docker compose ps
    exit 1
}

# ==========================================
# PASSO 5: AGUARDAR SERVIÇOS
# ==========================================
Write-Host "`n→ PASSO 5: Aguardando serviços ficarem healthy (120 segundos)..." -ForegroundColor Yellow

$tempoEspera = 120
for ($i = 0; $i -lt $tempoEspera; $i += 10) {
    Start-Sleep -Seconds 10
    $percentual = [math]::Round(($i / $tempoEspera) * 100)
    Write-Host "  ⏳ $percentual% - $i/$tempoEspera segundos..." -ForegroundColor Gray
}

Write-Host "  ✓ Tempo de espera concluído!" -ForegroundColor Green

# ==========================================
# PASSO 6: VERIFICAR STATUS
# ==========================================
Write-Host "`n→ PASSO 6: Verificando status dos containers..." -ForegroundColor Yellow

docker compose ps

$backend = docker compose ps | Select-String "backend.*healthy"
$frontend = docker compose ps | Select-String "frontend.*healthy"
$postgres = docker compose ps | Select-String "postgres.*healthy"
$minio = docker compose ps | Select-String "minio.*healthy"

Write-Host ""
if ($backend) { Write-Host "  ✓ Backend: healthy" -ForegroundColor Green } else { Write-Host "  ✗ Backend: NÃO healthy" -ForegroundColor Red }
if ($frontend) { Write-Host "  ✓ Frontend: healthy" -ForegroundColor Green } else { Write-Host "  ✗ Frontend: NÃO healthy" -ForegroundColor Red }
if ($postgres) { Write-Host "  ✓ PostgreSQL: healthy" -ForegroundColor Green } else { Write-Host "  ✗ PostgreSQL: NÃO healthy" -ForegroundColor Red }
if ($minio) { Write-Host "  ✓ MinIO: healthy" -ForegroundColor Green } else { Write-Host "  ✗ MinIO: NÃO healthy" -ForegroundColor Red }

# ==========================================
# PASSO 7: TESTAR LOGIN
# ==========================================
Write-Host "`n→ PASSO 7: Testando login (curl)..." -ForegroundColor Yellow

$loginUrl = "http://localhost:8080/v1/auth/login"
$body = '{"username":"admin","password":"admin123"}'

try {
    $response = Invoke-RestMethod -Uri $loginUrl -Method Post -Body $body -ContentType "application/json" -ErrorAction Stop
    
    if ($response.accessToken) {
        Write-Host "  ✓ LOGIN FUNCIONOU!" -ForegroundColor Green
        Write-Host "  ✓ accessToken recebido: $($response.accessToken.Substring(0, 30))..." -ForegroundColor Green
        Write-Host "  ✓ tokenType: $($response.tokenType)" -ForegroundColor Green
        Write-Host "  ✓ expiresIn: $($response.expiresIn)ms ($([math]::Round($response.expiresIn / 60000, 1)) minutos)" -ForegroundColor Green
    } else {
        Write-Host "  ✗ ERRO: Resposta não contém accessToken!" -ForegroundColor Red
        Write-Host "  Resposta: $response" -ForegroundColor Gray
    }
} catch {
    Write-Host "  ✗ ERRO AO FAZER LOGIN!" -ForegroundColor Red
    Write-Host "  Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    Write-Host "  Mensagem: $($_.Exception.Message)" -ForegroundColor Red
    
    # Mostrar logs do backend
    Write-Host "`n  Últimas 30 linhas do backend:" -ForegroundColor Yellow
    docker compose logs --tail=30 backend
    exit 1
}

# ==========================================
# PASSO 8: TESTAR FRONTEND
# ==========================================
Write-Host "`n→ PASSO 8: Testando frontend..." -ForegroundColor Yellow

try {
    $frontendResponse = Invoke-WebRequest -Uri "http://localhost:3000" -UseBasicParsing -ErrorAction Stop
    if ($frontendResponse.StatusCode -eq 200) {
        Write-Host "  ✓ Frontend acessível!" -ForegroundColor Green
        Write-Host "  ✓ Status: $($frontendResponse.StatusCode)" -ForegroundColor Green
    }
} catch {
    Write-Host "  ✗ ERRO: Frontend não está acessível!" -ForegroundColor Red
    Write-Host "  $($_.Exception.Message)" -ForegroundColor Red
}

# ==========================================
# PASSO 9: TESTAR SWAGGER
# ==========================================
Write-Host "`n→ PASSO 9: Testando Swagger UI..." -ForegroundColor Yellow

try {
    $swaggerResponse = Invoke-WebRequest -Uri "http://localhost:8080/swagger-ui.html" -UseBasicParsing -ErrorAction Stop
    if ($swaggerResponse.StatusCode -eq 200) {
        Write-Host "  ✓ Swagger acessível!" -ForegroundColor Green
    }
} catch {
    Write-Host "  ✗ AVISO: Swagger não acessível (não crítico)" -ForegroundColor Yellow
}

# ==========================================
# RESULTADO FINAL
# ==========================================
Write-Host "`n╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  RESULTADO DO TESTE DE REPRODUTIBILIDADE              ║" -ForegroundColor Cyan
Write-Host "╠════════════════════════════════════════════════════════╣" -ForegroundColor Cyan

if ($response.accessToken -and $frontendResponse.StatusCode -eq 200 -and $backend -and $frontend) {
    Write-Host "║                                                        ║" -ForegroundColor Green
    Write-Host "║  ✅ SUCESSO! PROJETO ESTÁ 100% FUNCIONAL               ║" -ForegroundColor Green
    Write-Host "║                                                        ║" -ForegroundColor Green
    Write-Host "║  O avaliador conseguirá executar sem problemas!       ║" -ForegroundColor Green
    Write-Host "║                                                        ║" -ForegroundColor Green
} else {
    Write-Host "║                                                        ║" -ForegroundColor Red
    Write-Host "║  ⚠️ ATENÇÃO! ALGUNS PROBLEMAS ENCONTRADOS              ║" -ForegroundColor Red
    Write-Host "║                                                        ║" -ForegroundColor Red
    Write-Host "║  Verifique os erros acima antes de submeter!          ║" -ForegroundColor Red
    Write-Host "║                                                        ║" -ForegroundColor Red
}

Write-Host "╚════════════════════════════════════════════════════════╝" -ForegroundColor Cyan

Write-Host "`n📋 PRÓXIMOS PASSOS:" -ForegroundColor Cyan
Write-Host "  1. Abrir navegador: http://localhost:3000" -ForegroundColor White
Write-Host "  2. Login: admin / admin123" -ForegroundColor White
Write-Host "  3. Testar todas as funcionalidades" -ForegroundColor White
Write-Host "  4. Fazer commit e push final" -ForegroundColor White
Write-Host ""

# Abrir navegador automaticamente
Write-Host "→ Abrindo navegador..." -ForegroundColor Yellow
Start-Process "http://localhost:3000"

Write-Host "`n✅ Teste concluído!" -ForegroundColor Green
Write-Host "   Diretório de teste: $testeDir" -ForegroundColor Gray
Write-Host "   Para limpar: docker compose down && cd .. && Remove-Item -Recurse -Force '$testeDir'" -ForegroundColor Gray
