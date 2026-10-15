FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/token-queue-system.war app.war
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.war"]