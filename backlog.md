# Backlog — Sistema Snap Dog Delivery

Documento de levantamento completo de funcionalidades pendentes para a versão 1.0.
Baseado na análise do código-fonte atual (fevereiro/2026).

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

**Contexto:** Clientes finais precisam se cadastrar e fazer login para acessar o catálogo
e realizar compras. O sistema de autenticação atual (`/login`) é exclusivo para usuários
admin — os clientes precisam de um fluxo separado.

**Decisão de arquitetura:** O cliente logado é vinculado à entidade `Client` já existente
(`tb_clients`). O login/cadastro público cria ou localiza um `Client` associado ao usuário.

**O que criar:**

- **Entidade `CustomerAccount`** (ou adicionar campos em `User`) — conta do cliente público
  - Campos: `id`, `client` (FK para `Client`), `email` (único), `password` (BCrypt),
    `emailVerified`, `createdAt`
  - Alternativa mais simples: adicionar `role = CUSTOMER` na enum `Role` existente e
    vincular `User` a `Client` com relacionamento `@OneToOne`

- **Pacote `com.dionialves.snapdogdelivery.auth.customer`**
  - `CustomerAuthController` (`@Controller`, rotas públicas sob `/`)
  - `CustomerRegisterDTO` — dados do formulário de cadastro (nome, e-mail, senha, telefone,
    endereço completo)
  - `CustomerLoginDTO`

- **Endpoints públicos de autenticação:**
  - `GET /cadastro` → formulário de registro
  - `POST /cadastro` → cria `Client` + conta de acesso, redireciona para `/catalogo`
  - `GET /entrar` → formulário de login público
  - `POST /entrar` → autenticação, redireciona para `/catalogo`
  - `POST /sair` → logout do cliente

- **Configuração de segurança (`SecurityConfig`):**
  - Criar segunda cadeia de filtros (`SecurityFilterChain`) para a área pública
  - Rotas públicas: `/`, `/catalogo`, `/entrar`, `/cadastro`, `/error`, recursos estáticos
  - Rotas autenticadas (cliente): `/catalogo/**`, `/carrinho/**`, `/checkout/**`, `/minha-conta/**`
  - Rotas admin mantidas em cadeia separada: `/admin/**`

- **Templates Thymeleaf públicos:**
  - `templates/public/auth/register.html` — formulário de cadastro com validação
  - `templates/public/auth/login.html` — formulário de login público (separado do admin)
  - Layout público: `templates/public/fragments/layout.html` (ver item 1.6)

---

### 1.2 Imagem de Produto

**Contexto:** O catálogo público precisa exibir foto dos produtos. A entidade `Product`
atual não possui campo de imagem.

**O que alterar:**

- **Entidade `Product`** — adicionar campos:
  - `imageUrl` (`String`, nullable, max 500) — URL externa da imagem (ex.: Imgur, CDN próprio)
  - Futuramente: `imagePath` para upload local (ver item 1.2.1)

- **`ProductDTO`** — adicionar campo `imageUrl` com `@Size(max = 500)`

- **`ProductResponseDTO`** — incluir `imageUrl` na resposta

- **Formulário admin (`admin/products/form.html`)** — adicionar campo de URL de imagem
  com preview em tempo real via JavaScript

**1.2.1 — Upload de arquivo (fase seguinte ao imageUrl):**

- Dependência `spring-boot-starter-web` já inclui suporte a `MultipartFile`
- Criar `StorageService` — salva arquivo em diretório configurável (`/uploads/products/`)
  ou futuramente em S3/bucket
- `ProductViewController` — aceitar `@RequestParam MultipartFile image`
- Servir arquivos estáticos uploadados via `ResourceHandler` no `WebMvcConfig`
- Adicionar campo de upload no formulário admin (com fallback para URL externa)

---

### 1.3 Catálogo Público de Produtos

**Contexto:** Página principal da loja — lista todos os produtos disponíveis com foto,
nome, descrição e preço. Visível apenas para clientes logados.

**O que criar:**

- **Pacote `com.dionialves.snapdogdelivery.store`**
  - `StoreController` (`@Controller`, `@RequestMapping("/catalogo")`)
  - Usa `ProductService` já existente (sem duplicação de lógica)

- **Endpoints:**
  - `GET /catalogo` → lista todos os produtos ativos, paginados
  - `GET /catalogo/{id}` → detalhe de um produto (foto ampliada, descrição completa, preço,
    botão "Adicionar ao carrinho")

- **Campo `active` no `Product`** (booleano, default `true`) — permite o admin ocultar
  produtos do catálogo sem excluí-los. Filtrar no `StoreController` por `active = true`.

