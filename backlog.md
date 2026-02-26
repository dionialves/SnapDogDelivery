# Backlog — Sistema Snap Dog Delivery

Documento de levantamento completo de funcionalidades pendentes para a versão 1.0.
Baseado na análise do código-fonte atual (fevereiro/2026).
Última atualização: fevereiro/2026.

---

## Sumário

1. [Área Pública — Novas Funcionalidades (v1.0)](#1-área-pública--novas-funcionalidades-v10)
2. [Área Administrativa — Novas Funcionalidades](#2-área-administrativa--novas-funcionalidades)
3. [Correções de Bugs e Dívidas Técnicas](#3-correções-de-bugs-e-dívidas-técnicas)
4. [Infraestrutura e Banco de Dados](#4-infraestrutura-e-banco-de-dados)
5. [Cobertura de Testes](#5-cobertura-de-testes)
6. [Refatoração — Desacoplamento de Autenticação Customer / User](#6-refatoração--desacoplamento-de-autenticação-customer--user)
7. [Funcionalidades Futuras (pós v1.0)](#7-funcionalidades-futuras-pós-v10)

---

## 1. Área Pública — Novas Funcionalidades (v1.0)

Toda a área pública é **inexistente** no projeto atual. O sistema hoje é exclusivamente
administrativo (rotas sob `/admin/**`). Esta seção descreve tudo que precisa ser criado
do zero para o lançamento da v1.0.

---

### 1.1 Autenticação de Clientes (Cadastro e Login)

> **CONCLUÍDO** (fevereiro/2026)

**Solução aplicada:**

- **Enum `Role`** — `CUSTOMER` adicionado
- **Entidade `User`** — campo `customer` (`@OneToOne`, nullable); admin users têm `customer = null`
- **`CustomerAuthController`** no pacote `domain/storefront/auth` (`@Controller`, rotas públicas):
  - `GET /register` → formulário de cadastro
  - `POST /register` → cria `Customer` + `User` (role `CUSTOMER`) vinculados, redireciona para `/login`
  - `GET /login` → formulário de login público (separado do admin `/admin/login`)
- **DTOs:** `CustomerRegisterDTO` (nome, e-mail, senha, telefone + endereço completo), `CustomerLoginDTO`
- **`SecurityConfig`** — duas cadeias de filtros (`@Order`):
  - Cadeia 1 (admin): `/admin/**` → `ADMIN` ou `SUPER_ADMIN`; `/admin/users/**` → `SUPER_ADMIN`
  - Cadeia 2 (público): `/`, `/catalog/**`, `/register`, `/login` → públicos; `/cart/**`, `/checkout/**`, `/account/**` → `CUSTOMER`
- **Templates:** `public/auth/register.html`, `public/auth/login.html`

---

### 1.2 Imagem de Produto

> **CONCLUÍDO** (fevereiro/2026) — Campos `imageUrl` e `active` adicionados à entidade `Product`, DTOs e formulário admin.

**Solução aplicada:**

- **Entidade `Product`** — adicionados campos:
  - `imageUrl` (`String`, nullable, max 500) — URL externa da imagem
  - `active` (`boolean`, default `true`) — controla visibilidade no catálogo público

- **`ProductDTO`** — campo `imageUrl` com `@Size(max = 500)` e campo `active`

- **`ProductResponseDTO`** — inclui `imageUrl` e `active` na resposta

- **Formulário admin (`admin/products/form.html`)** — campo de URL de imagem com preview e checkbox de ativo/inativo

**1.2.1 — Upload de arquivo (fase seguinte ao imageUrl):**

> **PARCIALMENTE CONCLUÍDO** (fevereiro/2026) — backend de upload implementado; campo no formulário HTML ainda pendente.

**Solução aplicada no backend:**

- **`StorageService`** (`infra/storage`) — salva `MultipartFile` em diretório configurável (`app.upload.dir`, padrão `uploads/products/`); valida extensão (jpg, jpeg, png, webp, gif); retorna URL relativa `/uploads/products/uuid.ext`
- **`ProductViewController`** — aceita `@RequestParam MultipartFile image`
- **`WebMvcConfig`** — registra `ResourceHandler` servindo `/uploads/**` do diretório local

**Ainda pendente:**

- Campo `<input type="file">` no formulário admin `admin/products/form.html` como alternativa à URL externa
- JavaScript para alternar entre os dois modos (upload de arquivo vs. URL externa)

---

### 1.3 Catálogo Público de Produtos

> **CONCLUÍDO** (fevereiro/2026)

**Solução aplicada:**

- **Pacote `domain/storefront/store`**
  - `StoreController` (`@Controller`) — delega para `ProductService` e `CartService`
- **`ProductService`** — adicionados `findAllActive(Pageable)` e `findFeatured()` (top 6 por nome)
- **Endpoints:**
  - `GET /` → landing page com produtos em destaque (`findFeatured()`)
  - `GET /catalog` → grid paginado de produtos ativos
  - `GET /catalog/{id}` → detalhe do produto com botão "Adicionar ao carrinho"
- **Templates:** `public/index.html`, `public/store/catalog.html`, `public/store/product-detail.html`

---

### 1.4 Carrinho de Compras

> **CONCLUÍDO** (fevereiro/2026)

**Solução aplicada:**

- **`Cart`** — POJO `Serializable` armazenado na `HttpSession`; `Map<Long, CartItem>` com métodos `addItem`, `removeItem`, `updateQuantity`, `getTotal`, `clear`
- **`CartItem`** — POJO `Serializable`; campos `productId`, `productName`, `imageUrl`, `unitPrice`, `quantity`; método `getSubtotal()`
- **Pacote `domain/storefront/cart`**
  - `CartService` — gerencia `Cart` na `HttpSession` (chave `"cart"`); inclui `getItemCount()` seguro para páginas públicas
  - `CartController` (`@Controller`, `/cart/**`) — protegido por role `CUSTOMER`
- **Endpoints implementados:** `GET /cart`, `POST /cart/add`, `POST /cart/remove/{productId}`, `POST /cart/update/{productId}`, `POST /cart/clear`
- **Template:** `public/cart/cart.html`
- **Header público** exibe contador de itens do carrinho (lido da sessão via `CartService.getItemCount()`)

---

### 1.5 Checkout e Finalização do Pedido

> **CONCLUÍDO** (fevereiro/2026)

**Solução aplicada:**

- **Entidade `Order`:**
  - ✅ Campo `origin` (enum `OrderOrigin`: `ONLINE`, `MANUAL`, default `MANUAL`)
  - ✅ Campo `deliveryAddress` (`String`, nullable) — snapshot do endereço no momento do pedido

- **Pacote `domain/storefront/checkout`**
  - `CheckoutService` — valida carrinho não vazio, busca `Customer` vinculado ao `User` autenticado, monta `OrderCreateDTO`, registra `deliveryAddress` como snapshot, delega para `OrderService.create()`, limpa carrinho após sucesso
  - `CheckoutController` (`@Controller`, `/checkout/**`) — protegido por role `CUSTOMER`

- **Endpoints implementados:**
  - `GET /checkout` → tela de revisão (resumo do carrinho + endereço de entrega)
  - `POST /checkout/confirm` → cria o pedido, redireciona para confirmação
  - `GET /checkout/confirmation/{orderId}` → tela de sucesso com número e status do pedido

- **Templates:** `public/checkout/review.html`, `public/checkout/confirmation.html`

---

### 1.6 Área do Cliente — My Account

> **CONCLUÍDO** (fevereiro/2026)

**Solução aplicada:**

- **Pacote `domain/storefront/account`**
  - `AccountController` (`@Controller`, `/account/**`) — protegido por role `CUSTOMER`

- **Endpoints implementados:**
  - `GET /account` → painel com pedidos recentes (`OrderService.findRecentByCustomerId()`)
  - `GET /account/orders` → histórico completo paginado
  - `GET /account/orders/{id}` → detalhe do pedido (produtos, status, valor total)
  - `GET /account/profile` → formulário de edição de dados cadastrais
  - `POST /account/profile` → salva alterações via `CustomerService.update()`

- **Templates:** `public/account/dashboard.html`, `public/account/orders.html`, `public/account/order-detail.html`, `public/account/profile.html`
- **`OrderService`** — adicionados `findByCustomerId(Long, Pageable)` e `findRecentByCustomerId(Long)`

---

### 1.7 Layout Público (Brand Snap Dog)

> **CONCLUÍDO** (fevereiro/2026)

**Solução aplicada:**

- `templates/public/fragments/layout.html` — layout base com:
  - Header: logo Snap Dog, link "Menu" (`/catalog`), ícone de carrinho com contador de itens (via `CartService.getItemCount()`), menu do usuário autenticado (nome + "Sign out") ou botões "Sign in" / "Register"
  - Footer com informações da loja
  - Stack: Tailwind CDN + Lucide Icons (mesmos assets do painel admin)

- `templates/public/index.html` — landing page (`GET /`):
  - Hero com chamada para ação ("Ver cardápio")
  - Seção de produtos em destaque (top 6 via `ProductService.findFeatured()`) — visível sem login
  - Acesso ao catálogo completo sem autenticação; ação de compra exige login

---

### 1.8 Modal de Quantidade ao Adicionar ao Carrinho

**Arquivos:** `templates/public/store/catalog.html`, `templates/public/store/product-detail.html`

**Problema:** O botão "Adicionar" no catálogo posta diretamente com `quantity=1` fixo — o
cliente não tem como escolher a quantidade antes de enviar.

**Solução:**

- Substituir o `<form>` de POST direto no card do catálogo por um botão que abre um modal JS
- O modal deve exibir: imagem do produto, nome, descrição e campo numérico de quantidade
  (min 1, max 20, padrão 1)
- Botão "Adicionar ao carrinho" dentro do modal submete o POST `/cart/add` com o `productId`
  e a `quantity` selecionada
- O modal deve ser acessível via teclado (fechar com `Esc`, foco retorna ao botão de origem)
- Implementado inteiramente com JavaScript vanilla + Tailwind — sem dependências externas
- A lógica de abertura do modal pode ser centralizada em um `<template>` reutilizável no
  layout ou inline no template do catálogo

> ⏳ **Pendente**

---

### 1.9 Máscaras de Telefone e CEP no Cadastro de Cliente

**Arquivo:** `templates/public/auth/register.html`

**Problema:** Os campos `phone` e `zipCode` não possuem máscara de entrada — o cliente pode
digitar no formato errado e só descobre o erro ao submeter o formulário (validação Jakarta).

**Solução:**

- Aplicar máscara JavaScript no campo `#phone` → formato `(00) 00000-0000`
- Aplicar máscara JavaScript no campo `#zipCode` → formato `00000-000`
- Implementar com JavaScript vanilla (evento `input` + regex) — sem bibliotecas externas
- A máscara deve ser aplicada também no formulário de edição de perfil
  (`templates/public/account/profile.html`)

> ⏳ **Pendente**

---

### 1.10 Auto-preenchimento de Endereço por CEP (ViaCEP)

**Arquivos:** `templates/public/auth/register.html`, `templates/public/account/profile.html`

**Problema:** O cliente precisa preencher manualmente rua, bairro, cidade e estado — dado
que todos esses campos podem ser obtidos automaticamente a partir do CEP.

**Solução:**

- Quando o campo `#zipCode` perder o foco (`blur`) e tiver 8 dígitos numéricos, disparar
  `fetch('https://viacep.com.br/ws/{cep}/json/')` (API pública, sem chave)
- Em caso de sucesso, preencher automaticamente: `street` (`logradouro`), `neighborhood`
  (`bairro`), `city` (`localidade`), `state` (converter `uf` para o enum `State`)
- Exibir um spinner discreto no campo CEP enquanto a requisição está em andamento
- Exibir mensagem de erro inline caso o CEP não seja encontrado (status `"erro": true` na
  resposta da API)
- Campos preenchidos automaticamente devem permanecer editáveis pelo cliente
- Implementado com JavaScript vanilla — sem dependências externas

> ⏳ **Pendente**

---

## 2. Área Administrativa — Novas Funcionalidades

---

### 2.1 Gerenciamento de Usuários Admin

> **CONCLUÍDO** (fevereiro/2026)

**Solução aplicada:**

- **`UserService`** — `findAll`, `findById`, `create`, `update`, `delete` (guard contra auto-exclusão)
- **`UserController`** (`@RestController`, `/admin/api/users`) — endpoints JSON
- **`UserViewController`** (`@Controller`, `/admin/users`) — CRUD completo via Thymeleaf:
  - `GET /admin/users` → lista paginada
  - `GET /admin/users/new` → formulário de criação
  - `POST /admin/users/new` → cria usuário
  - `GET /admin/users/{id}` → formulário de edição
  - `POST /admin/users/{id}` → atualiza usuário
  - `POST /admin/users/{id}/delete` → remove usuário
- **DTOs:** `UserCreateDTO`, `UserUpdateDTO`, `UserResponseDTO` (sem senha)
- **Templates:** `admin/users/list.html` e `admin/users/form.html`
- **Sidebar** — link "Users" adicionado ao `layout.html`, visível apenas para `SUPER_ADMIN`
- **`UserDTO.java` legado excluído** — não era usado por nenhum controller

---

### 2.2 Controle de Acesso por Role

> **CONCLUÍDO** (fevereiro/2026)

**Solução aplicada:**

- **`SecurityConfig`** — `hasRole()` aplicado nas rotas admin:
  - `/admin/users/**` → apenas `SUPER_ADMIN`
  - `/admin/**` (restante) → `ADMIN` ou `SUPER_ADMIN`
- **Role `CUSTOMER`** adicionada à enum `Role`
- **Templates** — `sec:authorize` aplicado via `thymeleaf-extras-springsecurity6`:
  - Link "Users" no sidebar visível apenas para `SUPER_ADMIN`
- **`thymeleaf-extras-springsecurity6`** adicionado ao `pom.xml`

---

### 2.3 Campo de Imagem no Formulário Admin de Produto

> **PARCIALMENTE CONCLUÍDO** (fevereiro/2026) — campo `imageUrl` com preview + backend de upload implementados; campo `<input type="file">` no HTML ainda pendente (ver 1.2.1).

**Solução aplicada:**

- `templates/admin/products/form.html` — campo de texto para `imageUrl` com preview em tempo real e checkbox para `active`
- `StorageService` + `ProductViewController` — suporte a upload de `MultipartFile` já no backend
- `WebMvcConfig` — arquivos enviados servidos via `/uploads/**`

**Ainda pendente:**

- Campo `<input type="file">` no formulário admin como alternativa à URL externa
- JavaScript para alternar entre os dois modos

---

### 2.4 Campo `origin` Visível na Área Admin

> **PARCIALMENTE CONCLUÍDO** (fevereiro/2026) — `OrderOrigin` implementado no backend; templates ainda pendentes.

**Solução aplicada:**

- Enum `OrderOrigin` (`ONLINE`, `MANUAL`) criada
- Campo `origin` adicionado à entidade `Order`, `OrderCreateDTO` e `OrderResponseDTO`
- `OrderService.create()` aplica `MANUAL` como default quando `origin` não é informado

**Ainda pendente:**

- `templates/admin/orders/list.html` — coluna ou badge "Origem" (Online / Manual)
- `templates/admin/orders/form.html` — exibir origin na visualização do pedido
- Filtro opcional por origin na listagem

---

### 2.5 Campo `active` no Gerenciamento de Produtos

> **CONCLUÍDO** (fevereiro/2026)

**Solução aplicada:**

- `templates/admin/products/list.html` — coluna de status (Ativo/Inativo) com badge visual
- `templates/admin/products/form.html` — checkbox "Exibir no catálogo público"
- `ProductService.update()` — processa o campo `active`

---

### 2.7 Crédito do Criador no Footer Público

**Arquivo:** `templates/public/fragments/layout.html`, linha ~187

**Problema:** O rodapé da área pública exibe apenas o copyright da marca, sem identificar
o desenvolvedor responsável pelo sistema.

**Solução:**

- Adicionar abaixo (ou ao lado) do copyright a linha:
  `Desenvolvido por <a href="https://www.dionialves.com">Dioni Alves</a>`
- Link deve abrir em nova aba (`target="_blank" rel="noopener"`)
- Estilo discreto, alinhado com o visual atual do footer (texto `xs`, cor `gray-400`)

> ⏳ **Pendente**

---

## 3. Correções de Bugs e Dívidas Técnicas

---

### 3.1 `System.out.println` em Código de Produção

> **CONCLUÍDO** (fevereiro/2026)

**Solução aplicada:** Removidos todos os debug prints:

| Arquivo | O que foi removido |
|---|---|
| `ClientViewController.java` | `System.out.println(model.asMap())` |
| `OrderService.java` | `System.out.println(saved)` |
| `GlobalExceptionHandler.java` | Dois prints no handler genérico de `Exception` |

---

### 3.2 Flash Messages Nunca Exibem (Pedidos, Produtos e Clientes)

> **CONCLUÍDO** (fevereiro/2026)

**Contexto expandido:** O problema era mais amplo do que o descrito originalmente — além de
produtos, os templates de pedidos e clientes também tinham chaves inconsistentes com os
seus respectivos controllers.

**Solução aplicada:** Padronização completa para `successMessage` / `errorMessage` em todos
os módulos:

| Arquivo | Correção |
|---|---|
| `orders/list.html` | `${message}` + `messageType` → `${successMessage}` / `${errorMessage}` |
| `orders/form.html` | Adicionado bloco `successMessage` (só existia `errorMessage`) |
| `clients/list.html` | `${messagem}` + `messageType` → `${successMessage}` / `${errorMessage}` |
| `products/list.html` | `${messagem}` + `messageType` → `${successMessage}` / `${errorMessage}` |
| `ClientViewController.java` | Flash attributes migrados de `messagem`/`messageType` para `successMessage`/`errorMessage` |

`ProductViewController` e `OrderViewController` já enviavam as chaves corretas — não precisaram de alteração.

---

### 3.3 Typo na URL do Botão "Voltar" em Produtos

> **CONCLUÍDO** (fevereiro/2026)

**Solução aplicada:** Corrigido `/admin/produts` → `/admin/products` em `products/form.html`.
Aproveitado para corrigir também a chave de flash message do formulário: `${erro}` → `${errorMessage}` /
`${successMessage}`, alinhando com o padrão do restante do projeto.

---

### 3.4 `@Transactional` Faltando em Métodos de Serviço

> **CONCLUÍDO** (fevereiro/2026)

**Solução aplicada:** `@Transactional` adicionado nos quatro métodos:

| Arquivo | Método |
|---|---|
| `ClientService.java` | `create(ClientDTO)` |
| `ClientService.java` | `delete(Long)` |
| `ProductService.java` | `create(ProductDTO)` |
| `ProductService.java` | `delete(Long)` |

---

### 3.5 Conversão de Enum Vazia em `OrderViewController`

> **CONCLUÍDO** (fevereiro/2026)

**Solução aplicada:** `@RequestParam(defaultValue = "") OrderStatus status` corrigido para
`@RequestParam(required = false) OrderStatus status` em `OrderViewController.findAll()`.
`OrderSpecifications.hasStatus` já tratava `null` como "sem filtro" — nenhuma alteração
necessária no serviço.

---

### 3.6 `BusinessException` Renderiza Template `error/500`

> **CONCLUÍDO** (fevereiro/2026)

**Solução aplicada:**
- Criado `templates/error/400.html` com visual adequado para erros de regra de negócio
  (título "Operação não permitida", mensagem dinâmica via `${message}`, botões "Voltar" e "Dashboard")
- `GlobalExceptionHandler.handleBusiness()` atualizado para apontar para `error/400` com `status=400`
- Removidos `System.out.println` remanescentes de `handleGeneric()` (bug 3.1 que não havia sido
  corrigido no rollback)

---

### 3.7 Formulário de Pedido Existente Posta para Rota Errada

> **CONCLUÍDO** (fevereiro/2026)

**Solução aplicada:** `th:action` tornado dinâmico em `orders/form.html`:
```html
th:action="${order != null and order.id != null}
    ? @{/admin/orders/{id}(id=${order.id})}
    : @{/admin/orders/new}"
```

---

### 3.8 Métodos Duplicados (Código Morto) nos Serviços

> **CONCLUÍDO** (fevereiro/2026)

**Solução aplicada:** Métodos duplicados removidos:

| Arquivo | Método removido |
|---|---|
| `CustomerService.java` | `searchByNameOrPhone(String)` — duplicava `search(String)` |
| `ProductService.java` | `searchByName(String)` — duplicava `search(String)` |

---

### 3.9 CSRF Desabilitado

> **CONCLUÍDO** (fevereiro/2026)

**Solução aplicada:** Removida a linha `http.csrf(csrf -> csrf.disable())` de `SecurityConfig.java`.
Todos os formulários de mutação já usavam `th:action` (Thymeleaf injeta o token automaticamente).
Os `fetch()` existentes são exclusivamente `GET` para APIs de busca — nenhum envia dados de escrita,
portanto não requerem o header `X-CSRF-TOKEN`.

---

### 3.10 Logo Placeholder no Layout Admin

**Arquivo:** `templates/admin/fragments/layout.html`, linha ~54

**Problema:** Comentário `<!-- Logo placeholder - depois substituímos pelo SVG real -->`
com emoji no lugar do logo.

**Solução:** Criar/adicionar o SVG ou imagem real do logo Snap Dog.

> ⏳ **Pendente** — requer asset de logo (SVG/imagem) que ainda não existe no projeto.

---

### 3.11 Renomeação do Domínio `Client` → `Customer`

> **CONCLUÍDO** (fevereiro/2026)

**Contexto:** O domínio de clientes foi originalmente nomeado `Client` (entidade, serviço,
controller, repositório, DTO e testes). Para consistência com a nomenclatura do projeto
(rotas `/admin/customers`, tabela `tb_customers`) e com a área pública (role `CUSTOMER`,
`CustomerAuthController`), todo o domínio foi renomeado.

**Solução aplicada:**

| Arquivo antigo | Arquivo novo |
|---|---|
| `Client.java` | `Customer.java` |
| `ClientService.java` | `CustomerService.java` |
| `ClientController.java` | `CustomerController.java` |
| `CustomerViewController.java` | `CustomerViewController.java` (já correto) |
| `ClientRepository.java` | `CustomerRepository.java` |
| `ClientDTO.java` | `CustomerDTO.java` |
| `ClientServiceTest.java` | `CustomerServiceTest.java` |
| `ClientControllerTest.java` | `CustomerControllerTest.java` |
| `ClientRepositoryTest.java` | `CustomerRepositoryTest.java` |

- Pacote renomeado: `client/` → `customer/`
- Todos os imports, referências cruzadas, rotas e templates atualizados
- `AGENTS.md`, `backlog.md` e `README.md` corrigidos para refletir os novos nomes
- IDs HTML e variáveis JavaScript em `orders/form.html` renomeados (`clientSearch` → `customerSearch` etc.)

---

### 3.12 Variáveis em Português e Mensagens de API em Inglês

> **CONCLUÍDO** (fevereiro/2026) — commit `0e1258d` em `develop`.

**Problema:** Identificadores Java em português (`salved`, `emailAtual`, `criarPedido`, `joao`, `maria`,
`criarClient` etc.) misturados com mensagens de API totalmente em inglês (`"Client not found with ID"`,
`"Order must have at least one product"`, `"CLient ID is madatory"` — com typos).

**Solução aplicada:**

| Arquivo | Alteração |
|---|---|
| `CustomerService.java` | `salved` → `saved`; 3 mensagens de `NotFoundException` e 1 de `BusinessException` traduzidas |
| `CustomerDTO.java` | 21 mensagens de validação Jakarta traduzidas para PT-BR |
| `OrderService.java` | 5 mensagens de `NotFoundException` e 3 de `BusinessException` traduzidas |
| `OrderCreateDTO.java` | Typos `"CLient ID is madatory"` corrigidos; 3 mensagens traduzidas |
| `ProductService.java` | 3 mensagens de `NotFoundException` traduzidas |
| `ProductDTO.java` | 3 mensagens de validação traduzidas |
| `ProductOrderDTO.java` | 4 mensagens de validação traduzidas |
| `GlobalExceptionHandler.java` | 6 strings de resposta traduzidas |
| `CustomUserDetailsService.java` | Mensagem `"User not found with email"` traduzida |
| `UserService.java` | `emailAtual` → `currentEmail` |
| `UserDTO.java` | **Excluído** (legado sem uso em nenhum controller) |
| `CustomerRepositoryTest.java` | `joao`/`maria` → `customerJohn`/`customerMary`; `criarClient` → `createCustomer`; parâmetros `nome`/`telefone` → `name`/`phone` |
| `OrderRepositoryTest.java` | `criarPedido` → `createOrder`; `criarPedidoComProduto` → `createOrderWithProduct`; `quantidade` → `quantity` |
| `OrderViewControllerTest.java` | `pedidoResponseDTO` → `orderResponseDTO` |
| `DashboardServiceTest.java` | `criarClient` → `createClient`; `criarOrder` → `createOrder` |
| `OrderServiceTest.java` | Asserção `"PENDING"` → `"PENDENTE"` (consequência da tradução da mensagem) |

---

### 3.13 Status do Pedido Exibido em Inglês na Tela de Confirmação

**Arquivo:** `templates/public/checkout/confirmation.html`, linha 32

**Problema:** `th:text="${order.status}"` imprime o nome literal do enum (`PENDING`,
`PREPARING` etc.) em inglês para o cliente.

**Solução:**

- Adicionar campo `label` em português à enum `OrderStatus`:
  `PENDING("Aguardando"), PREPARING("Em preparo"), OUT_FOR_DELIVERY("Saiu para entrega"),
  DELIVERED("Entregue"), CANCELED("Cancelado")`
- Atualizar `confirmation.html` para usar `${order.status.label}` (ou equivalente via DTO)
- Verificar e atualizar todos os demais templates públicos que exibam `order.status`
  diretamente (`account/orders.html`, `account/order-detail.html`)
- O campo `status` nos DTOs de resposta da API admin pode manter o nome do enum — não
  afeta o cliente

> ⏳ **Pendente**

---

### 3.14 Dropdown do Cliente Exibe E-mail em Vez do Nome

**Arquivo:** `templates/public/fragments/layout.html`, linha 83

**Problema:** `sec:authentication="name"` retorna o `username` do `UserDetails` — que no
`CustomerUserDetailsService` é o e-mail. O dropdown exibe `joao@email.com` em vez de
`João Silva`.

**Solução:**

- Injetar o nome do cliente autenticado no modelo via `@ControllerAdvice` ou
  `@ModelAttribute` nos controllers da área pública que usam o layout
- Substituir `sec:authentication="name"` por `th:text="${customerName}"` (ou equivalente)
- Alternativa mais limpa: implementar `CustomerPrincipal` que encapsula `Customer`
  e expõe `getName()` com o nome real, mantendo `getUsername()` retornando o e-mail para o
  Spring Security

> ⏳ **Pendente**

---

### 3.15 Conflito de Sessão entre Área Admin e Área Pública

**Contexto:** No mesmo navegador, ao logar no painel admin (`/admin/login`) e em seguida
acessar a área pública, a sessão do cliente é corrompida — o Spring Security lê o
`SecurityContext` da sessão compartilhada e encontra credenciais de admin, quebrando o fluxo
do cliente.

**Causa raiz:** As duas `SecurityFilterChain` compartilham o mesmo atributo de
`SecurityContext` no `HttpSession` (`SPRING_SECURITY_CONTEXT`). A sessão admin sobrescreve
o contexto esperado pela cadeia pública.

**Solução:**

- Configurar `HttpSessionSecurityContextRepository` com atributo de sessão distinto para
  cada cadeia:
  - Cadeia admin: `setSpringSecurityContextAttrName("ADMIN_SECURITY_CONTEXT")`
  - Cadeia pública: mantém atributo padrão `SPRING_SECURITY_CONTEXT`
- Isso garante que os dois contextos de autenticação coexistam no mesmo browser sem
  interferência mútua

> ⏳ **Pendente**

---

### 3.16 Copyright com Ano Desatualizado (2025)

**Problema:** Todos os rodapés exibem `© 2025 SnapDog Delivery`.

| Arquivo | Ocorrência |
|---|---|
| `templates/public/fragments/layout.html` | linha ~187 |
| `templates/public/auth/register.html` | linha ~228 |
| `templates/public/auth/login.html` | painel esquerdo — rodapé |
| `templates/admin/auth/login.html` | rodapé |

**Solução:** Atualizar para `© 2026 SnapDog Delivery` em todos os arquivos listados.

> ⏳ **Pendente**

---

## 4. Infraestrutura e Banco de Dados

---

### 4.1 `ddl-auto=create-drop` em Produção

**Arquivo:** `src/main/resources/application.properties`, linha 5

**Problema crítico:** `spring.jpa.hibernate.ddl-auto=create-drop` destrói e recria
todas as tabelas a cada restart da aplicação. **Todos os dados são perdidos.**

**Solução:**
1. Alterar para `validate` em produção (Hibernate valida o schema sem modificar)
2. Adicionar **Flyway** para gerenciar migrations:

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

3. Criar scripts de migration em `src/main/resources/db/migration/`:
   - `V1__create_initial_schema.sql` — DDL completo das tabelas atuais
   - `V2__add_product_image_url.sql` — campo `image_url` em `tb_products`
   - `V3__add_product_active.sql` — campo `active` em `tb_products`
   - `V4__add_order_origin.sql` — campo `origin` em `tb_orders`
   - `V5__add_order_delivery_address.sql` — campo `delivery_address` em `tb_orders`
   - `V6__add_user_client_fk.sql` — campo `client_id` (FK nullable) em `tb_users`
   - (migrations seguintes conforme novas features)

---

### 4.2 Nome da Classe Principal

> **CONCLUÍDO** (fevereiro/2026)

**Arquivo:** ~~`GreendogdeliveryApplication.java`~~ → `SnapdogDeliveryApplication.java`

**Problema:** O nome da classe ainda refletia o nome antigo do projeto ("Greendog").

**Solução aplicada:** Renomeados `GreendogdeliveryApplication.java` → `SnapdogDeliveryApplication.java`
e `GreendogdeliveryApplicationTests.java` → `SnapdogDeliveryApplicationTests.java`.
Nenhuma referência encontrada em `application.properties`.

---

### 4.3 Senha Padrão no `application.properties`

> **CONCLUÍDO** (fevereiro/2026)

**Problema:** Credenciais de banco (`postgresadmin`) hardcoded no arquivo versionado.

**Solução aplicada:** Separação em três profiles:

| Profile | Arquivo | Uso | Versionado |
|---|---|---|---|
| `dev` (padrão) | `application-dev.properties` | Desenvolvimento local | Sim |
| `prod` | `application-prod.properties` | Servidor / produção | **Não** (`.gitignore`) |
| `test` | `application-test.yml` | Testes automatizados (H2) | Sim |

- `application.properties` mantém apenas configurações comuns e define `spring.profiles.active=dev`
- `application-prod.properties` usa variáveis de ambiente: `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}`
- `application-prod.properties.example` versionado como referência para o time
- Ativar produção com `--spring.profiles.active=prod` ou `SPRING_PROFILES_ACTIVE=prod`

---

## 5. Cobertura de Testes

> **Suite admin + auth desacoplamento: CONCLUÍDA** (fevereiro/2026) — **114 testes**, 0 falhas, BUILD SUCCESS.
> **Suite storefront: ⏳ Pendente** — as funcionalidades 1.3–1.6 foram implementadas mas ainda não possuem testes automatizados.
>
> **Evolução da suite:**
> - Entrega inicial: 76 testes (serviços, controllers, repositórios — domínio admin)
> - Adicionados 25 testes para o módulo `user` (`UserServiceTest`, `UserControllerTest`, `UserViewControllerTest`)
> - Renomeação de variáveis/métodos auxiliares em PT-BR para inglês (fevereiro/2026)
> - Adicionados 13 testes para o desacoplamento de autenticação (seção 6): `CustomerUserDetailsServiceTest`, `AdminAuthControllerTest`, `UserRoleTest`; expansão de `CustomerServiceTest` e `CustomerRepositoryTest`
>
> **Infraestrutura corrigida junto com a entrega inicial:**
> - `annotationProcessorPaths` do Lombok movido para `maven-compiler-plugin` no `pom.xml`
> - `spring-security-test` adicionado ao `pom.xml`
> - `@Profile("!test")` adicionado ao `DataSeeder` para evitar dados de seed no H2
> - `NotFoundException` passa a estender `BusinessException`

---

### 5.1 Testes de Serviço (Unitários)

| Classe de Teste | Status | Testes |
|---|---|---|
| `CustomerServiceTest` | ✅ Concluído | 8 — `create`, `update`, `delete` (com e sem pedidos), `search` paginado |
| `ProductServiceTest` | ✅ Concluído | 9 — `create`, `update`, `delete`, `search` |
| `OrderServiceTest` | ✅ Concluído | 16 — `create` (fluxo completo), `updateStatus` (todas as transições válidas e inválidas), `delete` |
| `DashboardServiceTest` | ✅ Concluído | 7 — `getDashboardSummary` (mocks de repositório), cálculo de crescimento |
| `UserServiceTest` | ✅ Concluído | 12 — `create`, `update`, `delete` (guard de auto-exclusão), `findById`, `findAll` |
| `CartServiceTest` | ⏳ Pendente | `addItem`, `removeItem`, `updateQuantity`, `clear`, `getItemCount` |
| `CheckoutServiceTest` | ⏳ Pendente | Fluxo completo: carrinho → pedido; validações de carrinho vazio e cliente sem vínculo |

---

### 5.2 Testes de Controller (Integração)

| Classe de Teste | Status | Testes |
|---|---|---|
| `CustomerControllerTest` | ✅ Concluído | 3 — `GET /admin/api/customers/search`: com resultado, termo vazio, sem resultado |
| `ProductControllerTest` | ✅ Concluído | 3 — `GET /admin/api/products/search`: com resultado, termo vazio, sem resultado |
| `OrderViewControllerTest` | ✅ Concluído | 13 — listagem, criação, atualização de status, exclusão (sucessos e erros) |
| `UserControllerTest` | ✅ Concluído | 3 — `GET /admin/api/users`: listagem paginada, sem usuários, página inválida |
| `UserViewControllerTest` | ✅ Concluído | 10 — listagem, criação, edição, exclusão (sucessos e erros) |
| `StoreControllerTest` | ⏳ Pendente | `GET /`, `GET /catalog`, `GET /catalog/{id}` — listagem pública, produto não encontrado |
| `CartControllerTest` | ⏳ Pendente | Add, remove, update, clear — autenticado e sem autenticação (redirect para `/login`) |
| `CheckoutControllerTest` | ⏳ Pendente | Revisão, confirmação, carrinho vazio, cliente sem endereço |
| `AccountControllerTest` | ⏳ Pendente | Dashboard, histórico de pedidos, detalhe, edição de perfil |

---

### 5.3 Testes de Repositório

| Classe de Teste | Status | Testes |
|---|---|---|
| `OrderRepositoryTest` | ✅ Concluído | 6 — `sumRevenueByCreatedAtBetween`, `findTopSellingProducts`, `existsByCustomerId` |
| `CustomerRepositoryTest` | ✅ Concluído | 8 — `findByNameContainingIgnoreCaseOrPhoneContaining`, `countByCreatedAt` |

---

## 6. Refatoração — Desacoplamento de Autenticação Customer / User

> **CONCLUÍDO** (fevereiro/2026)

**Contexto:** Hoje o sistema possui um único mecanismo de autenticação que atende tanto o
painel admin quanto a área pública do cliente. Um `Customer` só consegue se autenticar porque
tem um registro vinculado na tabela de usuários admin (`tb_users`), com a role `CUSTOMER`.
Isso cria um acoplamento indevido: a entidade de cliente carrega dependência de uma estrutura
pensada para usuários administrativos.

**Objetivo:** Separar completamente os dois fluxos de autenticação. O `Customer` passa a ter
suas próprias credenciais e seu próprio mecanismo de login, acessível em `/login`. O `User`
fica restrito ao painel administrativo, autenticando exclusivamente em `/admin/login`.
Cada contexto terá seu próprio serviço de autenticação, sua própria cadeia de segurança e
sua própria tela de login — sem nenhum compartilhamento entre os dois.

---

### 6.1 Credenciais direto na entidade Customer

> **CONCLUÍDO** (fevereiro/2026)

**Solução aplicada:**
- `password` (`String`, NOT NULL, max 255) e `active` (`boolean`, default `true`) adicionados à entidade `Customer`
- `CustomerDTO` atualizado para expor `active`; `password` nunca é exposto
- `DataSeeder` atualizado: todos os 10 clientes seed recebem senha `"cliente123"` encodada em BCrypt e `active = true`

---

### 6.2 Serviço de autenticação exclusivo para Customer

> **CONCLUÍDO** (fevereiro/2026)

**Solução aplicada:**
- `CustomerUserDetailsService` criado em `domain/storefront/auth`; consulta `CustomerRepository.findByEmail()`
- Lança `UsernameNotFoundException` se e-mail não encontrado; `DisabledException` se `active = false`
- Atribui authority `ROLE_CUSTOMER`
- `CustomerRepository` — adicionados `findByEmail(String)` e `existsByEmail(String)`
- `CustomUserDetailsService` mantido sem alterações

---

### 6.3 Separação das cadeias de segurança com providers dedicados

> **CONCLUÍDO** (fevereiro/2026)

**Solução aplicada:**
- Cadeia 1 (admin): `securityMatcher("/admin/**")` + `DaoAuthenticationProvider(CustomUserDetailsService)` — login em `/admin/login`, logout redireciona para `/admin/login`
- Cadeia 2 (público): `DaoAuthenticationProvider(CustomerUserDetailsService)` — login em `/login`, logout redireciona para `/`
- Cada cadeia injeta seu próprio `AuthenticationProvider` explicitamente

---

### 6.4 Atualização do fluxo de cadastro de Customer

> **CONCLUÍDO** (fevereiro/2026)

**Solução aplicada:**
- `CustomerAuthController` reescrito: cria apenas `Customer` com `password` (BCrypt) e `active = true`
- Verificação de e-mail duplicado usa `CustomerRepository.existsByEmail()`
- Dependência de `UserRepository` e do domínio `user` removida do controller
- `CustomerLoginDTO` removido

---

### 6.5 Controller e template de login do admin

> **CONCLUÍDO** (fevereiro/2026)

**Solução aplicada:**
- `AdminAuthController` criado em `domain/admin/auth`; serve `GET /admin/login`; adiciona `error`/`message` ao model
- Template `templates/auth/login.html` (legado) copiado para `templates/admin/auth/login.html`
- Formulário aponta para `POST /admin/login`

---

### 6.6 Limpeza da entidade User e da enum Role

> **CONCLUÍDO** (fevereiro/2026)

**Solução aplicada:**
- Campo `customer` (`@OneToOne`) e coluna `customer_id` removidos da entidade `User`
- `CUSTOMER` removido da enum `Role` — agora contém apenas `USER`, `ADMIN`, `SUPER_ADMIN`
- `AccountController`, `CheckoutController` e `CheckoutService` refatorados para buscar `Customer` via `CustomerRepository.findByEmail()` em vez de `user.getCustomer()`
- Formulário admin de usuários exclui `CUSTOMER` automaticamente (usa `Role.values()` dinamicamente)

---

### 6.7 Tela de login personalizada para Customer

> **CONCLUÍDO** (fevereiro/2026)

**Solução aplicada:**
- `templates/public/auth/login.html` redesenhado com layout split (`min-h-screen flex`)
- Painel esquerdo (gradiente vermelho da marca): logo, slogan "O melhor hot dog da cidade, na sua porta", 3 benefícios com ícones Lucide (entrega rápida, ingredientes frescos, acompanhe seu pedido), rodapé; oculto em mobile (`hidden md:flex`)
- Painel direito (branco): logo mobile-only, campos e-mail/senha com ícone prefixado, link "Esqueceu a senha?" (desabilitado), divisor, link para cadastro
- Página standalone sem layout compartilhado; `lucide.createIcons()` no fim do body

---

### 6.8 Cobertura de testes para o desacoplamento

> **CONCLUÍDO** (fevereiro/2026) — suite total: **114 testes**, 0 falhas, BUILD SUCCESS.

**Solução aplicada:**

| Classe | Novos testes |
|---|---|
| `CustomerUserDetailsServiceTest` | 3 — cliente ativo, e-mail não encontrado, cliente inativo |
| `AdminAuthControllerTest` | 3 — GET sem parâmetros, com `?error`, com `?logout` |
| `CustomerServiceTest` | +1 — `findById` verifica campo `active` no DTO |
| `UserRoleTest` | 2 — `Role` não contém `CUSTOMER`; contém exatamente USER/ADMIN/SUPER_ADMIN |
| `CustomerRepositoryTest` | +4 — `findByEmail` (existente e inexistente), `existsByEmail` (true e false) |

---

## 7. Funcionalidades Futuras (pós v1.0)

Itens identificados como desejáveis mas fora do escopo imediato do lançamento.

| Funcionalidade | Descrição |
|---|---|
| **Login com Google (OAuth2)** | `spring-boot-starter-oauth2-client` + configuração no Google Cloud Console. Cria/vincula `Customer` automaticamente ao `User`. |
| **Campo `notes` em `Order`** | Campo `String` (max 500, nullable) para observações do cliente no pedido (ex.: "sem cebola"). Exibido no checkout e visível na área admin. |
| **E-mail de confirmação de pedido** | Spring Mail + template de e-mail (HTML) enviado após checkout bem-sucedido. |
| **Notificação de mudança de status** | E-mail ou push notification quando o status do pedido avança (ex.: "Seu pedido saiu para entrega!"). |
| **Taxa de entrega dinâmica** | Campo `deliveryFee` em `Order`, calculada por CEP/distância ou valor fixo por bairro. Hoje exibe "Grátis" fixo no formulário. |
| **Cupom de desconto** | Entidade `Coupon` com código, tipo (percentual/fixo), validade e limite de usos. Aplicável no checkout. |
| **Painel de status do pedido em tempo real** | WebSocket ou polling para o cliente acompanhar `PENDING → PREPARING → OUT_FOR_DELIVERY → DELIVERED` sem recarregar a página. |
| **Relatórios admin** | Exportação de pedidos em CSV/PDF, gráficos de faturamento por período. |
| **Múltiplos endereços por cliente** | `Address` como entidade separada vinculada a `Customer`. Cliente escolhe o endereço no checkout. |
| **Avaliação de produtos** | `Review` com nota (1–5) e comentário, visível no catálogo público. |
| **Estoque** | Campo `stock` em `Product`, decremento no checkout, alerta quando zerado. |
| **PWA / app mobile** | Progressive Web App com manifest e service worker para experiência mobile-first. |
| **Gestão de área de entrega** | Definir bairros/CEPs atendidos; validar no checkout se o endereço do cliente está na área coberta. |

---

*Documento gerado em fevereiro/2026. Última atualização: fevereiro/2026 — adicionados itens 1.8 (modal de quantidade), 1.9 (máscaras), 1.10 (ViaCEP), 2.7 (crédito no footer), 3.13 (status PT-BR), 3.14 (nome no dropdown), 3.15 (conflito de sessão), 3.16 (copyright 2026).*
