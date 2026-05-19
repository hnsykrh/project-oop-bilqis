# One-time setup: downloads Maven into this project folder (no Windows PATH needed).
$ErrorActionPreference = "Stop"
$projectRoot = $PSScriptRoot
$toolsDir = Join-Path $projectRoot ".tools"
$mavenVersion = "3.9.6"
$mavenHome = Join-Path $toolsDir "apache-maven-$mavenVersion"
$mvn = Join-Path $mavenHome "bin\mvn.cmd"

if (Test-Path $mvn) {
    Write-Host "Maven already installed at: $mavenHome"
    & $mvn -version
    exit 0
}

New-Item -ItemType Directory -Path $toolsDir -Force | Out-Null
$zip = Join-Path $env:TEMP "apache-maven-$mavenVersion-bin.zip"
$url = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"

Write-Host "Downloading Maven $mavenVersion (about 9 MB)..."
Invoke-WebRequest -Uri $url -OutFile $zip -UseBasicParsing

Write-Host "Extracting to $toolsDir ..."
Expand-Archive -Path $zip -DestinationPath $toolsDir -Force
Remove-Item $zip -Force -ErrorAction SilentlyContinue

if (-not (Test-Path $mvn)) {
    Write-Error "Setup failed. Expected: $mvn"
}
Write-Host "Success! Maven is ready."
& $mvn -version
