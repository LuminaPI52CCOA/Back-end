<#
    .SYNOPSIS
    Script de automação para empacotar a entrega individual do exercício Fluent Bit + AWS Glue + Athena.
#>

$ErrorActionPreference = "Stop"

$destinationZip = Join-Path -Path $PSScriptRoot -ChildPath "entrega-fluentbit-logs.zip"

Write-Host "Iniciando criacao do pacote de entrega: $destinationZip" -ForegroundColor Cyan

# Remove zip anterior se existir
if (Test-Path $destinationZip) {
    Remove-Item $destinationZip -Force
    Write-Host "Arquivo .zip anterior removido com sucesso." -ForegroundColor Yellow
}

# Criar pasta temporaria para montar o pacote limpo
$tempDir = Join-Path -Path $env:TEMP -ChildPath "entrega_fluentbit_temp_$([guid]::NewGuid().ToString().Substring(0,8))"
New-Item -ItemType Directory -Path $tempDir | Out-Null

try {
    # Lista de arquivos e pastas essenciais
    $itemsToCopy = @(
        "docker-compose.yml",
        "Dockerfile",
        "fluent-bit.conf",
        "fluent-bit.yaml",
        "parsers.conf",
        "nginx-proxy.conf",
        ".env.example",
        "FLUENTBIT_PIPELINE_LOGS.md",
        "pom.xml",
        "pipeline-logs",
        "evidencias",
        "src"
    )

    foreach ($item in $itemsToCopy) {
        $sourcePath = Join-Path -Path $PSScriptRoot -ChildPath $item
        if (Test-Path $sourcePath) {
            Copy-Item -Path $sourcePath -Destination $tempDir -Recurse -Force
            Write-Host "  [+] Incluido: $item" -ForegroundColor Green
        } else {
            Write-Host "  [-] Aviso: $item nao encontrado." -ForegroundColor DarkYellow
        }
    }

    # Compacta a pasta temporaria para o zip final
    Compress-Archive -Path "$tempDir\*" -DestinationPath $destinationZip -CompressionLevel Optimal
    Write-Host "`nEntrega empacotada com sucesso em: $destinationZip" -ForegroundColor Green
    
    $fileSize = (Get-Item $destinationZip).Length / 1MB
    Write-Host ("Tamanho final do .zip: {0:N2} MB" -f $fileSize) -ForegroundColor Cyan
}
finally {
    # Limpa pasta temporaria
    if (Test-Path $tempDir) {
        Remove-Item $tempDir -Recurse -Force -ErrorAction SilentlyContinue
    }
}
