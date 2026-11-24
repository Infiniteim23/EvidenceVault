param([string]$BackupDir = ".\backups")
if (-not $env:DB_PASSWORD) { throw "Set DB_PASSWORD before running this script." }
New-Item -ItemType Directory -Force -Path $BackupDir | Out-Null
$stamp = Get-Date -Format "yyyyMMdd-HHmmss"
$out = Join-Path $BackupDir "evidencevault-$stamp.sql"
$env:MYSQL_PWD = $env:DB_PASSWORD
mysqldump --host localhost --user evidencevault --single-transaction --routines --triggers evidencevault | Out-File -Encoding utf8 $out
Remove-Item Env:MYSQL_PWD
Write-Host "Created $out"
