# Auditoria visual inicial — Cuidar+

## Paleta de cores

- Base quente: fundo creme `oklch(0.99 0.005 100)`, aproximado em React Native como `#FCFBF7`.
- Primária: azul médio de confiança `oklch(0.55 0.13 235)`, aproximado como `#2F8FC4`.
- Texto: azul-marinho escuro `oklch(0.22 0.04 240)`, aproximado como `#1B2D40`.
- Apoio: céu claro, menta/sálvia, coral e amarelo suave para cartões e destaques.
- Superfícies: cartões brancos, bordas azul-acinzentadas claras e estados muted suaves.

## Tipografia

- Família principal: Plus Jakarta Sans, com pesos 400, 500, 600, 700 e 800.
- Títulos fortes, com tracking levemente negativo e tamanhos próximos de 26–30 px.
- Corpo entre 13–15 px, com metadados/legendas entre 10–12 px.

## Espaçamentos

- Ritmo baseado em 4 px.
- Margens laterais recorrentes de 24–28 px.
- Áreas de toque principais entre 48–56 px.
- Gaps comuns de 8, 12, 16 e 24 px.

## Bordas e sombras

- Raio-base de 16 px, cartões grandes chegando a 28–32 px.
- Botões e campos arredondados em 16 px.
- Sombras frias e difusas; CTA primário com brilho azul.

## Componentes reutilizáveis identificados

- Shell mobile / área segura.
- Marca `C+` / `Cuidar+`.
- Botão primário.
- Cabeçalho com ação de voltar.
- Campo com ícone.
- Cartão de menu/opção.
- Placeholder visual para telas ainda não migradas.

## Fluxo de navegação desta etapa

- `/` apresenta onboarding e leva para `/login` ou `/overview`.
- `/overview` lista todas as telas criadas para inspeção.
- Autenticação: `/login`, `/signup`, `/forgot-password`, `/reset-password`.
- Perfil: `/profile`, `/edit-profile`.
- Nenhum backend, autenticação simulada ou regra de negócio foi implementado.
