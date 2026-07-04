# Padrão de UX para chamadas de API

## Objetivo

Toda chamada de API deve ter estado visual claro e bloquear ações concorrentes. Isso evita duplo envio, navegação indevida, tela desmontada durante uma requisição e inconsistências de estado.

## Regra geral

Sempre que uma tela consumir API:

- crie um estado de loading, submitting, saving ou busy;
- bloqueie o botão principal;
- bloqueie inputs;
- bloqueie selects, chips e date pickers;
- bloqueie links e ações secundárias;
- bloqueie navegação durante operações críticas;
- use `finally` para liberar a interface;
- preserve os dados preenchidos em caso de erro;
- mostre uma mensagem amigável.

## Loading inicial e submitting

Loading inicial acontece quando a tela está carregando dados. Nesse caso, mostre spinner, skeleton ou um estado de carregamento e não exiba formulário vazio como se fosse dado real.

Submitting acontece quando o usuário enviou dados. Nesse caso, bloqueie o formulário inteiro, mostre loading no botão principal, impeça sair da tela e impeça outra ação crítica até a requisição terminar.

## Ações que devem ser bloqueadas

- Botão principal.
- Links como "Cadastre-se" e "Esqueci minha senha".
- Botão de voltar.
- Cards clicáveis.
- Chips.
- Selects.
- Inputs.
- DatePicker.
- Busca CEP.
- Submit pelo teclado.

## Exemplo prático

```ts
try {
  setIsSubmitting(true);
  await apiCall();
} catch (error) {
  showFriendlyError(error);
} finally {
  setIsSubmitting(false);
}
```

Use componentes e helpers compartilhados sempre que possível:

- `PrimaryButton` com `disabled` e `loading`;
- `BackButton` e `AppHeader` com `disabled`/`backDisabled`;
- `AppTextInput` com `disabled`;
- `OptionGroup` e `DatePickerField` com `disabled`;
- `useBlockNavigationWhenBusy(isBusy)` para operações críticas;
- `guardPress(isBusy, callback)` para ações clicáveis customizadas.

## Checklist para novas telas

- O botão principal tem loading?
- Inputs ficam desabilitados?
- Links secundários ficam desabilitados?
- Botão de voltar é tratado?
- Há prevenção de duplo clique?
- Erro libera a tela?
- Dados preenchidos são preservados?
- Há mensagem amigável?
- Acentuação está correta?

## Delay artificial

Use o delay artificial para testar estados de loading:

```env
EXPO_PUBLIC_API_ARTIFICIAL_DELAY_MS=1000
```

Use apenas em desenvolvimento. Nunca ative em produção. Esse delay é útil para confirmar bloqueios visuais, links desabilitados, botão de voltar inativo e prevenção de duplo envio.
