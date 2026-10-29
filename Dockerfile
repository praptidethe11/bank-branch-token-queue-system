FROM eclipse-temurin:21-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY target/token-queue-system.war app.war
EXPOSE 8082
RUN useradd -r appuser
USER appuser
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8082/ || exit 1
ENTRYPOINT ["java", "-jar", "app.war"]
