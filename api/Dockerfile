FROM ubuntu:24.04 AS build

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        ca-certificates \
        maven \
        openjdk-21-jdk-headless \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace

COPY api/pom.xml api/pom.xml
RUN mvn --batch-mode --no-transfer-progress -f api/pom.xml dependency:go-offline

COPY api/src api/src
RUN mvn --batch-mode --no-transfer-progress -f api/pom.xml clean package -DskipTests \
    && cp api/target/cuida-plus-api-0.1.0.jar /workspace/app.jar

FROM eclipse-temurin:21-jre-noble AS runtime

ENV PORT=8080

WORKDIR /app

RUN mkdir -p /app/uploads \
    && chown -R 10001:10001 /app

COPY --from=build --chown=10001:10001 /workspace/app.jar /app/app.jar

USER 10001:10001

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD curl --fail --silent --show-error "http://localhost:${PORT}/health" > /dev/null || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
