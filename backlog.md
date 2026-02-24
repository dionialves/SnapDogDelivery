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
6. [Funcionalidades Futuras (pós v1.0)](#6-funcionalidades-futuras-pós-v10)

---

## 1. Área Pública — Novas Funcionalidades (v1.0)

Toda a área pública é **inexistente** no projeto atual. O sistema hoje é exclusivamente
administrativo (rotas sob `/admin/**`). Esta seção descreve tudo que precisa ser criado
do zero para o lançamento da v1.0.

---

### 1.1 Autenticação de Clientes (Cadastro e Login)

**Contexto:** Clientes finais precisam se cadastrar e fazer login para realizar compras.
O sistema de autenticação atual (`/admin/login`) é exclusivo para usuários admin —
os clientes precisam de um fluxo separado com rotas e templates próprios.

**Decisão de arquitetura:** Reutilizar a entidade `User` já existente (`tb_users`).
O cadastro público adiciona `role = CUSTOMER` à enum `Role` e cria um relacionamento
`@OneToOne` entre `User` e `Client`. Usuários admin não possuem `Client` vinculado (campo nullable).

**Alterações na entidade `User`:**

- Adicionar `role = CUSTOMER` na enum `Role` (`com.dionialves.snapdogdelivery.user.Role`)
- Adicionar campo `client` (`@OneToOne`, FK `client_id`, nullable) em `User`
  — usuários admin: `client = null`; clientes públicos: `client` aponta para o `Client` criado no cadastro

**O que criar:**

- **`CustomerAuthController`** no pacote `com.dionialves.snapdogdelivery.auth`
  (`@Controller`, rotas públicas sob `/`)
  - `CustomerRegisterDTO` — nome, e-mail, senha, telefone, endereço completo (todos os
    campos de `Client`)
  - `CustomerLoginDTO` — e-mail e senha

- **Endpoints públicos de autenticação:**
  - `GET /register` → formulário de cadastro do cliente
  - `POST /register` → cria `Client` + `User` (role `CUSTOMER`) vinculados, redireciona para `/catalog`
  - `GET /login` → formulário de login público (rota separada do admin `/admin/login`)
  - `POST /login` → autenticação Spring Security, redireciona para `/catalog`
  - `POST /logout` → logout do cliente

- **Configuração de segurança (`SecurityConfig`):**
  - Criar segunda cadeia de filtros (`SecurityFilterChain`) para a área pública
  - Rotas **totalmente públicas** (sem autenticação): `/`, `/catalog`, `/catalog/**`,
    `/register`, `/login`, `/error`, recursos estáticos
  - Rotas autenticadas (cliente com role `CUSTOMER`): `/cart/**`, `/checkout/**`, `/account/**`
  - Rotas admin mantidas em cadeia separada: `/admin/**` (roles `ADMIN` / `SUPER_ADMIN`)

- **Templates Thymeleaf públicos:**
  - `templates/public/auth/register.html` — formulário de cadastro com validação em tempo real
  - `templates/public/auth/login.html` — formulário de login público (separado do admin)
  - Layout público: `templates/public/fragments/layout.html` (ver item 1.7)

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

> ⏳ **Pendente** — a URL externa foi implementada; o upload de arquivo local/S3 ainda não.

- Dependência `spring-boot-starter-web` já inclui suporte a `MultipartFile`
- Criar `StorageService` — salva arquivo em diretório configurável (`/uploads/products/`)
  ou futuramente em S3/bucket
- `ProductViewController` — aceitar `@RequestParam MultipartFile image`
- Servir arquivos estáticos uploadados via `ResourceHandler` no `WebMvcConfig`
- Adicionar campo de upload no formulário admin (com fallback para URL externa)

---

### 1.3 Catálogo Público de Produtos

> ⏳ **Pendente** — campo `active` já existe na entidade (ver 1.2 concluído); falta o `StoreController` e os templates públicos.

**Contexto:** Página principal da loja — lista todos os produtos disponíveis com foto,
nome, descrição e preço. O catálogo é **público** (não requer login). O login só é exigido
quando o cliente tenta adicionar um produto ao carrinho.

**O que criar:**

- **Pacote `com.dionialves.snapdogdelivery.store`**
  - `StoreController` (`@Controller`, `@RequestMapping("/catalog")`)
  - Reutiliza `ProductService` já existente (sem duplicação de lógica)

- **Endpoints:**
  - `GET /catalog` → lista todos os produtos ativos, paginados (público)
  - `GET /catalog/{id}` → detalhe de um produto — foto ampliada, descrição completa, preço,
    botão "Add to cart" (público para visualizar; exige login ao clicar)

- **Templates:**
  - `templates/public/store/catalog.html` — grid de cards de produtos com foto, nome,
    preço e botão de carrinho
  - `templates/public/store/product-detail.html` — página de detalhe do produto

---

### 1.4 Carrinho de Compras

**Contexto:** Cliente autenticado adiciona produtos ao carrinho antes de finalizar o pedido.
Implementação via sessão HTTP (sem persistência em banco — se fechar o browser, o carrinho
é perdido). Tentativa de adicionar ao carrinho sem estar logado redireciona para `/login`.

**O que criar:**

- **Classe `Cart`** (não é entidade JPA — POJO `Serializable` armazenado na sessão HTTP)
  - `Map<Long, CartItem> items` (chave: `productId`)
  - Métodos: `addItem(Long productId, String name, BigDecimal price, int qty)`,
    `removeItem(Long productId)`, `updateQuantity(Long productId, int qty)`,
    `getTotal()`, `clear()`

- **Classe `CartItem`** (POJO Serializable)
  - Campos: `productId`, `productName`, `imageUrl`, `unitPrice`, `quantity`
  - Método: `getSubtotal()`

- **Pacote `com.dionialves.snapdogdelivery.cart`**
  - `CartService` — gerencia `Cart` na `HttpSession` (chave `"cart"`)
  - `CartController` (`@Controller`, `@RequestMapping("/cart")`)

- **Endpoints** (todos exigem autenticação com role `CUSTOMER`):
  - `POST /cart/add` → adiciona item (recebe `productId` e `quantity`)
  - `POST /cart/remove/{productId}` → remove item
  - `POST /cart/update/{productId}` → atualiza quantidade
  - `GET /cart` → visualização do carrinho
  - `POST /cart/clear` → esvazia o carrinho

- **Template:**
  - `templates/public/cart/cart.html` — tabela de itens, subtotais, total, botão "Checkout"

- **UX no catálogo:** botão "Add to cart" em cada card faz `POST /cart/add`. Se não
  autenticado, Spring Security redireciona para `/login` e retorna após login. Se autenticado,
  responde com atualização do contador de itens no header (toast ou redirect).

---

### 1.5 Checkout e Finalização do Pedido

> ⏳ **Parcialmente concluído** — `OrderOrigin` e campo `origin` já implementados (fevereiro/2026); falta o `CheckoutController` e templates públicos.

**Contexto:** Cliente revisa o carrinho, confirma endereço de entrega e finaliza o pedido.
O pedido é criado na entidade `Order` já existente com origem `ONLINE`.

**Alterações na entidade `Order`:**

- ✅ Campo `origin` (enum `OrderOrigin`: `ONLINE`, `MANUAL`, default `MANUAL`) — **concluído**
- ⏳ Campo `deliveryAddress` (`String`, nullable) — snapshot do endereço de entrega — **pendente**

**O que criar:**

- ✅ **Enum `OrderOrigin`** em `com.dionialves.snapdogdelivery.order` — **concluído**

- **Pacote `com.dionialves.snapdogdelivery.checkout`**
  - `CheckoutController` (`@Controller`, `@RequestMapping("/checkout")`)
  - `CheckoutService` — converte `Cart` (sessão) em `Order` persistida:
    - Valida que o carrinho não está vazio
    - Busca o `Client` vinculado ao `User` autenticado via `user.getClient()`
    - Cria `OrderCreateDTO` internamente e delega para `OrderService.create()`
    - Registra `deliveryAddress` como snapshot do endereço atual do `Client`
    - Limpa o carrinho após criação bem-sucedida

- **Endpoints** (exigem autenticação com role `CUSTOMER`):
  - `GET /checkout` → tela de revisão — resumo do carrinho + endereço do cliente
  - `POST /checkout/confirm` → cria o pedido, redireciona para confirmação
  - `GET /checkout/confirmation/{orderId}` → tela de sucesso com número e resumo do pedido

- **Templates:**
  - `templates/public/checkout/review.html` — resumo do pedido, endereço, total
  - `templates/public/checkout/confirmation.html` — tela de sucesso com número do pedido
    e status atual

---

### 1.6 Área do Cliente — My Account

**Contexto:** Cliente logado consulta histórico de pedidos e gerencia seus dados cadastrais.

**O que criar:**

- **Pacote `com.dionialves.snapdogdelivery.account`**
  - `AccountController` (`@Controller`, `@RequestMapping("/account")`)

- **Endpoints** (exigem autenticação com role `CUSTOMER`):
  - `GET /account` → painel do cliente com resumo dos pedidos recentes
  - `GET /account/orders` → histórico completo de pedidos, paginado
  - `GET /account/orders/{id}` → detalhe de um pedido (produtos, status, valor total)
  - `GET /account/profile` → formulário de edição de dados cadastrais (nome, telefone,
    endereço)
  - `POST /account/profile` → salva alterações cadastrais

- **Templates:**
  - `templates/public/account/dashboard.html`
  - `templates/public/account/orders.html`
  - `templates/public/account/order-detail.html`
  - `templates/public/account/profile.html`

---

### 1.7 Layout Público (Brand Snap Dog)

**Contexto:** A área pública precisa de identidade visual própria, separada do painel admin.
Deve usar a paleta "snapdog" (vermelho `#dc2626`) já definida no Tailwind, com foco em UX
de delivery.

**O que criar:**

- `templates/public/fragments/layout.html` — layout base público:
  - Header com: logo Snap Dog, link "Menu" (`/catalog`), ícone de carrinho com contador
    de itens (lê da sessão — exibido apenas para clientes autenticados), menu do usuário
    (nome + "Sign out") ou botões "Sign in" / "Register"
  - Footer com informações da loja (horário, telefone, redes sociais)
  - Mesmo stack de CSS: Tailwind CDN + Lucide Icons

- `templates/public/index.html` — landing page pública (home `GET /`):
  - Hero com nome da marca e chamada para ação ("View menu")
  - Seção de produtos em destaque (3–6 itens) — **visível sem login**
  - Informações sobre a entrega (horários, área de cobertura)
  - Catálogo completo acessível sem autenticação; apenas a ação de compra exige login

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

> **PARCIALMENTE CONCLUÍDO** (fevereiro/2026) — campo `imageUrl` com preview implementado; upload de arquivo ainda pendente (ver 1.2.1).

**Solução aplicada:**

- `templates/admin/products/form.html` — campo de texto para `imageUrl` com preview em tempo real e checkbox para `active`

**Ainda pendente:**

- Campo de upload de arquivo (`<input type="file">`) como alternativa à URL
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
| `ClientService.java` | `searchByNameOrPhone(String)` — duplicava `search(String)` |
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

### 3.11 Variáveis em Português e Mensagens de API em Inglês

> **CONCLUÍDO** (fevereiro/2026) — commit `0e1258d` em `develop`.

**Problema:** Identificadores Java em português (`salved`, `emailAtual`, `criarPedido`, `joao`, `maria`,
`criarClient` etc.) misturados com mensagens de API totalmente em inglês (`"Client not found with ID"`,
`"Order must have at least one product"`, `"CLient ID is madatory"` — com typos).

**Solução aplicada:**

| Arquivo | Alteração |
|---|---|
| `ClientService.java` | `salved` → `saved`; 3 mensagens de `NotFoundException` e 1 de `BusinessException` traduzidas |
| `ClientDTO.java` | 21 mensagens de validação Jakarta traduzidas para PT-BR |
| `OrderService.java` | 5 mensagens de `NotFoundException` e 3 de `BusinessException` traduzidas |
| `OrderCreateDTO.java` | Typos `"CLient ID is madatory"` corrigidos; 3 mensagens traduzidas |
| `ProductService.java` | 3 mensagens de `NotFoundException` traduzidas |
| `ProductDTO.java` | 3 mensagens de validação traduzidas |
| `ProductOrderDTO.java` | 4 mensagens de validação traduzidas |
| `GlobalExceptionHandler.java` | 6 strings de resposta traduzidas |
| `CustomUserDetailsService.java` | Mensagem `"User not found with email"` traduzida |
| `UserService.java` | `emailAtual` → `currentEmail` |
| `UserDTO.java` | **Excluído** (legado sem uso em nenhum controller) |
| `ClientRepositoryTest.java` | `joao`/`maria` → `clientJohn`/`clientMary`; `criarClient` → `createClient`; parâmetros `nome`/`telefone` → `name`/`phone` |
| `OrderRepositoryTest.java` | `criarPedido` → `createOrder`; `criarPedidoComProduto` → `createOrderWithProduct`; `quantidade` → `quantity` |
| `OrderViewControllerTest.java` | `pedidoResponseDTO` → `orderResponseDTO` |
| `DashboardServiceTest.java` | `criarClient` → `createClient`; `criarOrder` → `createOrder` |
| `OrderServiceTest.java` | Asserção `"PENDING"` → `"PENDENTE"` (consequência da tradução da mensagem) |

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

> **CONCLUÍDO** (fevereiro/2026) — Suite de **101 testes** implementada, 0 falhas, BUILD SUCCESS.
>
> **Evolução da suite:**
> - Entrega inicial: 76 testes (serviços, controllers, repositórios)
> - Adicionados 25 testes para o módulo `user` (`UserServiceTest`, `UserControllerTest`, `UserViewControllerTest`)
> - Renomeação de variáveis/métodos auxiliares em PT-BR para inglês (fevereiro/2026)
>
> **Infraestrutura corrigida junto com a entrega inicial:**
> - `annotationProcessorPaths` do Lombok movido para `maven-compiler-plugin` no `pom.xml`
> - `spring-security-test` adicionado ao `pom.xml`
> - `@Profile("!test")` adicionado ao `DataSeeder` para evitar dados de seed no H2
> - `NotFoundException` passa a estender `BusinessException`
>
> Testes pendentes dependem de funcionalidades ainda não implementadas (seções 1.3, 1.4, 1.5).

---

### 5.1 Testes de Serviço (Unitários)

| Classe de Teste | Status | Testes |
|---|---|---|
| `ClientServiceTest` | ✅ Concluído | 10 — `create`, `update`, `delete` (com e sem pedidos), `search` paginado |
| `ProductServiceTest` | ✅ Concluído | 9 — `create`, `update`, `delete`, `search` |
| `OrderServiceTest` | ✅ Concluído | 16 — `create` (fluxo completo), `updateStatus` (todas as transições válidas e inválidas), `delete` |
| `DashboardServiceTest` | ✅ Concluído | 7 — `getDashboardSummary` (mocks de repositório), cálculo de crescimento |
| `UserServiceTest` | ✅ Concluído | 12 — `create`, `update`, `delete` (guard de auto-exclusão), `findById`, `findAll` |
| `CheckoutServiceTest` | ⏳ Pendente | Depende da feature 1.5 (Checkout) |

---

### 5.2 Testes de Controller (Integração)

| Classe de Teste | Status | Testes |
|---|---|---|
| `ClientControllerTest` | ✅ Concluído | 3 — `GET /admin/api/clients/search`: com resultado, termo vazio, sem resultado |
| `ProductControllerTest` | ✅ Concluído | 3 — `GET /admin/api/products/search`: com resultado, termo vazio, sem resultado |
| `OrderViewControllerTest` | ✅ Concluído | 13 — listagem, criação, atualização de status, exclusão (sucessos e erros) |
| `UserControllerTest` | ✅ Concluído | 3 — `GET /admin/api/users`: listagem paginada, sem usuários, página inválida |
| `UserViewControllerTest` | ✅ Concluído | 10 — listagem, criação, edição, exclusão (sucessos e erros) |
| `StoreControllerTest` | ⏳ Pendente | Depende da feature 1.3 (Catálogo Público) |
| `CartControllerTest` | ⏳ Pendente | Depende da feature 1.4 (Carrinho) |
| `CheckoutControllerTest` | ⏳ Pendente | Depende da feature 1.5 (Checkout) |

---

### 5.3 Testes de Repositório

| Classe de Teste | Status | Testes |
|---|---|---|
| `OrderRepositoryTest` | ✅ Concluído | 6 — `sumRevenueByCreatedAtBetween`, `findTopSellingProducts`, `existsByClientId` |
| `ClientRepositoryTest` | ✅ Concluído | 8 — `findByNameContainingIgnoreCaseOrPhoneContaining`, `countByCreatedAt` |

---

## 6. Funcionalidades Futuras (pós v1.0)

Itens identificados como desejáveis mas fora do escopo imediato do lançamento.

| Funcionalidade | Descrição |
|---|---|
| **Login com Google (OAuth2)** | `spring-boot-starter-oauth2-client` + configuração no Google Cloud Console. Cria/vincula `Client` automaticamente ao `User`. |
| **Campo `notes` em `Order`** | Campo `String` (max 500, nullable) para observações do cliente no pedido (ex.: "sem cebola"). Exibido no checkout e visível na área admin. |
| **E-mail de confirmação de pedido** | Spring Mail + template de e-mail (HTML) enviado após checkout bem-sucedido. |
| **Notificação de mudança de status** | E-mail ou push notification quando o status do pedido avança (ex.: "Seu pedido saiu para entrega!"). |
| **Taxa de entrega dinâmica** | Campo `deliveryFee` em `Order`, calculada por CEP/distância ou valor fixo por bairro. Hoje exibe "Grátis" fixo no formulário. |
| **Cupom de desconto** | Entidade `Coupon` com código, tipo (percentual/fixo), validade e limite de usos. Aplicável no checkout. |
| **Painel de status do pedido em tempo real** | WebSocket ou polling para o cliente acompanhar `PENDING → PREPARING → OUT_FOR_DELIVERY → DELIVERED` sem recarregar a página. |
| **Relatórios admin** | Exportação de pedidos em CSV/PDF, gráficos de faturamento por período. |
| **Múltiplos endereços por cliente** | `Address` como entidade separada vinculada a `Client`. Cliente escolhe o endereço no checkout. |
| **Avaliação de produtos** | `Review` com nota (1–5) e comentário, visível no catálogo público. |
| **Estoque** | Campo `stock` em `Product`, decremento no checkout, alerta quando zerado. |
| **PWA / app mobile** | Progressive Web App com manifest e service worker para experiência mobile-first. |
| **Gestão de área de entrega** | Definir bairros/CEPs atendidos; validar no checkout se o endereço do cliente está na área coberta. |

---

*Documento gerado em fevereiro/2026. Última atualização: fevereiro/2026 — suite em 101 testes, 0 falhas.*
