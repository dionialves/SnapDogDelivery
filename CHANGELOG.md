# Changelog — SnapDog Delivery

Índice de versões lançadas. Cada versão a partir da **0.3.0** possui documentação completa
com regras de negócio, fluxos de trabalho e detalhes das alterações.

---

## [Não lançado] — develop

### Correções

- **Copyright atualizado de 2025 para 2026** (01/03/2026)
  Corrigido em 5 templates: `public/fragments/layout.html`, `public/auth/login.html`,
  `public/auth/register.html`, `admin/auth/login.html` e `auth/login.html`.

- **Logo integrada na sidebar do layout admin** (01/03/2026)
  Substituído container quadrado 40×40px + textos redundantes por container branco
  de largura total com a logo em tamanho legível (`h-9`), respeitando o design horizontal
  da imagem e o contraste com o fundo vermelho da sidebar.

- **Logo real na tela de login admin** (01/03/2026)
  Removido header vermelho com emoji 🌭 de `admin/auth/login.html`. Substituído por
  seção branca com a logo centralizada (`h-14`) e subtítulo "Painel Administrativo".

- **Logo real substituída no layout admin** (01/03/2026)
  Removido placeholder com emoji 🌭 da sidebar do painel admin. Substituído por
  `<img src="/image/logo.png">` dentro do container branco existente.

---

## [0.3.0] — 28/02/2026

Storefront completo de ponta a ponta: catálogo público, carrinho, checkout e área da conta
do cliente. Segurança dual-chain com autenticação independente para admin e clientes. CRUD
de usuários administrativos com controle de acesso por role. Reestruturação de pacotes em
slicing vertical. 180 testes com 0 falhas.

→ [Documentação completa](docs/v0.3.0.md)

---

## [0.2.1] — 24/02/2026

Sem documentação disponível para versões anteriores à 0.3.0.

---

## [0.2.0] — 23/02/2026

Sem documentação disponível para versões anteriores à 0.3.0.

---

## [0.1.1] — 18/02/2026

Sem documentação disponível para versões anteriores à 0.3.0.

---

## [0.1.0] — 15/02/2026

Sem documentação disponível para versões anteriores à 0.3.0.
