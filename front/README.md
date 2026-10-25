# Cuida Mais Prototipo

Aplicativo Expo com Expo Router integrado a API Spring Boot do projeto.

## Como rodar

1. Instale as dependencias:

   ```bash
   npm install
   ```

2. Configure a URL da API criando um arquivo `.env` na pasta `front`:

   ```env
   EXPO_PUBLIC_API_URL=http://localhost:8080
   ```

   O app resolve esse valor em runtime para funcionar melhor no mobile:

   - Android Emulator: `localhost` sera convertido para `http://10.0.2.2:8080`
   - iOS Simulator: `http://localhost:8080` funciona normalmente
   - Celular fisico: se o app estiver rodando pelo Expo com host LAN, `localhost` sera convertido para `http://IP_LOCAL_DA_MAQUINA:8080`

   Se a conversao automatica nao bater com a sua rede, configure explicitamente:

   ```env
   EXPO_PUBLIC_API_URL=http://IP_LOCAL_DA_MAQUINA:8080
   ```

   A API Spring Boot deste projeto expoe os endpoints reais em `/api/auth/*`.

3. Inicie o app:

   ```bash
   npx expo start
   ```

## Recuperação de senha por código

O fluxo acontece integralmente no aplicativo: o usuário informa o e-mail, digita o código numérico recebido e define a nova senha. A recuperação de senha não usa deep link e a rota legada `/reset-password` apenas encaminha para esse fluxo.

No backend, a validade do código pode ser configurada por ambiente. O serviço limita o valor efetivo ao intervalo seguro de 5 a 15 minutos e usa 10 minutos por padrão:

```env
PASSWORD_RESET_EXPIRATION_MINUTES=10
```
