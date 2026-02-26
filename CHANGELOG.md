# Changelog — SnapDog Delivery

Registro de todas as alterações relevantes do projeto, organizadas por data e categoria.

---

## [Não lançado] — develop

### Novas Funcionalidades

- **Credenciais próprias para Customer** (`d38c323` — 26/02/2026 15:20)
  Campos `password` (BCrypt, NOT NULL) e `active` (boolean, default `true`) adicionados à
  entidade `Customer`. `DataSeeder` atualizado para semear senha `"cliente123"` encodada em
  BCrypt em todos os clientes de desenvolvimento.

- **`CustomerUserDetailsService`** (`b25cde3` — 26/02/2026 15:20)
  Serviço de autenticação exclusivo para a área pública, em `domain/storefront/auth`.
  Consulta `CustomerRepository.findByEmail()`, atribui `ROLE_CUSTOMER`, lança
  `DisabledException` para clientes inativados.

- **`AdminAuthController`** (`b25cde3` — 26/02/2026 15:20)
  Controller dedicado ao login admin em `domain/admin/auth`, servindo `GET /admin/login`.
  Template `templates/admin/auth/login.html` criado.

- **Gerenciamento de Usuários Admin** (`3473179` / `3dd0b41` — 24/02/2026 16:33)
  CRUD completo: `UserService`, `UserController` (REST), `UserViewController` (Thymeleaf).
  Templates `admin/users/list.html` e `admin/users/form.html`. Link "Users" no sidebar,
  visível apenas para `SUPER_ADMIN`.

- **Controle de Acesso por Role** (`3473179` — 24/02/2026 16:33)
  `SecurityConfig` com `hasRole()` aplicado: `/admin/users/**` exige `SUPER_ADMIN`;
  restante de `/admin/**` exige `ADMIN` ou `SUPER_ADMIN`. `thymeleaf-extras-springsecurity6`
  adicionado ao `pom.xml`; `sec:authorize` aplicado no sidebar.

- **Campo `origin` em Order** (`90c935e` — 24/02/2026 16:29)
  Enum `OrderOrigin` (`ONLINE`, `MANUAL`) criada. Campo `origin` adicionado à entidade `Order`,
  `OrderCreateDTO` e `OrderResponseDTO`. `OrderService.create()` aplica `MANUAL` como padrão.

- **Campo `imageUrl` e `active` em Product** (`87a0475` — 24/02/2026 16:27)
  Campo `imageUrl` (URL externa, max 500) e `active` (visibilidade no catálogo) adicionados
  à entidade `Product`, DTOs e formulário admin com preview e checkbox.
  Backend de upload via `StorageService` + `WebMvcConfig` implementado.

- **Área Pública — Autenticação de Clientes** (commit anterior a 24/02/2026)
  `CustomerAuthController`, templates `public/auth/register.html` e `public/auth/login.html`,
  `CustomerRegisterDTO`. Duas cadeias de filtros de segurança configuradas.

- **Área Pública — Catálogo de Produtos** (commit anterior a 24/02/2026)
  `StoreController`, `ProductService.findAllActive()` e `findFeatured()`.
  Templates `public/store/catalog.html`, `public/store/product-detail.html` e
  `public/index.html`.

- **Área Pública — Carrinho de Compras** (commit anterior a 24/02/2026)
  `Cart` / `CartItem` (POJO na sessão), `CartService`, `CartController`.
  Template `public/cart/cart.html`. Contador de itens no header público.

- **Área Pública — Checkout** (commit anterior a 24/02/2026)
  `CheckoutService`, `CheckoutController`. Campo `deliveryAddress` (snapshot) em `Order`.
  Templates `public/checkout/review.html` e `public/checkout/confirmation.html`.

- **Área Pública — My Account** (commit anterior a 24/02/2026)
  `AccountController` com histórico de pedidos, detalhe e edição de perfil.
  `OrderService.findByCustomerId()` e `findRecentByCustomerId()` adicionados.
  Templates `public/account/dashboard.html`, `orders.html`, `order-detail.html`, `profile.html`.

- **Layout Público (Brand Snap Dog)** (commit anterior a 24/02/2026)
  `templates/public/fragments/layout.html` com header (logo, carrinho, menu do usuário) e
  footer. `public/index.html` com hero e produtos em destaque.

- **Suite Completa de Testes** (`178fcaa` — 23/02/2026 19:14)
  76 testes iniciais cobrindo serviços, controllers e repositórios do domínio admin.
  Infraestrutura: Lombok no `maven-compiler-plugin`, `spring-security-test`, `@Profile("!test")`
  no `DataSeeder`, `NotFoundException` estende `BusinessException`.

---

### Melhorias

- **Tela de login do cliente redesenhada** (`fcbddd5` — 26/02/2026 15:20)
  Layout split: painel esquerdo com gradiente da marca, slogan e benefícios (oculto em mobile);
  painel direito com formulário. Template standalone sem layout compartilhado.

- **Desacoplamento completo de autenticação** (`b25cde3` — 26/02/2026 15:20)
  Duas `SecurityFilterChain` independentes, cada uma com seu próprio `DaoAuthenticationProvider`.
  Cadeia admin: `securityMatcher("/admin/**")`, login em `/admin/login`.
  Cadeia pública: login em `/login`, logout redireciona para `/`.

