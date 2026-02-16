# SnapDog Delivery

Sistema de gerenciamento de pedidos para delivery de hot dogs, construido com as tecnologias Spring Boot e Thymeleaf. Esse projeto é resultado dos estudos realizado junto ao livro <a href="https://www.casadocodigo.com.br/products/livro-spring-boot">Spring Boot - Acelere o Desenvolvimento de Micro Serviços</a>. O livro inicia um projeto chamado de GreenDogDelivery e qu quis criar a minha própria versão e aplicar os conhecimentos, por este livro adiquiridos.

![SnapDogDelivery Dashboard](https://github.com/dionialves/imagefiles/blob/main/snapdogdelivery/snapdogdelivery-dashboard.png)
![SnapDogDelivery Create Order](https://github.com/dionialves/imagefiles/blob/main/snapdogdelivery/snapdogdelivery-novopedido.png)

## Tecnologias

**Backend:**
- Java 21
- Spring Boot 4.0.2
- Spring Data JPA / Hibernate
- Hibernate Validator (Jakarta Validation)
- Lombok

**Frontend:**
- Thymeleaf + Layout Dialect
- Tailwind CSS 3 (CDN)
- Lucide Icons (CDN)

**Banco de Dados:**
- PostgreSQL 18 (Docker)
- H2 (testes)

**Build & Infra:**
- Maven
- Docker Compose

## Funcionalidades

### Dashboard
- Cards com metricas do dia vs dia anterior (pedidos, faturamento, novos clientes, ticket medio)
- Indicadores de crescimento percentual
- Tabela de pedidos recentes
- Ranking de produtos mais vendidos

### Clientes
- CRUD completo com validacao de campos
- Busca por nome ou telefone
- Endereco completo com estado (enum UF)

### Produtos
- CRUD completo (nome, preco, descricao)
- Busca por nome

### Pedidos
- Criacao de pedidos com selecao de cliente e produtos
- Controle de status: Pendente → Preparando → Em Entrega → Entregue
- Cancelamento apenas de pedidos pendentes
- Pedidos nao podem ser alterados apos criacao
- Snapshot do preco do produto no momento do pedido (`priceAtTime`)
- Filtros por status e busca por nome do cliente

## Arquitetura

Estrutura orientada a dominios, onde cada dominio (`client`, `product`, `order`, `dashboard`) contem suas camadas:

```
src/main/java/com/dionialves/snapdogdelivery/
├── client/          # Entity, DTO, Repository, Service, Controllers
├── product/         # Entity, DTO, Repository, Service, Controllers
├── order/           # Entity, DTO, Repository, Service, ViewController
├── productorder/    # Entity de junção (Order ↔ Product)
├── dashboard/       # Service, ViewController, DTOs de métricas
├── config/          # DataSeeder (dados de exemplo)
└── exception/       # GlobalExceptionHandler, NotFoundException, BusinessException
```

Cada dominio segue o padrao de **Dual Controller**:
- `*Controller` (`@RestController`) — API JSON em `/admin/api/`
- `*ViewController` (`@Controller`) — Paginas Thymeleaf em `/admin/`

## Rotas

| Rota | Descricao |
|------|-----------|
| `/admin/dashboard` | Dashboard com metricas |
| `/admin/clients` | Listagem de clientes |
| `/admin/clients/new` | Formulario de novo cliente |
| `/admin/products` | Listagem de produtos |
| `/admin/products/new` | Formulario de novo produto |
| `/admin/orders` | Listagem de pedidos com filtros |
| `/admin/orders/new` | Formulario de novo pedido |

## Como Rodar

### Pre-requisitos
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

A aplicacao estara disponivel em `http://localhost:8080/admin/`.

O `DataSeeder` popula automaticamente o banco com 10 clientes, 10 produtos e 50 pedidos de exemplo.