- **Templates:**
  - `templates/public/store/catalog.html` — grid de cards de produtos com foto, nome,
    preço e botão de carrinho
  - `templates/public/store/product-detail.html` — página de detalhe do produto

---

### 1.4 Carrinho de Compras

**Contexto:** Cliente adiciona produtos ao carrinho antes de finalizar o pedido.
Implementação via sessão HTTP (sem persistência em banco — se fechar o browser, perde).

**O que criar:**

- **Classe `Cart`** (não é entidade JPA — é um POJO `Serializable` para a sessão)
  - `Map<Long, CartItem> items` (chave: `productId`)
  - Métodos: `addItem(Long productId, String name, BigDecimal price, int qty)`,
    `removeItem(Long productId)`, `updateQuantity(Long productId, int qty)`,
    `getTotal()`, `clear()`

- **Classe `CartItem`** (POJO Serializable)
  - `productId`, `productName`, `imageUrl`, `unitPrice`, `quantity`
  - Método `getSubtotal()`

- **Pacote `com.dionialves.snapdogdelivery.cart`**
  - `CartService` — gerencia `Cart` na `HttpSession` (chave `"cart"`)
  - `CartController` (`@Controller`, `@RequestMapping("/carrinho")`)

- **Endpoints:**
  - `POST /carrinho/adicionar` → adiciona item (recebe `productId` e `quantity`)
  - `POST /carrinho/remover/{productId}` → remove item
  - `POST /carrinho/atualizar/{productId}` → atualiza quantidade
  - `GET /carrinho` → visualização do carrinho
  - `POST /carrinho/limpar` → esvazia o carrinho

- **Templates:**
  - `templates/public/cart/cart.html` — tabela de itens, subtotais, total, botão "Finalizar pedido"

- **UX no catálogo:** botão "Adicionar ao carrinho" em cada card de produto faz `POST`
  e exibe feedback (toast ou atualização do ícone de carrinho no header com contador de itens)

---

### 1.5 Checkout e Finalização do Pedido

**Contexto:** Cliente revisa o carrinho, confirma endereço de entrega e finaliza o pedido.
O pedido é criado na entidade `Order` já existente, com origem `ONLINE`.

**Alterações na entidade `Order`:**

- Adicionar campo `origin` (enum `OrderOrigin`: `ONLINE`, `MANUAL`, default `MANUAL`)
- Adicionar campo `deliveryAddress` (`String`, nullable) — endereço de entrega registrado
  no momento do pedido (snapshot, assim como `priceAtTime` no produto)
- Considerar adicionar `notes` (`String`, nullable, max 500) — observações do cliente

**O que criar:**

- **Enum `OrderOrigin`** em `com.dionialves.snapdogdelivery.order`

- **Pacote `com.dionialves.snapdogdelivery.checkout`**
  - `CheckoutController` (`@Controller`, `@RequestMapping("/checkout")`)
  - `CheckoutService` — converte `Cart` (sessão) em `Order` persistida
    - Valida que o carrinho não está vazio
    - Busca o `Client` vinculado ao usuário logado
    - Cria `OrderCreateDTO` internamente e delega para `OrderService.create()`
    - Limpa o carrinho após criação bem-sucedida

- **Endpoints:**
  - `GET /checkout` → tela de revisão (resumo do carrinho + endereço do cliente)
  - `POST /checkout/confirmar` → cria o pedido, redireciona para confirmação
  - `GET /checkout/confirmacao/{orderId}` → tela de sucesso com resumo do pedido

- **Templates:**
  - `templates/public/checkout/review.html` — resumo do pedido, endereço, total
  - `templates/public/checkout/confirmation.html` — tela de sucesso com número do pedido
    e status atual

---

### 1.6 Área do Cliente — Minha Conta

**Contexto:** Cliente logado pode consultar seu histórico de pedidos e dados cadastrais.

**O que criar:**

- **Pacote `com.dionialves.snapdogdelivery.customer`**
  - `CustomerAreaController` (`@Controller`, `@RequestMapping("/minha-conta")`)

- **Endpoints:**
  - `GET /minha-conta` → painel do cliente (resumo de pedidos recentes)
  - `GET /minha-conta/pedidos` → histórico completo de pedidos paginado
  - `GET /minha-conta/pedidos/{id}` → detalhe de um pedido (produtos, status, valor)
  - `GET /minha-conta/dados` → formulário de edição de dados cadastrais (nome, telefone,
    endereço)
  - `POST /minha-conta/dados` → salva alterações cadastrais

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

