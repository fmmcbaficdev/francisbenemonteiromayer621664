# Script de Diagnóstico - Erro 500 no Login
# Execute: .\diagnose-error.ps1

Write-Host "=== DIAGNÓSTICO DO ERRO 500 ===" -ForegroundColor Cyan
Write-Host ""

# 1. Verificar status dos containers
Write-Host "1. Status dos Containers:" -ForegroundColor Yellow
docker compose ps
Write-Host ""

# 2. Verificar logs do backend (últimas 50 linhas)
Write-Host "2. Logs do Backend (últimas 50 linhas):" -ForegroundColor Yellow
docker logs ppfullstack621664-backend --tail=50
Write-Host ""

# 3. Verificar se o banco de dados está acessível
Write-Host "3. Testando conexão com PostgreSQL:" -ForegroundColor Yellow
docker exec ppfullstack621664-postgres pg_isready -U seplag -d seplag
Write-Host ""

# 4. Verificar se a tabela de usuários existe
Write-Host "4. Verificando tabela de usuários:" -ForegroundColor Yellow
docker exec ppfullstack621664-postgres psql -U seplag -d seplag -c "\dt usuarios"
Write-Host ""

# 5. Verificar se existe o usuário admin
Write-Host "5. Verificando usuário admin:" -ForegroundColor Yellow
docker exec ppfullstack621664-postgres psql -U seplag -d seplag -c "SELECT id, username, nome, ativo FROM usuarios WHERE username='admin';"
Write-Host ""

# 6. Testar endpoint de health
Write-Host "6. Testando Health Check:" -ForegroundColor Yellow
curl -s http://localhost:8080/actuator/health | ConvertFrom-Json | ConvertTo-Json
Write-Host ""

# 7. Testar login via curl
Write-Host "7. Testando Login via API:" -ForegroundColor Yellow
$body = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/v1/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $body `
        -ErrorAction Stop
    
    Write-Host "✅ Login bem-sucedido!" -ForegroundColor Green
    $response.Content | ConvertFrom-Json | ConvertTo-Json
} catch {
    Write-Host "❌ Erro no login:" -ForegroundColor Red
    Write-Host $_.Exception.Message
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Resposta do servidor:" -ForegroundColor Red
        Write-Host $responseBody
    }
}

Write-Host ""
Write-Host "=== FIM DO DIAGNÓSTICO ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Por favor, copie TODA a saída acima e envie para o desenvolvedor." -ForegroundColor Yellow