- **Refatoração de `CustomerAuthController`** (`b25cde3` — 26/02/2026 15:20)
  Cadastro cria apenas `Customer` (sem `User`); verifica e-mail duplicado via
  `CustomerRepository.existsByEmail()`; dependência de `UserRepository` removida.
  `CustomerLoginDTO` excluído.

- **`AccountController`, `CheckoutController`, `CheckoutService` refatorados** (`b25cde3` — 26/02/2026 15:20)
  Carregam `Customer` via `CustomerRepository.findByEmail()` em vez de `user.getCustomer()`.

- **Renomeação `Client` → `Customer`** (`56e51ae` / `dec236c` / `a963328` — 26/02/2026 10:03)
  Domínio inteiro renomeado: entidade, serviço, controller, repositório, DTO, testes e pacote.
  Rotas, templates, IDs HTML e variáveis JS atualizados. `AGENTS.md`, `backlog.md` e `README.md`
  corrigidos.

- **Variáveis PT-BR → inglês e mensagens de API traduzidas** (`0e1258d` — 26/02/2026 10:03)
  Identificadores Java em PT-BR (`salved`, `emailAtual`, `criarPedido`, `joao`, `maria` etc.)
  renomeados para inglês. Mensagens de `NotFoundException`, `BusinessException` e validações
  Jakarta traduzidas para PT-BR. Typos (`"CLient ID is madatory"`) corrigidos.

- **Separação de profiles (dev / prod / test)** (`da4b39f` — 23/02/2026 19:35)
  `application-dev.properties`, `application-prod.properties` (`.gitignore`) com variáveis de
  ambiente, `application-prod.properties.example` versionado. Senha do banco removida do
  versionamento.

- **Suite de testes expandida** (`89af679` — 24/02/2026 16:37)
  25 testes adicionados para o módulo `user`: `UserServiceTest`, `UserControllerTest`,
  `UserViewControllerTest`.

- **Suite de testes — desacoplamento de autenticação** (`ab29e19` — 26/02/2026 15:20)
  13 testes adicionados: `CustomerUserDetailsServiceTest` (3), `AdminAuthControllerTest` (3),
  `UserRoleTest` (2), `CustomerServiceTest` (+1), `CustomerRepositoryTest` (+4).
  Total: **114 testes**, 0 falhas.

- **Renomeação da classe principal** (`9814e55` — 23/02/2026 19:29)
  `GreendogdeliveryApplication` → `SnapdogDeliveryApplication`;
  `GreendogdeliveryApplicationTests` → `SnapdogDeliveryApplicationTests`.

- **Padronização de indentação dos templates** (`31d7070` — 24/02/2026 16:03)
  Indentação padronizada para 2 espaços em todos os templates Thymeleaf.

---

### Correções

- **`System.out.println` removidos** (`1588f6f` — 23/02/2026 19:41)
  Debug prints removidos de `ClientViewController`, `OrderService` e `GlobalExceptionHandler`.

- **Flash messages padronizadas** (`1588f6f` — 23/02/2026 19:41)
  Chaves `successMessage` / `errorMessage` / `messageType` unificadas em todos os módulos
  (pedidos, clientes, produtos). Templates e controllers corrigidos.

- **Typo na URL do botão "Voltar" em Produtos** (`88d473f` — 23/02/2026 19:59)
  `/admin/produts` → `/admin/products` em `products/form.html`.

- **`@Transactional` faltando nos serviços** (`1588f6f` — 23/02/2026 19:41)
  Adicionado em `CustomerService.create()`, `CustomerService.delete()`,
  `ProductService.create()` e `ProductService.delete()`.

- **Conversão de enum vazia em `OrderViewController`** (`1588f6f` — 23/02/2026 19:41)
  `@RequestParam(defaultValue = "")` → `@RequestParam(required = false)` em `findAll()`.

- **`BusinessException` renderizava template `error/500`** (`88d473f` — 23/02/2026 19:59)
  `GlobalExceptionHandler.handleBusiness()` atualizado para `error/400`; template
  `templates/error/400.html` criado.

- **Formulário de pedido postava para rota errada** (`88d473f` — 23/02/2026 19:59)
  `th:action` em `orders/form.html` tornado dinâmico para distinguir criação de edição.

- **Métodos duplicados nos serviços removidos** (`1588f6f` — 23/02/2026 19:41)
  `CustomerService.searchByNameOrPhone()` e `ProductService.searchByName()` excluídos.

- **CSRF desabilitado** (`88d473f` — 23/02/2026 19:59)
  `csrf.disable()` removido de `SecurityConfig`. Formulários já usavam `th:action`.

- **Logout usando GET em vez de POST** (`fb9e231` — 23/02/2026 20:37)
  Logout corrigido para usar `POST` com token CSRF.

- **CSRF ausente no DELETE de produto e cliente** (`7007d2b` / `d234501` — 24/02/2026 15:38)
  Token CSRF adicionado aos formulários de exclusão de produto e cliente.

- **Conversão de preço ao salvar produto** (`7007d2b` — 24/02/2026 15:38)
  Conversão de `BigDecimal` corrigida ao persistir produto.

- **Limpeza da entidade `User` e da enum `Role`** (`b25cde3` — 26/02/2026 15:20)
  Campo `customer` (`@OneToOne`) e `customer_id` removidos de `User`.
  `CUSTOMER` removido da enum `Role` — agora contém apenas `USER`, `ADMIN`, `SUPER_ADMIN`.
