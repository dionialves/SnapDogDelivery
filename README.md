# SnapDog Delivery

![versão](https://img.shields.io/badge/versão-0.4.0-red)

Sistema de gerenciamento de pedidos para delivery de hot dogs, construído com Spring Boot e Thymeleaf. Este projeto é resultado dos estudos realizados junto ao livro [Spring Boot - Acelere o Desenvolvimento de Micro Serviços](https://www.casadocodigo.com.br/products/livro-spring-boot). O livro inicia um projeto chamado GreenDogDelivery — esta é minha própria versão, onde apliquei e expandi os conhecimentos adquiridos.

![SnapDogDelivery Dashboard](https://github.com/dionialves/imagefiles/blob/main/snapdogdelivery/snapdogdelivery-dashboard.png)
![SnapDogDelivery Create Order](https://github.com/dionialves/imagefiles/blob/main/snapdogdelivery/snapdogdelivery-novopedido.png)

---

## Tecnologias

**Backend:**
- Java 21
- Spring Boot 4.0.2
- Spring Security (dual filter chain)
- Spring Data JPA / Hibernate
- Flyway (controle de versão do schema)
- Hibernate Validator (Jakarta Validation)
- Lombok

**Frontend:**
- Thymeleaf + Layout Dialect
- Tailwind CSS 3 (CDN)
- Lucide Icons (CDN)
- Cropper.js (CDN — editor de recorte de imagem)

**Banco de Dados:**
- PostgreSQL 18 (Docker)
- H2 (testes)

**Build & Infra:**
- Maven 3+
- Docker Compose

---

## Funcionalidades

### Painel Admin (`/admin`)

**Dashboard**
- Cards com métricas do dia vs. dia anterior (pedidos, faturamento, novos clientes, ticket médio)
- Indicadores de crescimento percentual
- Tabela dos 5 pedidos mais recentes
- Ranking dos 5 produtos mais vendidos

**Clientes**
- CRUD completo com validação de campos
- Busca por nome ou telefone
- Endereço completo com estado (enum UF)

**Produtos**
- CRUD completo (nome, preço, descrição, categoria, status ativo/inativo)
- Busca por nome
- Categorias: Hot Dog e Bebida
- Flag de destaque — limite de 6 produtos em destaque simultâneos
- Upload de imagem com drop zone, editor de recorte (Cropper.js) e envio via AJAX

**Pedidos**
- Criação de pedidos manuais com seleção de cliente e produtos
- Controle de status: Pendente → Preparando → Em Entrega → Entregue
- Cancelamento apenas de pedidos pendentes
- Pedidos não podem ser alterados após criação
- Snapshot do preço do produto no momento do pedido (`priceAtTime`)
- Filtros por status e busca por nome do cliente

**Usuários Administrativos**
- CRUD completo (visível apenas para `SUPER_ADMIN`)
- Roles: `ADMIN` e `SUPER_ADMIN`

**Configurações da Empresa**
- Edição de nome, e-mail, telefone, endereço, horário de funcionamento e copyright
- Dados exibidos dinamicamente no footer da área pública

---

### Área Pública — Storefront (`/`)

**Catálogo**
- Landing page com até 6 produtos em destaque
- Catálogo completo paginado, organizado em seções por categoria (Hot Dogs / Bebidas)
- Tabs de filtro por categoria
- Modal de quantidade ao adicionar ao carrinho (seletor 1–20)

**Carrinho**
- Mantido na sessão HTTP (sem persistência em banco)
- Atualização de quantidades e remoção de itens
- Contador de itens exibido no cabeçalho

**Checkout**
- Revisão do pedido e endereço de entrega antes de confirmar
- Snapshot do endereço salvo no pedido (`deliveryAddress`)
- Carrinho esvaziado automaticamente após confirmação

**Área da Conta**
- Painel com os 5 pedidos mais recentes
- Histórico completo de pedidos paginado
- Edição de dados cadastrais com máscara de telefone e CEP
- Autocomplete de endereço via API ViaCEP

**Autenticação do Cliente**
- Cadastro e login independentes do sistema admin
- Campos de telefone e CEP com máscara automática no cadastro

---

## Arquitetura

Estrutura orientada a domínios com **slicing vertical** sob `domain/`:

```
src/main/java/com/dionialves/snapdogdelivery/
├── domain/
│   ├── admin/
│   │   ├── auth/          # Login admin
│   │   ├── customer/      # Entity, DTO, Repository, Service, Controllers
│   │   ├── product/       # Entity, DTO, Repository, Service, Controllers
│   │   ├── order/         # Entity, DTO, Repository, Service, Controllers
│   │   ├── productorder/  # Entidade de junção (Order ↔ Product)
│   │   ├── dashboard/     # Service, ViewController, DTOs de métricas
│   │   ├── settings/      # CompanySettings, Service, ViewController
│   │   └── user/          # User, Role, Service, Controllers
│   └── storefront/
│       ├── auth/          # Autenticação do cliente
│       ├── store/         # Catálogo público
│       ├── cart/          # Carrinho (sessão)
│       ├── checkout/      # Finalização de pedido
│       └── account/       # Área da conta do cliente
├── config/                # DataSeeder, FlywayConfig
├── exception/             # GlobalExceptionHandler, NotFoundException, BusinessException
└── infra/
    ├── security/          # SecurityConfig (dual filter chain)
    └── storage/           # StorageService (upload de imagens)
```

Cada domínio segue o padrão de **Dual Controller**:
- `*Controller` (`@RestController`) — API JSON em `/admin/api/`
- `*ViewController` (`@Controller`) — Páginas Thymeleaf em `/admin/`

---

## Rotas

### Painel Admin

| Rota | Descrição |
|------|-----------|
| `/admin/login` | Login admin |
| `/admin/dashboard` | Dashboard com métricas |
| `/admin/customers` | Listagem de clientes |
| `/admin/customers/new` | Formulário de novo cliente |
| `/admin/products` | Listagem de produtos |
| `/admin/products/new` | Formulário de novo produto |
| `/admin/orders` | Listagem de pedidos com filtros |
| `/admin/orders/new` | Formulário de novo pedido |
| `/admin/users` | Gestão de usuários (apenas `SUPER_ADMIN`) |
| `/admin/settings` | Configurações da empresa |

### Área Pública

| Rota | Descrição |
|------|-----------|
| `/` | Landing page com produtos em destaque |
| `/catalog` | Catálogo completo por categoria |
| `/login` | Login do cliente |
| `/register` | Cadastro do cliente |
| `/cart` | Carrinho de compras |
| `/checkout` | Revisão e confirmação do pedido |
| `/account` | Painel da conta do cliente |
| `/account/orders` | Histórico de pedidos |
| `/account/profile` | Edição de perfil |

---

## Como Rodar

### Pré-requisitos
- Java 21
- Docker e Docker Compose
- Maven 3+

### Subir o banco de dados

```bash
docker compose up -d
```

### Compilar e rodar

```bash
mvn clean install
mvn spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`.

### Acesso

O `DataSeeder` popula automaticamente o banco com dados de exemplo (10 clientes, 10 produtos, 50 pedidos).

**Painel Admin** — `http://localhost:8080/admin/login`

| Usuário | E-mail | Senha | Role |
|---------|--------|-------|------|
| Administrador | admin@snapdog.com | admin123 | ADMIN |
| Super Admin | superadmin@snapdog.com | super123 | SUPER_ADMIN |

**Área Pública** — `http://localhost:8080/login`

| E-mail | Senha |
|--------|-------|
| joao.silva@email.com | cliente123 |
| maria.oliveira@email.com | cliente123 |

> Todos os 10 clientes gerados usam a senha `cliente123`.

---

## Documentação

Cada versão a partir da **0.3.0** possui documentação completa com regras de negócio e fluxos de trabalho em [`/docs`](./docs/).

| Versão | Descrição |
|--------|-----------|
| [v0.4.0](docs/v0.4.0.md) | Categorias, destaques, editor de imagem, storefront aprimorado, Flyway |
| [v0.3.1](docs/v0.3.1.md) | Correções de bugs pós v0.3.0 |
| [v0.3.0](docs/v0.3.0.md) | Storefront completo, segurança dual-chain, usuários admin |
