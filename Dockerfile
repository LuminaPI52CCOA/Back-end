FROM eclipse-temurin:21-jre-alpine
# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia o arquivo .jar gerado pelo 'mvn package' da pasta target para dentro do container
COPY target/backend-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta em que o Spring Boot roda por padrão (geralmente 8080)
EXPOSE 8080

# Comando para iniciar a aplicação Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]