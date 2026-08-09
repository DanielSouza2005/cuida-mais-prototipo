# Cuida Plus API

API Spring Boot do Cuidar+ com autenticacao, cadastro, recuperacao de senha e edicao de perfil.

## Requisitos

- Java 21
- Maven 3.9+
- PostgreSQL 16

## Configuracao

A API nao usa `.env` proprio. As configuracoes ficam em:

```text
api/src/main/resources/application.properties
```

Esse arquivo contem as propriedades de desenvolvimento local para:

- porta da API;
- conexao com PostgreSQL;
- JWT;
- CORS;
- recuperacao de senha;
- SMTP.

As credenciais do banco e a chave JWT são obrigatoriamente fornecidas por variáveis de ambiente. Use `api/.env.example` como referência e não coloque valores reais no arquivo versionado.

## Rodando localmente

Na raiz do repositorio:

```bash
docker compose up -d postgres
```

Na pasta `api`:

```bash
mvn spring-boot:run
```

Por padrao a API sobe em `http://localhost:8080`.

## Docker

O build usa Ubuntu 24.04 e OpenJDK 21 no estágio de compilação. A execução usa a imagem JRE 21 do Eclipse Temurin, contém apenas o JAR da aplicação e roda com o UID/GID não-root `10001:10001`.

Na raiz do repositório, gere a imagem:

```bash
docker build -t cuida-plus-api .
```

Prepare um arquivo local de variáveis, ajuste os valores e mantenha-o fora do controle de versão:

```bash
cp api/.env.example api/.env
```

Execute a API com acesso ao PostgreSQL instalado na máquina hospedeira:

```bash
docker run --rm \
  --name cuida-plus-api \
  --add-host=host.docker.internal:host-gateway \
  --env-file api/.env \
  -p 8080:8080 \
  -v cuida-plus-uploads:/app/uploads \
  cuida-plus-api
```

No Docker Desktop, `host.docker.internal` já está disponível; a opção `--add-host` mantém o mesmo comando compatível com Docker Engine no Linux.

Para definir somente o profile adicionalmente:

```bash
docker run --rm -p 8080:8080 \
  --env-file api/.env \
  -e SPRING_PROFILES_ACTIVE=prod \
  cuida-plus-api
```

Depois da inicialização, verifique o endpoint público existente:

```bash
curl --fail http://localhost:8080/health
```

A imagem também possui um healthcheck contra `/health`. Ele reutiliza o `curl` já fornecido pela imagem oficial do Eclipse Temurin, sem instalar pacotes adicionais no estágio final.

## Recuperacao de senha

O endpoint `POST /api/auth/forgot-password` sempre retorna uma mensagem neutra. Quando o e-mail existe, a API gera um token criptograficamente seguro, armazena apenas seu hash SHA-256, invalida tokens anteriores nao usados e envia um link por e-mail quando `app.mail.enabled=true`.

O endpoint `POST /api/auth/reset-password` valida o token, rejeita tokens invalidos, expirados ou ja usados, atualiza a senha com o `PasswordEncoder` da aplicacao e marca o token como utilizado.

## Endpoints

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- `POST /api/auth/logout`
- `GET /api/users/me`
- `PUT /api/users/me`
