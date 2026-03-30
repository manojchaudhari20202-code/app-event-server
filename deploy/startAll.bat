PowerShell -Command "Get-Process -Name '*javaw*' | Stop-Process -Force"

echo 'Starting Infra Server...'
call rm -rf logs\app-infra-server.log
start "INFRA" javaw -jar app-infra-server.jar

echo 'Starting Event API Server...'
call rm -rf logs\app-event-server.log
start "API" javaw -jar app-api-server.jar

echo 'Starting Event Mock Data server ...'
call rm -rf logs\app-mock-server.log
start "MOCK" javaw -jar app-mock-server.jar