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

## Documentação da API

Com a aplicação em execução, o contrato completo e navegável está disponível em:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`

Os endpoints de documentação são públicos. Para desabilitá-los em um ambiente, defina `OPENAPI_ENABLED=false`.

## Autenticação e autorização

As rotas são protegidas por padrão. Envie o token obtido no login em todas as rotas protegidas:

```http
Authorization: Bearer <jwt>
```

As rotas `/api/admin/**` exigem o papel `ADMIN`. Rotas de responsável e cuidador também validam no serviço se o usuário participa do recurso solicitado. O logout é stateless: ele confirma a saída para o cliente, mas não mantém uma lista de revogação do JWT; o cliente deve descartar o token.

## Convenções do contrato HTTP

- URIs, parâmetros e propriedades externas usam inglês; nomes internos e do banco podem permanecer em português.
- `GET` consulta recursos, `POST` cria recursos ou dispara comandos não idempotentes, `PUT` substitui a representação editável e `PATCH` altera parcialmente dados ou estado. `DELETE` remove o recurso permitido pelo domínio.
- Transições de aceite, rejeição e cancelamento usam `PATCH`. Os antigos `POST .../accept` e `POST .../reject` permanecem temporariamente como aliases depreciados e aparecem assim no OpenAPI.
- Rotas canônicas de tarefas do cuidador usam `/api/caregiver/care-tasks` e o sub-recurso `/occurrences`. Aliases sob `/api/caregiver/tasks` ou sem `/occurrences` são compatibilidade depreciada.
- Datas, horários e instantes seguem ISO 8601 (`YYYY-MM-DD`, `HH:mm:ss` e um instante com offset/UTC). O fuso padrão dos fluxos assistenciais é `America/Sao_Paulo` quando indicado.
- Endpoints paginados recebem `page` baseado em zero e `size`. Os envelopes de domínio expõem `content`, `page`, `size`, `totalElements` e `totalPages`; alguns também expõem `last`. Listas sem paginação retornam um array JSON.
- Uploads usam `multipart/form-data`; os nomes e limites das partes constam no OpenAPI. O limite de uma foto é 5 MB.

## Respostas de erro

Erros da aplicação e da camada de segurança usam o mesmo formato:

```json
{
  "timestamp": "2026-10-12T18:00:00Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Dados inválidos.",
  "fields": {
    "campo": "Mensagem de validação."
  }
}
```

Os status mais comuns são `400` (requisição/validação), `401` (autenticação ausente ou sessão inválida), `403` (sem acesso), `404` (recurso), `409` (conflito de estado), `413` (upload excedido) e `500` (erro inesperado). O campo `code` é estável para tratamento pelo cliente; `message` é voltado à exibição. `fields` fica vazio quando não há erros por campo.

## Mapa dos módulos

| Módulo | Bases principais |
|---|---|
| Saúde e autenticação | `/health`, `/api/auth` |
| Conta, usuário e perfil | `/api/account`, `/api/users`, `/api/profile`, `/api/profile-photos` |
| Administração e auditoria | `/api/admin` |
| Busca de cuidadores | `/api/caregivers` |
| Rotinas de cuidado | `/api/responsible/care-routines` |
| Solicitações e oportunidades | `/api/service-requests`, `/api/responsible/service-requests`, `/api/responsible/service-publications`, `/api/caregiver/service-requests`, `/api/caregiver/service-opportunities` |
| Contratações e encerramento | `/api/contracts`, `/api/responsible/contracts`, `/api/caregiver/contracts` |
| Agenda e atendimento | `/api/agenda/events`, `/api/{role}/contracts/{contractId}/attendance` |
| Tarefas e ocorrências | `/api/responsible/care-tasks`, `/api/caregiver/care-tasks`, `/api/responsible/care-occurrences`, `/api/care-occurrences` |
| Diário assistencial | `/api/caregiver/assisted-person-diary`, `/api/responsible/assisted-person-diary`, `/api/care-diary` |
| Relatórios de atendimento | leitura em `/api/{role}/contracts/{contractId}/attendance/{date}/report`, edição/finalização em `/api/caregiver/contracts/{contractId}/attendance/{date}/report`, consulta por id em `/api/attendance-reports` |
| Notificações | `/api/notifications` |

O Swagger UI é a fonte detalhada para métodos, filtros, corpos, enums, respostas e schemas de cada operação, inclusive endpoints administrativos e multipart.
