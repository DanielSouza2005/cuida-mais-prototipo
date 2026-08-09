FROM ubuntu:24.04 AS build

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        ca-certificates \
        maven \
        openjdk-21-jdk-headless \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace

# Como o Root Directory do Render já é /api,
# pom.xml e src estão diretamente no contexto do Docker.
COPY pom.xml ./pom.xml

# Baixa dependências primeiro para aproveitar cache.
RUN mvn --batch-mode --no-transfer-progress dependency:go-offline

COPY src ./src

RUN mvn --batch-mode --no-transfer-progress clean package -DskipTests \
    && cp target/cuida-plus-api-0.1.0.jar /workspace/app.jar


FROM eclipse-temurin:21-jre-noble AS runtime

WORKDIR /app

ENV PORT=8080

RUN mkdir -p /app/uploads \
    && chown -R 10001:10001 /app

COPY --from=build --chown=10001:10001 /workspace/app.jar /app/app.jar

USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]