# 1: Build - compilar con Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# 2: Runtime - imagen ligera solo para ejecutar
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/fancollector-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]