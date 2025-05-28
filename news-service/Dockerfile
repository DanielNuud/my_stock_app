FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY build/libs/news-service.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
