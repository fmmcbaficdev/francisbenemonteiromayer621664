# ==========================================
# SCRIPT DE TESTE - SIMULAR AVALIADOR
# ==========================================
# Este script simula exatamente o que o avaliador vai fazer
# Execute em PowerShell COMO ADMINISTRADOR

Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "  TESTE DE REPRODUTIBILIDADE - SIMULANDO AVALIADOR" -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

$ErrorActionPreference = "Continue"
$testeDir = "C:\temp\teste-avaliador-seplag"

# ==========================================
# PASSO 1: LIMPAR AMBIENTE
# ==========================================
Write-Host "-> PASSO 1: Limpando ambiente de teste..." -ForegroundColor Yellow

if (Test-Path $testeDir) {
    Write-Host "  Removendo diretorio anterior..." -ForegroundColor Gray
    Remove-Item -Path $testeDir -Recurse -Force -ErrorAction SilentlyContinue
}

# ==========================================
# PASSO 2: CLONAR REPOSITORIO
# ==========================================
Write-Host "`n-> PASSO 2: Clonando repositorio (como avaliador)..." -ForegroundColor Yellow
Write-Host "  git clone https://github.com/fmmcbaficdev/francisbenemonteiromayer621664" -ForegroundColor Gray

try {
    git clone https://github.com/fmmcbaficdev/francisbenemonteiromayer621664 $testeDir 2>&1 | Out-Null
    Write-Host "  OK Repositorio clonado com sucesso!" -ForegroundColor Green
}
catch {
    Write-Host "  ERRO ao clonar repositorio!" -ForegroundColor Red
    Write-Host "    Verifique se o push foi feito: git push origin main" -ForegroundColor Yellow
    exit 1
}

Set-Location $testeDir

# ==========================================
# PASSO 3: CONFIGURAR .env
# ==========================================
Write-Host "`n-> PASSO 3: Configurando .env (cp .env.example .env)..." -ForegroundColor Yellow

if (!(Test-Path ".env.example")) {
    Write-Host "  ERRO: .env.example nao encontrado!" -ForegroundColor Red
    exit 1
}

Copy-Item ".env.example" ".env" -Force
Write-Host "  OK .env criado!" -ForegroundColor Green

# Verificar JWT_SECRET (extrair APENAS o valor)
Write-Host "`n  Verificando JWT_SECRET..." -ForegroundColor Gray
$jwtLine = Get-Content .env | Where-Object { $_ -match "^JWT_SECRET=" }
$jwtValue = ($jwtLine -split "=", 2)[1]
Write-Host "  JWT_SECRET=$jwtValue" -ForegroundColor Gray

# Verificar se o VALOR tem underscore (caractere ilegal em Base64)
if ($jwtValue -match "_") {
    Write-Host "  ERRO: JWT_SECRET contem underscore (_) no valor!" -ForegroundColor Red
    Write-Host "    O .env.example no GitHub esta DESATUALIZADO!" -ForegroundColor Red
    Write-Host "    SOLUCAO: Faca git add .env.example, git commit e git push" -ForegroundColor Yellow
    exit 1
}
else {
    Write-Host "  OK JWT_SECRET valido (Base64 sem underscores)!" -ForegroundColor Green
}

# ==========================================
# PASSO 4: SUBIR DOCKER COMPOSE
# ==========================================
Write-Host "`n-> PASSO 4: Subindo servicos (docker compose up --build -d)..." -ForegroundColor Yellow
Write-Host "  Isso pode levar 2-3 minutos..." -ForegroundColor Gray

try {
    docker compose up --build -d 2>&1 | Out-Null
    Write-Host "  OK Containers iniciados!" -ForegroundColor Green
}
catch {
    Write-Host "  ERRO ao subir containers!" -ForegroundColor Red
    docker compose ps
    exit 1
}

# ==========================================
# PASSO 5: AGUARDAR SERVICOS
# ==========================================
Write-Host "`n-> PASSO 5: Aguardando servicos ficarem healthy (120 segundos)..." -ForegroundColor Yellow

$tempoEspera = 120
for ($i = 0; $i -lt $tempoEspera; $i += 10) {
    Start-Sleep -Seconds 10
    $percentual = [math]::Round(($i / $tempoEspera) * 100)
    Write-Host "  Aguardando $percentual% - $i/$tempoEspera segundos..." -ForegroundColor Gray
}

Write-Host "  OK Tempo de espera concluido!" -ForegroundColor Green

# ==========================================
# PASSO 6: VERIFICAR STATUS
# ==========================================
Write-Host "`n-> PASSO 6: Verificando status dos containers..." -ForegroundColor Yellow

docker compose ps

$psOutput = docker compose ps | Out-String
$backend = $psOutput -match "backend.*healthy"
$frontend = $psOutput -match "frontend.*healthy"
$postgres = $psOutput -match "postgres.*healthy"
$minio = $psOutput -match "minio.*healthy"

Write-Host ""
if ($backend) { Write-Host "  OK Backend: healthy" -ForegroundColor Green } else { Write-Host "  X Backend: NAO healthy" -ForegroundColor Red }
if ($frontend) { Write-Host "  OK Frontend: healthy" -ForegroundColor Green } else { Write-Host "  X Frontend: NAO healthy" -ForegroundColor Red }
if ($postgres) { Write-Host "  OK PostgreSQL: healthy" -ForegroundColor Green } else { Write-Host "  X PostgreSQL: NAO healthy" -ForegroundColor Red }
if ($minio) { Write-Host "  OK MinIO: healthy" -ForegroundColor Green } else { Write-Host "  X MinIO: NAO healthy" -ForegroundColor Red }

