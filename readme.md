
## Build and Deploy Instructions [Sequence]

### Checkout main branch
- git clone -b main git@github.com:manojchaudhari20202-code/app-event-server.git
- OR Donwload ::: https://github.com/manojchaudhari20202-code/app-event-server/archive/refs/heads/main.zip

### Test
mvn clean test surefire-report:report-only -Dmaven.test.failure.ignore=true

### Build
mvn clean package -DskipTests

### Start Infra Server
- cd app-infra-server 
- mvn exec:java -Dexec.mainClass="com.example.app.AppInfraBootstrap"
- DB GUI URL ::: http://localhost:8081/
- Health URL ::: http://localhost:8081/actuator/health

### Start Mock Server
- cd app-mock-server 
- mvn exec:java -Dexec.mainClass="com.example.app.MockServer"
- GraphQL GUI URL ::: http://localhost:8080/graphiql
- Health URL ::: http://localhost:8080/actuator/health

### Start API Server
- cd app-api-server 
- mvn exec:java -Dexec.mainClass="com.example.app.APIServer"
- GraphQL GUI URL ::: http://localhost:8080/graphiql
- Health URL ::: http://localhost:8080/actuator/health

--- 

# Architecture