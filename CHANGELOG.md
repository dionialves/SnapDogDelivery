# Changelog — SnapDog Delivery

Índice de versões lançadas. Cada versão a partir da **0.3.0** possui documentação completa
com regras de negócio, fluxos de trabalho e detalhes das alterações.

---

## [Não lançado] — develop

### Correções

- **Select de estado inoperante no cadastro e perfil do cliente** (01/03/2026)
  Substituído `th:value="${s.name()}"` por `th:value="${s}"` nas options do `<select>` de
  estado em `register.html` e `profile.html`. O Thymeleaf/Spring passa a gerenciar a
  conversão enum ↔ String automaticamente, corrigindo a seleção.

- **Conflito de sessão entre área admin e área pública corrigido** (01/03/2026)
  Configurado `HttpSessionSecurityContextRepository` com chave `"ADMIN_SECURITY_CONTEXT"` na
  cadeia admin via `.securityContext(sc -> sc.securityContextRepository(adminRepo))`. A cadeia
  pública mantém `SPRING_SECURITY_CONTEXT` (padrão). Login admin no mesmo browser não
  corrompe mais a sessão do cliente e vice-versa.

- **Dropdown do cliente exibindo nome em vez do e-mail** (01/03/2026)
  Criado `CustomerPrincipal` implementando `UserDetails` com `getUsername()` retornando o
  e-mail (Spring Security) e `getName()` retornando o nome real. `CustomerUserDetailsService`
  atualizado para retornar `CustomerPrincipal`. Template `public/fragments/layout.html`
  atualizado para usar `sec:authentication="principal.name"`.

- **Status do pedido exibido em PT-BR na área pública** (01/03/2026)
  Adicionado campo `label` PT-BR ao enum `OrderStatus` (Aguardando, Em preparo, Saiu para
  entrega, Entregue, Cancelado). Adicionado campo `statusLabel` no `OrderResponseDTO`.
  Templates `confirmation.html`, `account/orders.html` e `account/order-detail.html`
  atualizados para usar `${order.statusLabel}`.

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
