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

## Recuperacao de senha por deep link

O app usa o scheme `cuidarplus`. O link mobile esperado para redefinicao de senha e:

```text
cuidarplus://reset-password?token=token_fake
```

Para testar no Android com o app instalado:

```bash
adb shell am start -W -a android.intent.action.VIEW -d "cuidarplus://reset-password?token=token_fake"
```

Para testar no iOS Simulator:

```bash
xcrun simctl openurl booted "cuidarplus://reset-password?token=token_fake"
```

No backend, configure os links por ambiente:

```env
PASSWORD_RESET_MOBILE_URL=cuidarplus://reset-password
PASSWORD_RESET_WEB_URL=http://localhost:8081/reset-password
PASSWORD_RESET_PREFER_MOBILE_LINK=true
PASSWORD_RESET_EXPIRATION_MINUTES=30
```

Em celular fisico durante o desenvolvimento, use o IP local da maquina no fallback web, por exemplo `http://IP_LOCAL_DA_MAQUINA:19006/reset-password`, porque `localhost` no celular aponta para o proprio aparelho.

Em producao, troque `PASSWORD_RESET_WEB_URL` para o dominio HTTPS final, por exemplo `https://seudominio.com/reset-password`. Universal Links/App Links ainda exigem associacao do dominio no Android e iOS antes de substituir o fallback web por abertura universal. Clientes de e-mail podem remover links com scheme customizado, entao o botao do e-mail usa uma URL HTTP/HTTPS segura para clique.