- `templates/public/fragments/layout.html` — layout base público
  - Header com: logo Snap Dog, link "Cardápio" (`/catalogo`), ícone de carrinho com
    contador de itens (lê da sessão), menu do usuário (nome + "Sair") ou botões
    "Entrar" / "Cadastrar-se"
  - Footer com informações da loja (horário, telefone, redes sociais)
  - Mesmo stack de CSS: Tailwind CDN + Lucide Icons

- `templates/public/index.html` — landing page pública (home):
  - Hero com nome da marca e chamada para ação ("Ver cardápio")
  - Seção de produtos em destaque (3–6 itens)
  - Informações sobre a entrega (horários, área de cobertura)
  - Acesso sem login para visualizar a home; catálogo completo requer login

---

## 2. Área Administrativa — Novas Funcionalidades

---

### 2.1 Gerenciamento de Usuários Admin

**Contexto:** Atualmente não existe nenhuma interface para criar, editar ou excluir usuários
administrativos. Os únicos usuários existem porque o `DataSeeder` os cria na inicialização.
O `UserDTO` existe no código mas não é usado por nenhum controller.

**O que criar:**

- **`UserService`** (`com.dionialves.snapdogdelivery.user`)
  - `findAll(int page, int size)` — lista paginada
  - `findById(Long)` — ou lança `NotFoundException`
  - `create(UserCreateDTO)` — valida e-mail único, encoda senha com `BCryptPasswordEncoder`
  - `update(Long, UserUpdateDTO)` — permite alterar nome, role e senha
  - `delete(Long)` — não permite excluir o próprio usuário logado

- **`UserController`** (`@RestController`, `/admin/api/users`) — endpoints JSON

- **`UserViewController`** (`@Controller`, `/admin/users`) — CRUD via Thymeleaf
  - `GET /admin/users` → lista paginada de usuários
  - `GET /admin/users/novo` → formulário de criação
  - `POST /admin/users/novo` → cria usuário
  - `GET /admin/users/{id}` → formulário de edição
  - `POST /admin/users/{id}` → atualiza usuário
  - `POST /admin/users/{id}/excluir` → remove usuário

- **DTOs:**
  - `UserCreateDTO` — nome, e-mail, senha, role (com validações)
  - `UserUpdateDTO` — nome, role, senha (opcional — só atualiza se preenchida)
  - `UserResponseDTO` — id, nome, e-mail, role, createdAt (sem senha)

- **Templates:**
  - `templates/admin/users/list.html`
  - `templates/admin/users/form.html`

- **Sidebar** (`layout.html`) — adicionar link "Usuários" visível apenas para `SUPER_ADMIN`

---

### 2.2 Controle de Acesso por Role

**Contexto:** A enum `Role` tem três níveis (`USER`, `ADMIN`, `SUPER_ADMIN`) mas o
`SecurityConfig` atual só exige `authenticated()` — qualquer usuário logado acessa tudo.

**O que implementar:**

- **`SecurityConfig`** — adicionar `hasRole()` nas rotas admin:
  - `/admin/users/**` → apenas `SUPER_ADMIN`
  - `/admin/**` (restante) → `ADMIN` ou `SUPER_ADMIN`

- **Templates** — ocultar links de menu com `sec:authorize` (Thymeleaf Security extras):
  - Link "Usuários" no sidebar → só aparece para `SUPER_ADMIN`
  - Botões de exclusão de pedidos/clientes → considerar restringir a `SUPER_ADMIN`

- **Dependência a adicionar no `pom.xml`:**
  ```xml
  <dependency>
      <groupId>org.thymeleaf.extras</groupId>
      <artifactId>thymeleaf-extras-springsecurity6</artifactId>
  </dependency>
  ```

---

### 2.3 Campo de Imagem no Formulário Admin de Produto

**Contexto:** Após adicionar `imageUrl` e suporte a upload na entidade `Product` (item 1.2),
o formulário admin precisa ser atualizado.

**O que alterar:**

- `templates/admin/products/form.html`:
  - Campo de texto para `imageUrl` com preview da imagem em tempo real
  - Campo de upload de arquivo (`<input type="file">`) como alternativa
  - JavaScript para alternar entre os dois modos e exibir preview

---

### 2.4 Campo `origin` Visível na Área Admin

**Contexto:** Com pedidos podendo vir de duas origens (`ONLINE` e `MANUAL`), o admin
precisa distingui-los.

**O que alterar:**

