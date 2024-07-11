FROM --platform=linux/x86_64 maven:3.9.7-eclipse-temurin-17-alpine as maven-builder
COPY src /app/src
COPY pom.xml /app

RUN mvn -f /app/pom.xml clean package -DskipTests

FROM --platform=linux/x86_64 openjdk:17-alpine
ARG JAR_NAME
COPY --from=maven-builder /app/target/$JAR_NAME /app/service1.jar
WORKDIR /app
EXPOSE 8080
RUN mkdir "/app/data"
CMD ["java", "-jar", "service1.jar"]
