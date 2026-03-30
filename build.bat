call mvn clean package -DskipTests
call cp app-infra-server\target\app-infra-server-1.0.0.jar deploy\app-infra-server.jar
call cp app-api-server\target\app-api-server-1.0.0.jar deploy\app-api-server.jar
call cp app-mock-server\target\app-mock-server-1.0.0.jar deploy\app-mock-server.jar