- `templates/admin/orders/list.html` — adicionar coluna ou badge "Origem" (Online / Manual)
- `templates/admin/orders/form.html` — exibir origem no modo de visualização do pedido
- Filtro opcional por origem na listagem de pedidos

---

### 2.5 Campo `active` no Gerenciamento de Produtos

**Contexto:** Após adicionar o campo `active` em `Product` (item 1.3), o admin precisa
poder ativar/desativar produtos do catálogo público.

**O que alterar:**

- `templates/admin/products/list.html` — coluna de status (Ativo/Inativo) com toggle
- `templates/admin/products/form.html` — checkbox "Exibir no catálogo público"
- `ProductService.update()` — processar o campo `active`

---

## 3. Correções de Bugs e Dívidas Técnicas

---

### 3.1 `System.out.println` em Código de Produção

Três ocorrências de debug prints que precisam ser removidos:

| Arquivo | Linha | Conteúdo |
|---|---|---|
| `ClientViewController.java` | 45 | `System.out.println(model.asMap())` |
| `OrderService.java` | 82 | `System.out.println(saved)` |
| `GlobalExceptionHandler.java` | 65–66 | Dois prints no handler genérico de `Exception` |

**Solução:** Remover todos. Se logging for necessário, configurar `org.slf4j.Logger`
(via `LoggerFactory.getLogger(...)`) — **não usar `@Slf4j`** (proibido pelo AGENTS.md).

---

### 3.2 Flash Messages de Produto Nunca Exibem

**Contexto:** `ProductViewController` envia `"successMessage"` / `"errorMessage"` nos
`RedirectAttributes`, mas `admin/products/list.html` lê `"messagem"` / `"messageType"`
(padrão do template de clientes). As mensagens de sucesso/erro em produtos nunca aparecem
na tela.

**Solução:** Padronizar todos os controllers e templates. Recomendado adotar o padrão
`"successMessage"` / `"errorMessage"` e atualizar `clients/list.html` para usar as
mesmas chaves.

**Arquivos afetados:**
- `ClientViewController.java` — alterar chaves de flash messages
- `templates/admin/clients/list.html` — atualizar `th:if` para novas chaves
- `templates/admin/products/list.html` — já usa o padrão correto (apenas verificar)

---

### 3.3 Typo na URL do Botão "Voltar" em Produtos

**Arquivo:** `templates/admin/products/form.html`, linha 17

**Problema:** Link aponta para `/admin/produts` (faltando o `c`).

**Solução:** Corrigir para `/admin/products`.

---

### 3.4 `@Transactional` Faltando em Métodos de Serviço

Quatro métodos de escrita sem anotação transacional:

| Arquivo | Método |
|---|---|
| `ClientService.java` | `create(ClientDTO)` |
| `ClientService.java` | `delete(Long)` |
| `ProductService.java` | `create(ProductDTO)` |
| `ProductService.java` | `delete(Long)` |

**Solução:** Adicionar `@Transactional` em cada um.

---

### 3.5 Conversão de Enum Vazia em `OrderViewController`

**Arquivo:** `OrderViewController.java`, linha 37

**Problema:** `@RequestParam(defaultValue = "") OrderStatus status` — quando `status` é
string vazia (`""`), Spring tenta converter para `OrderStatus` e lança
`ConversionFailedException` (HTTP 500). Deveria retornar todos os pedidos quando sem filtro.

**Solução:**
```java
// De:
@RequestParam(defaultValue = "") OrderStatus status
// Para:
@RequestParam(required = false) OrderStatus status
```
E ajustar a lógica de serviço/specification para aceitar `null` como "sem filtro"
(o `OrderSpecifications.hasStatus` já trata `null` corretamente — só falta a correção
no controller).

---

### 3.6 `BusinessException` Renderiza Template `error/500`

**Arquivo:** `GlobalExceptionHandler.java`, linha ~47

**Problema:** Erros de negócio (HTTP 400) renderizam o template `error/500.html`, o que
é semanticamente incorreto (o template diz "Erro interno do servidor").

**Solução:** Criar `templates/error/400.html` com mensagem adequada para erros de
validação/negócio, ou reusar `error/500.html` com título e mensagem dinâmicos via
variável de modelo.

---

### 3.7 Formulário de Pedido Existente Posta para Rota Errada

**Arquivo:** `templates/admin/orders/form.html`, linha 34

**Problema:** Ao visualizar um pedido existente (`GET /admin/orders/{id}`), o `action`
do formulário está fixo em `/admin/orders/new`, fazendo com que um possível submit crie
um pedido duplicado em vez de atualizar.

