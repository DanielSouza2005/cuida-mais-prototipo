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