# ==========================================
# PASSO 7: TESTAR LOGIN
# ==========================================
Write-Host "`n-> PASSO 7: Testando login (curl)..." -ForegroundColor Yellow

$loginUrl = "http://localhost:8080/v1/auth/login"
$bodyJson = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

$loginOk = $false
try {
    $response = Invoke-RestMethod -Uri $loginUrl -Method Post -Body $bodyJson -ContentType "application/json" -ErrorAction Stop
    
    if ($response.accessToken) {
        Write-Host "  OK LOGIN FUNCIONOU!" -ForegroundColor Green
        $tokenPreview = $response.accessToken.Substring(0, [Math]::Min(30, $response.accessToken.Length))
        Write-Host "  OK accessToken recebido: $tokenPreview..." -ForegroundColor Green
        Write-Host "  OK tokenType: $($response.tokenType)" -ForegroundColor Green
        $expiresInMinutes = [math]::Round($response.expiresIn / 60000, 1)
        Write-Host "  OK expiresIn: $($response.expiresIn)ms ($expiresInMinutes minutos)" -ForegroundColor Green
        $loginOk = $true
    }
    else {
        Write-Host "  ERRO: Resposta nao contem accessToken!" -ForegroundColor Red
        Write-Host "  Resposta: $response" -ForegroundColor Gray
    }
}
catch {
    Write-Host "  ERRO AO FAZER LOGIN!" -ForegroundColor Red
    Write-Host "  Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    Write-Host "  Mensagem: $($_.Exception.Message)" -ForegroundColor Red
    
    # Mostrar logs do backend
    Write-Host "`n  Ultimas 30 linhas do backend:" -ForegroundColor Yellow
    docker compose logs --tail=30 backend
}

# ==========================================
# PASSO 8: TESTAR FRONTEND
# ==========================================
Write-Host "`n-> PASSO 8: Testando frontend..." -ForegroundColor Yellow

$frontendOk = $false
try {
    $frontendResponse = Invoke-WebRequest -Uri "http://localhost:3000" -UseBasicParsing -ErrorAction Stop
    if ($frontendResponse.StatusCode -eq 200) {
        Write-Host "  OK Frontend acessivel!" -ForegroundColor Green
        Write-Host "  OK Status: $($frontendResponse.StatusCode)" -ForegroundColor Green
        $frontendOk = $true
    }
}
catch {
    Write-Host "  ERRO: Frontend nao esta acessivel!" -ForegroundColor Red
    Write-Host "  $($_.Exception.Message)" -ForegroundColor Red
}

# ==========================================
# PASSO 9: TESTAR SWAGGER
# ==========================================
Write-Host "`n-> PASSO 9: Testando Swagger UI..." -ForegroundColor Yellow

try {
    $swaggerResponse = Invoke-WebRequest -Uri "http://localhost:8080/swagger-ui.html" -UseBasicParsing -ErrorAction Stop
    if ($swaggerResponse.StatusCode -eq 200) {
        Write-Host "  OK Swagger acessivel!" -ForegroundColor Green
    }
}
catch {
    Write-Host "  AVISO: Swagger nao acessivel (nao critico)" -ForegroundColor Yellow
}

# ==========================================
# RESULTADO FINAL
# ==========================================
Write-Host "`n================================================================" -ForegroundColor Cyan
Write-Host "  RESULTADO DO TESTE DE REPRODUTIBILIDADE" -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan

if ($loginOk -and $frontendOk -and $backend -and $frontend) {
    Write-Host ""
    Write-Host "  SUCESSO! PROJETO ESTA 100% FUNCIONAL" -ForegroundColor Green
    Write-Host ""
    Write-Host "  O avaliador conseguira executar sem problemas!" -ForegroundColor Green
    Write-Host ""
}
else {
    Write-Host ""
    Write-Host "  ATENCAO! ALGUNS PROBLEMAS ENCONTRADOS" -ForegroundColor Red
    Write-Host ""
    Write-Host "  Verifique os erros acima antes de submeter!" -ForegroundColor Red
    Write-Host ""
}

Write-Host "================================================================" -ForegroundColor Cyan

Write-Host "`nPROXIMOS PASSOS:" -ForegroundColor Cyan
Write-Host "  1. Abrir navegador: http://localhost:3000" -ForegroundColor White
Write-Host "  2. Login: admin / admin123" -ForegroundColor White
Write-Host "  3. Testar todas as funcionalidades" -ForegroundColor White
Write-Host ""

# Abrir navegador automaticamente
Write-Host "-> Abrindo navegador..." -ForegroundColor Yellow
Start-Process "http://localhost:3000"

Write-Host "`nOK Teste concluido!" -ForegroundColor Green
Write-Host "   Diretorio de teste: $testeDir" -ForegroundColor Gray
Write-Host "   Para limpar: cd '$testeDir'; docker compose down; cd ..; Remove-Item -Recurse -Force '$testeDir'" -ForegroundColor Gray
