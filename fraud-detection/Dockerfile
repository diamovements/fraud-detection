FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY . .

# RUN ./gradlew build

COPY build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]