**Solução:** Usar `th:action` dinâmico:
```html
th:action="${order.id != null} ? @{/admin/orders/{id}(id=${order.id})} : @{/admin/orders/new}"
```

---

### 3.8 Métodos Duplicados (Código Morto) nos Serviços

| Arquivo | Método duplicado |
|---|---|
| `ClientService.java` | `searchByNameOrPhone(String)` (linha ~55) — duplica `search(String)` |
| `ProductService.java` | `searchByName(String)` (linha ~52) — duplica `search(String)` |

**Solução:** Remover os métodos duplicados não utilizados.

---

### 3.9 CSRF Desabilitado

**Arquivo:** `SecurityConfig.java`, linha 39 — `csrf.disable()`

**Contexto:** CSRF foi desabilitado para simplificar o desenvolvimento. Em produção,
formulários Thymeleaf incluem o token automaticamente via `th:action`.

**Solução para v1.0:** Reativar CSRF e garantir que todos os formulários usem `th:action`
(o que já é o caso na maioria). Verificar endpoints AJAX (`fetch()`) que precisarão
incluir o header `X-CSRF-TOKEN`.

---

### 3.10 Logo Placeholder no Layout Admin

**Arquivo:** `templates/admin/fragments/layout.html`, linha ~54

**Problema:** Comentário `<!-- Logo placeholder - depois substituímos pelo SVG real -->`
com emoji no lugar do logo.

**Solução:** Criar/adicionar o SVG ou imagem real do logo Snap Dog.

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
   - (migrations seguintes conforme novas features)

---

### 4.2 Nome da Classe Principal

**Arquivo:** `GreendogdeliveryApplication.java`

**Problema:** O nome da classe ainda reflete o nome antigo do projeto ("Greendog").

**Solução:** Renomear para `SnapdogDeliveryApplication.java` e atualizar a referência
no `application.properties` se houver.

---

### 4.3 Senha Padrão no `application.properties`

**Arquivo:** `src/main/resources/application.properties`

**Problema:** Credenciais de banco (`postgresadmin`) hardcoded no arquivo versionado.

**Solução:** Usar variáveis de ambiente:
```properties
spring.datasource.password=${DB_PASSWORD:postgresadmin}
```
E adicionar `application.properties` ao `.gitignore` ou criar um `application-prod.properties`
fora do repositório.

---

## 5. Cobertura de Testes

O projeto possui apenas um teste (`contextLoads()`). A infraestrutura de testes está
pronta (H2, `@ActiveProfiles("test")`). Testes são necessários antes do lançamento da v1.0.

---

### 5.1 Testes de Serviço (Unitários)

| Classe de Teste | Casos prioritários |
|---|---|
| `ClientServiceTest` | `create`, `update`, `delete` (com e sem pedidos), `search` paginado |
| `ProductServiceTest` | `create`, `update`, `delete`, `search` |
| `OrderServiceTest` | `create` (fluxo completo), `updateStatus` (todas as transições válidas e inválidas), `delete` |
| `DashboardServiceTest` | `getDashboardSummary` (mocks de repositório), cálculo de crescimento |
| `CheckoutServiceTest` (novo) | conversão de `Cart` em `Order`, carrinho vazio |

---

### 5.2 Testes de Controller (Integração)

| Classe de Teste | Casos prioritários |
|---|---|
| `ClientControllerTest` | `GET /admin/api/clients/search` — resultado, lista vazia |
| `ProductControllerTest` | `GET /admin/api/products/search` |
| `OrderViewControllerTest` | criação de pedido, avanço de status, cancelamento |
| `StoreControllerTest` (novo) | catálogo sem login (redirect), catálogo com login |
| `CartControllerTest` (novo) | adicionar, remover, atualizar quantidade |
| `CheckoutControllerTest` (novo) | confirmar pedido, carrinho vazio |

---

### 5.3 Testes de Repositório

| Classe de Teste | Casos prioritários |
|---|---|
| `OrderRepositoryTest` | `sumRevenueByCreatedAtBetween`, `findTopSellingProducts`, `existsByClientId` |
| `ClientRepositoryTest` | `findByNameContainingIgnoreCaseOrPhoneContaining`, `countByCreatedAt` |

---

## 6. Funcionalidades Futuras (pós v1.0)

Itens identificados como desejáveis mas fora do escopo imediato do lançamento.

| Funcionalidade | Descrição |
|---|---|
| **Login com Google (OAuth2)** | `spring-boot-starter-oauth2-client` + configuração no Google Cloud Console. Cria/vincula `Client` automaticamente. |
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

*Documento gerado em fevereiro/2026. Atualizar conforme decisões de implementação.*
