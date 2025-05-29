FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY build/libs/api-gateway.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
