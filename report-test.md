# Relatório Técnico de Testes — Snap Dog Delivery

**Data:** fevereiro/2026
**Versão da aplicação:** 0.0.1-SNAPSHOT
**Stack:** Spring Boot 4.0.2 · Java 21 · JUnit 5 · Mockito · AssertJ · H2 (in-memory)

---

## 1. Resultado Final

```
Tests run: 63   Failures: 0   Errors: 0   Skipped: 0   BUILD SUCCESS
```

| Classe de Teste              | Tipo         | Casos |
|------------------------------|--------------|------:|
| `ClientServiceTest`          | Unitário     |    10 |
| `ProductServiceTest`         | Unitário     |     9 |
| `OrderServiceTest`           | Unitário     |    16 |
| `DashboardServiceTest`       | Unitário     |     7 |
| `ClientControllerTest`       | Controller   |     3 |
| `ProductControllerTest`      | Controller   |     3 |
| `ClientRepositoryTest`       | Integração   |     8 |
| `OrderRepositoryTest`        | Integração   |     6 |
| `GreendogdeliveryApplicationTests` | Smoke  |     1 |
| **Total**                    |              |**63** |

---

## 2. Estratégia de Testes

A bateria foi organizada em três camadas independentes que refletem a arquitetura vertical do projeto.

### 2.1 Testes Unitários de Serviço (`@ExtendWith(MockitoExtension.class)`)

Todos os repositórios e dependências são substituídos por dublês Mockito (`@Mock`). O serviço real é instanciado por injeção de construtor via `@InjectMocks`.

**Por que esta abordagem:**
- O serviço é a única camada com lógica de negócio. Isolá-la garante que um erro no banco não mascare um bug de domínio.
- Execução em milissegundos — sem contexto Spring, sem banco, sem I/O.
- Permite testar cada caminho de erro exatamente uma vez, com total controle sobre o estado devolvido pelo repositório.

**Padrão de cada teste:**
1. `when(repositório.método(...)).thenReturn(...)` — configura o comportamento esperado do stub.
2. Chama o método do serviço.
3. `assertThat(resultado)...` — verifica o valor retornado.
4. `verify(repositório).método(...)` ou `verify(..., never()).método(...)` — confirma que a operação de escrita foi (ou não foi) invocada.

### 2.2 Testes de Controller (`MockMvcBuilders.standaloneSetup`)

O controller real é instanciado com o serviço mockado e registrado num `MockMvc` standalone, sem subir contexto Spring completo.

**Por que esta abordagem:**
- Spring Boot 4.0.2 removeu `@WebMvcTest` e `@DataJpaTest` do módulo `spring-boot-test-autoconfigure`. A alternativa padrão para testes de controller sem contexto completo é o `standaloneSetup` do `MockMvcBuilders`.
- O objetivo é verificar que o controller faz o bind correto do parâmetro `?q=` e serializa a resposta em JSON — não re-testar o serviço.
- Custo mínimo: sem segurança, sem banco, sem `DataSeeder`.

**Padrão de cada teste:**
```java
mockMvc.perform(get("/admin/api/clients/search").param("q", "termo"))
       .andExpect(status().isOk())
       .andExpect(jsonPath("$[0].name").value("..."));
```

### 2.3 Testes de Repositório (`@SpringBootTest + @ActiveProfiles("test") + @Transactional`)

Sobe o contexto Spring completo contra H2 em memória. Cada teste insere seus próprios dados no `setUp`, executa a query e verifica o resultado. A anotação `@Transactional` na classe garante rollback automático após cada teste, mantendo o banco limpo entre os casos.

**Por que esta abordagem:**
- `@DataJpaTest` foi removido no Spring Boot 4. A alternativa é `@SpringBootTest` com perfil `test` (que aponta para H2).
- Testa as queries JPQL customizadas (`@Query`) e os derived queries do Spring Data que não são verificáveis com mocks — o comportamento real do banco importa aqui (ex.: `COALESCE`, `GROUP BY`, filtro por intervalo de data).
- `@Transactional` + rollback evita que um teste contamine os dados de outro.

**Problema encontrado e resolvido:**
O `DataSeeder` (que popula dados de desenvolvimento) rodava na inicialização do contexto de teste e corrompía as asserções de contagem/soma. Solução: adicionar `@Profile("!test")` ao `DataSeeder`, desabilitando-o no perfil `test`.

---

## 3. Infraestrutura e Configuração

### 3.1 Correções no `pom.xml`

**Problema:** O `annotationProcessorPaths` do Lombok estava configurado no `spring-boot-maven-plugin`, que não participa da fase `testCompile`. O compilador Maven não encontrava os getters/setters gerados pelo Lombok ao compilar as classes de teste.

**Solução:** Mover o `annotationProcessorPaths` para o `maven-compiler-plugin`, que gerencia ambas as fases `compile` e `testCompile`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>1.18.42</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

Adicionada também a dependência `spring-security-test` (escopo `test`) necessária para `@WithMockUser`.

### 3.2 Perfil `test`

`src/test/resources/application-test.yml` configura H2 em memória:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
```

Todos os testes que sobem contexto Spring usam `@ActiveProfiles("test")`.

---

## 4. Detalhamento dos Testes

### 4.1 `CustomerServiceTest` — 10 casos

Arquivo: `src/test/java/com/dionialves/snapdogdelivery/domain/admin/customer/CustomerServiceTest.java`

| Método de Teste | O que valida |
|---|---|
| `search_semPaginacao_retornaListaDTOs` | O método `search(String)` delega ao repositório e converte entidades em DTOs. |
| `search_paginado_retornaPage` | O método `search(String, int, int)` retorna `Page<CustomerDTO>` com os dados corretos. |
| `findById_existente_retornaDTO` | Quando o repositório retorna um `Customer`, o serviço devolve o DTO mapeado corretamente. |
| `findById_inexistente_lancaNotFoundException` | Quando o repositório retorna `Optional.empty()`, lança `NotFoundException` com o ID na mensagem. |
| `create_dadosValidos_persisteERetornaDTO` | O método `create` chama `repository.save()` e retorna o DTO com os dados persistidos. |
| `update_existente_atualizaERetornaDTO` | O método `update` carrega a entidade, aplica os novos dados e retorna o DTO atualizado. |
| `update_inexistente_lancaNotFoundException` | Tentativa de atualizar um ID inexistente lança `NotFoundException`. |
| `delete_semPedidos_removeSucesso` | Cliente sem pedidos é removido — `deleteById` é chamado uma vez. |
| `delete_inexistente_lancaNotFoundException` | Tentativa de excluir ID inexistente lança `NotFoundException` sem chamar `deleteById`. |
| `delete_comPedidos_lancaBusinessException` | Cliente com pedidos associados lança `BusinessException` sem chamar `deleteById` — regra de integridade de domínio. |

**Decisão de design:** O `delete` testa tanto `NotFoundException` quanto `BusinessException` porque são dois guards sequenciais no serviço (`existsById` → `existsByClientId`). Cada caminho precisa de um caso próprio.

---

### 4.2 `ProductServiceTest` — 9 casos

Arquivo: `src/test/java/com/dionialves/snapdogdelivery/product/ProductServiceTest.java`

| Método de Teste | O que valida |
|---|---|
| `search_semPaginacao_retornaListaDTOs` | `search(String)` converte `List<Product>` em `List<ProductResponseDTO>`. |
| `search_paginado_retornaPage` | `search(String, int, int)` retorna `Page<ProductResponseDTO>` com preço correto (`isEqualByComparingTo` para `BigDecimal`). |
| `findById_existente_retornaDTO` | Produto encontrado retorna DTO com ID e nome corretos. |
| `findById_inexistente_lancaNotFoundException` | ID inexistente lança `NotFoundException`. |
| `create_dadosValidos_persisteERetornaDTO` | `create` persiste via `save` e devolve DTO com nome e preço. |
| `update_existente_atualizaERetornaDTO` | `update` altera nome e preço da entidade carregada do repositório. |
| `update_inexistente_lancaNotFoundException` | `update` com ID inválido lança `NotFoundException`. |
| `delete_existente_removeSucesso` | `deleteById` é chamado quando produto existe. |
| `delete_inexistente_lancaNotFoundException` | `NotFoundException` lançada e `deleteById` nunca chamado. |

**Nota:** O `ProductService` não tem a guarda `existsByOrderId` que `ClientService` tem, por isso não há teste de `BusinessException` no `delete`. Isso é intencional — a regra de integridade de produto ainda não existe no código de produção.

---

### 4.3 `OrderServiceTest` — 16 casos

Arquivo: `src/test/java/com/dionialves/snapdogdelivery/order/OrderServiceTest.java`

Este é o teste mais extenso porque `OrderService` concentra a maior densidade de regras de negócio: criação de pedido com múltiplos produtos e máquina de estados de `OrderStatus`.

#### Grupo `findById` (2 casos)

| Método de Teste | O que valida |
|---|---|
| `findById_existente_retornaDTO` | Pedido encontrado retorna DTO com status serializado como string `"PENDING"`. |
| `findById_inexistente_lancaNotFoundException` | ID ausente lança `NotFoundException`. |

#### Grupo `create` (3 casos)

| Método de Teste | O que valida |
|---|---|
| `create_dadosValidos_persisteERetornaDTO` | Fluxo completo: resolve cliente, resolve produto, cria `ProductOrder` com snapshot de preço, persiste e retorna DTO com status `PENDING`. |
| `create_clienteInexistente_lancaNotFoundException` | `clientId` inválido aborta antes de chamar `save`. |
| `create_produtoInexistente_lancaNotFoundException` | `productId` inválido dentro da lista de produtos lança `NotFoundException`. |

#### Grupo `updateStatus` — transições válidas (4 casos)

Cada transição da máquina de estados é testada individualmente para garantir que `canAdvanceTo` e `isFinal` estão corretos:

| Método de Teste | Transição testada |
|---|---|
| `updateStatus_pendingParaPreparing_sucesso` | `PENDING → PREPARING` |
| `updateStatus_preparingParaOutForDelivery_sucesso` | `PREPARING → OUT_FOR_DELIVERY` |
| `updateStatus_outForDeliveryParaDelivered_sucesso` | `OUT_FOR_DELIVERY → DELIVERED` |
| `updateStatus_cancelarPending_sucesso` | `PENDING → CANCELED` (cancelamento só é permitido neste status) |

#### Grupo `updateStatus` — transições inválidas (5 casos)

| Método de Teste | O que valida |
|---|---|
| `updateStatus_cancelarPreparing_lancaBusinessException` | Cancelar pedido em `PREPARING` é proibido — a mensagem deve citar `PENDING` como único status cancelável. |
| `updateStatus_transicaoInvalida_lancaBusinessException` | Pular etapas (`PENDING → DELIVERED`) lança `BusinessException`. |
| `updateStatus_pedidoFinal_lancaBusinessException` | Tentar alterar pedido `DELIVERED` (status final) lança `BusinessException` com o nome do status na mensagem. |
| `updateStatus_pedidoCancelado_lancaBusinessException` | `CANCELED` também é final — qualquer tentativa de mudança é bloqueada. |
| `updateStatus_inexistente_lancaNotFoundException` | Pedido não encontrado lança `NotFoundException` antes de qualquer validação de status. |

#### Grupo `delete` (2 casos)

| Método de Teste | O que valida |
|---|---|
| `delete_existente_removeSucesso` | `existsById` → `deleteById` — sequência correta. |
| `delete_inexistente_lancaNotFoundException` | `existsById` retorna `false` → `NotFoundException`, sem chamar `deleteById`. |

---

### 4.4 `DashboardServiceTest` — 7 casos

Arquivo: `src/test/java/com/dionialves/snapdogdelivery/dashboard/DashboardServiceTest.java`

O `DashboardService` não tem entidade própria — é um agregador de leitura que combina dados de `OrderRepository` e `ClientRepository`. Os testes focam nos cálculos derivados (ticket médio, percentual de crescimento) e na proteção contra divisão por zero.

| Método de Teste | O que valida |
|---|---|
| `getDashboardSummary_retornaValoresCorretos` | Com 5 pedidos e R$ 150,00 hoje e 3 pedidos e R$ 90,00 ontem: ticket médio hoje = 30,00; ticket médio ontem = 30,00. Verifica todos os campos do DTO de uma vez. |
| `getDashboardSummary_semPedidos_ticketMedioZero` | Com zero pedidos, o divisor seria 0 — verifica que o código retorna `BigDecimal.ZERO` ao invés de lançar `ArithmeticException`. |
| `getOrdersGrowthPercentage_ontemZero_retornaZero` | Com `ordersYesterday = 0`, o denominador do cálculo de crescimento seria 0 — verifica retorno seguro de `0` (proteção por guard clause no DTO). |
| `getRevenueGrowthPercentage_calculaPercentualCorreto` | R$ 120 hoje vs R$ 100 ontem → crescimento de 20%. Valida a fórmula `(hoje - ontem) / ontem * 100`. |
| `getRecentOrders_retornaListaDTOs` | Dois pedidos mockados são mapeados para `DashboardRecentOrderDTO` com `clientName` e `status` corretos. |
| `getRecentOrders_semPedidos_retornaListaVazia` | Lista vazia do repositório produz lista vazia no resultado — sem `NullPointerException`. |
| `getTopSellingProducts_retornaListaDTOs` | Produto com `totalSold = 42` é retornado corretamente pelo DTO de projeção `DashboardTopProductDTO`. |

---

### 4.5 `CustomerControllerTest` — 3 casos

Arquivo: `src/test/java/com/dionialves/snapdogdelivery/domain/admin/customer/CustomerControllerTest.java`

Usa `MockMvcBuilders.standaloneSetup(customerController)` — o menor custo possível para testar o contrato HTTP do controller.

| Método de Teste | O que valida |
|---|---|
| `search_retornaListaCustomers` | `GET /admin/api/customers/search?q=joão` retorna HTTP 200 e JSON com `name` e `email` do cliente mockado. |
| `search_termoVazio_retornaTodos` | `?q=` vazio retorna HTTP 200 e array com 2 elementos. |
| `search_semResultados_retornaListaVazia` | `?q=xyz` retorna HTTP 200 e array vazio `[]` — não 404. |

**Por que não testar autenticação aqui:** O `standaloneSetup` não carrega o `SecurityConfig`. O teste de redirecionamento para login seria um teste de integração de segurança — escopo dos testes de integração com contexto completo, ainda não implementados.

---

### 4.6 `ProductControllerTest` — 3 casos

Arquivo: `src/test/java/com/dionialves/snapdogdelivery/product/ProductControllerTest.java`

Mesma abordagem do `ClientControllerTest`, espelhando os casos para o domínio `product`.

| Método de Teste | O que valida |
|---|---|
| `search_retornaListaProdutos` | `GET /admin/api/products/search?q=hot` retorna HTTP 200, `name` e `price` (15.90) corretos no JSON. |
| `search_termoVazio_retornaTodos` | Array com 2 produtos para `?q=`. |
| `search_semResultados_retornaListaVazia` | Array vazio `[]` para termo sem correspondência. |

---

### 4.7 `CustomerRepositoryTest` — 8 casos

Arquivo: `src/test/java/com/dionialves/snapdogdelivery/domain/admin/customer/CustomerRepositoryTest.java`

Contexto Spring completo com H2. Cada teste roda em transação revertida. O `setUp` persiste dois clientes reais (`João Silva` e `Maria Santos`) para servir de base.

| Método de Teste | O que valida |
|---|---|
| `findByNameOrPhone_porNome_retornaCliente` | Busca por `"joão"` (minúscula) encontra `João Silva` — valida o `IgnoreCase`. |
| `findByNameOrPhone_porTelefone_retornaCliente` | Busca por fragmento de telefone `"99876"` encontra `Maria Santos`. |
| `findByNameOrPhone_termoGeral_retornaResultado` | Busca por `"silva"` retorna exatamente 1 cliente. |
| `findByNameOrPhone_semCorrespondencia_retornaVazio` | Termo `"xyz"` retorna lista vazia — sem exceção. |
| `findByNameOrPhone_paginado_retornaPage` | `Page` com `pageSize=10` contém pelo menos os 2 clientes do setUp. |
| `findByNameOrPhone_paginadoTamanho1_retornaUmPorPagina` | `pageSize=1` retorna 1 resultado por página mas `totalElements >= 2`. |
| `countByCreatedAt_hoje_retornaContagem` | Os 2 clientes criados no setUp (com `createdAt = LocalDate.now()`) são contados. |
| `countByCreatedAt_dataSemClientes_retornaZero` | Data de 1 ano atrás retorna 0 — confirma que o filtro de data funciona. |

---

### 4.8 `OrderRepositoryTest` — 6 casos

Arquivo: `src/test/java/com/dionialves/snapdogdelivery/order/OrderRepositoryTest.java`

Testa as queries JPQL customizadas que não podem ser verificadas com mocks. O `setUp` persiste 1 cliente e 1 produto base; cada teste cria seus próprios pedidos.

| Método de Teste | O que valida |
|---|---|
| `countByCreatedAtBetween_contaApenasNoPeriodo` | 1 pedido de hoje + 1 de ontem → `countByCreatedAtBetween(hoje_início, hoje_fim)` retorna 1. |
| `sumRevenueByCreatedAtBetween_somaApenasDoPeriodo` | 2 unidades × R$ 15,90 hoje + 1 unidade ontem → soma do período retorna R$ 31,80. |
| `sumRevenueByCreatedAtBetween_semPedidos_retornaZero` | Sem pedidos no período, `COALESCE(SUM(...), 0)` retorna `BigDecimal.ZERO` em vez de `null`. |
| `existsByClientId_comPedido_retornaTrue` | Após persistir 1 pedido para o cliente, `existsByClientId` retorna `true`. |
| `existsByClientId_semPedido_retornaFalse` | Sem pedidos persistidos, retorna `false` — usado como guard antes de deletar cliente. |
| `findTopSellingProducts_retornaOrdenadoPorQuantidade` | 2 pedidos com 3 e 5 unidades do mesmo produto → `totalSold = 8`, query `GROUP BY + SUM` está correta. |

---

### 4.9 `GreendogdeliveryApplicationTests` — 1 caso

Arquivo: `src/test/java/com/dionialves/snapdogdelivery/GreendogdeliveryApplicationTests.java`

Teste de fumaça (smoke test): sobe o contexto Spring completo com perfil `test` e verifica que a aplicação inicializa sem erros. Detecta problemas de configuração, beans mal definidos e dependências circulares.

---

## 5. Funcionalidades Ativas vs. Cobertura de Testes

### Legenda
- **Coberto** — existe ao menos um teste automatizado cobrindo o caminho principal e os caminhos de erro.
- **Parcial** — cobertura existe mas há caminhos relevantes sem teste.
- **Não coberto** — funcionalidade ativa sem nenhum teste.

---

### 5.1 Domínio: Cliente (`customer/`)

| Funcionalidade | Classe Testada | Status |
|---|---|:---:|
| Criar cliente | `CustomerServiceTest` | Coberto |
| Atualizar cliente | `CustomerServiceTest` | Coberto |
| Excluir cliente sem pedidos | `ClientServiceTest` | Coberto |
| Bloquear exclusão com pedidos | `ClientServiceTest` | Coberto |
| Buscar por nome/telefone (lista) | `ClientServiceTest`, `ClientRepositoryTest` | Coberto |
| Buscar paginado | `ClientServiceTest`, `ClientRepositoryTest` | Coberto |
| Buscar por ID | `ClientServiceTest` | Coberto |
| Contar clientes por data | `ClientRepositoryTest` | Coberto |
| Endpoint REST `GET /search` | `ClientControllerTest` | Coberto |
| Formulário Thymeleaf (CRUD visual) | — | **Não coberto** |
| Flash messages de sucesso/erro | — | **Não coberto** |

---

### 5.2 Domínio: Produto (`product/`)

| Funcionalidade | Classe Testada | Status |
|---|---|:---:|
| Criar produto | `ProductServiceTest` | Coberto |
| Atualizar produto | `ProductServiceTest` | Coberto |
| Excluir produto | `ProductServiceTest` | Coberto |
| Buscar por nome (lista) | `ProductServiceTest` | Coberto |
| Buscar paginado | `ProductServiceTest` | Coberto |
| Buscar por ID | `ProductServiceTest` | Coberto |
| Endpoint REST `GET /search` | `ProductControllerTest` | Coberto |
| Formulário Thymeleaf (CRUD visual) | — | **Não coberto** |
| Guard de integridade referencial no delete | — | **Não coberto** |

> `ProductService.delete` não possui guarda de `existsByProductId` em pedidos (ao contrário do `ClientService`). Não há teste pois a regra não existe na aplicação.

---

### 5.3 Domínio: Pedido (`order/`)

| Funcionalidade | Classe Testada | Status |
|---|---|:---:|
| Criar pedido com produtos | `OrderServiceTest` | Coberto |
| Criar pedido com cliente inválido | `OrderServiceTest` | Coberto |
| Criar pedido com produto inválido | `OrderServiceTest` | Coberto |
| Avanço de status PENDING → PREPARING | `OrderServiceTest` | Coberto |
| Avanço de status PREPARING → OUT_FOR_DELIVERY | `OrderServiceTest` | Coberto |
| Avanço de status OUT_FOR_DELIVERY → DELIVERED | `OrderServiceTest` | Coberto |
| Cancelamento de pedido PENDING | `OrderServiceTest` | Coberto |
| Bloqueio de cancelamento em PREPARING | `OrderServiceTest` | Coberto |
| Bloqueio de transição inválida (pular etapa) | `OrderServiceTest` | Coberto |
| Bloqueio de alteração de status final (DELIVERED/CANCELED) | `OrderServiceTest` | Coberto |
| Excluir pedido | `OrderServiceTest` | Coberto |
| Buscar por ID | `OrderServiceTest` | Coberto |
| Busca com filtros (status + cliente) | — | **Parcial** |
| Somar receita por período | `OrderRepositoryTest` | Coberto |
| Contar pedidos por período | `OrderRepositoryTest` | Coberto |
| Verificar existência por cliente | `OrderRepositoryTest` | Coberto |
| Top produtos mais vendidos | `OrderRepositoryTest` | Coberto |
| Formulário Thymeleaf (criação visual) | — | **Não coberto** |
| Snapshot de preço (`priceAtTime`) | — | **Parcial** |

> A busca com filtros (`OrderService.search(OrderStatus, String)`) tem o serviço testado implicitamente via `OrderServiceTest.findById`, mas os cenários de filtro combinado (status + nome do cliente) não têm caso de teste dedicado.

---

### 5.4 Domínio: Dashboard (`dashboard/`)

| Funcionalidade | Classe Testada | Status |
|---|---|:---:|
| Resumo do dia (pedidos, receita, ticket médio, novos clientes) | `DashboardServiceTest` | Coberto |
| Cálculo de crescimento percentual (pedidos, receita, ticket, clientes) | `DashboardServiceTest` | Coberto |
| Proteção contra divisão por zero (ticket médio, crescimento) | `DashboardServiceTest` | Coberto |
| Pedidos recentes (últimos 5) | `DashboardServiceTest` | Coberto |
| Produtos mais vendidos (top 5) | `DashboardServiceTest` | Coberto |
| Renderização Thymeleaf do painel | — | **Não coberto** |

---

### 5.5 Infraestrutura e Segurança (`infra/`, `auth/`, `exception/`)

| Funcionalidade | Classe Testada | Status |
|---|---|:---:|
| Contexto Spring inicializa sem erros | `GreendogdeliveryApplicationTests` | Coberto |
| `GlobalExceptionHandler` — resposta JSON para `NotFoundException` | — | **Não coberto** |
| `GlobalExceptionHandler` — resposta JSON para `BusinessException` | — | **Não coberto** |
| `GlobalExceptionHandler` — ModelAndView para rotas Thymeleaf | — | **Não coberto** |
| Login com credenciais válidas | — | **Não coberto** |
| Login com credenciais inválidas | — | **Não coberto** |
| Acesso a rota protegida sem autenticação | — | **Não coberto** |

---

### 5.6 Resumo de Cobertura por Domínio

| Domínio | Coberto | Parcial | Não coberto |
|---|:---:|:---:|:---:|
| Cliente | 10 | 0 | 2 |
| Produto | 7 | 0 | 2 |
| Pedido | 13 | 2 | 2 |
| Dashboard | 5 | 0 | 1 |
| Segurança / Infra | 1 | 0 | 6 |
| **Total** | **36** | **2** | **13** |

---

## 6. O que Não Foi Testado e Por Quê

### 6.1 ViewControllers e templates Thymeleaf

Os `ClientViewController`, `ProductViewController` e `OrderViewController` renderizam páginas HTML com `Model`, `RedirectAttributes` e flash messages. Testá-los requer `@SpringBootTest` com contexto completo + `MockMvc` autenticado — custo alto por valor marginal neste momento. A prioridade foram as regras de negócio.

### 6.2 `GlobalExceptionHandler`

O handler tem dois comportamentos distintos (JSON para `/admin/api/**`, `ModelAndView` para rotas Thymeleaf). Cobrir os dois requer testes de integração com contexto Security ativo. Fica como próxima iteração.

### 6.3 `CustomUserDetailsService` e fluxo de login

Testam a integração entre Spring Security e `UserRepository`. Exigem `@SpringBootTest` com contexto Security completo ou testes de slice de segurança. Não incluídos nesta iteração.

### 6.4 `OrderService.search` com filtros combinados

A busca com `OrderStatus` + `clientSearch` via `Specification` tem cobertura implícita (a `OrderSpecifications` é uma utility pura testável unitariamente), mas não há casos de teste exercitando as combinações possíveis (ambos nulos, status preenchido, busca preenchida, ambos preenchidos).

---

## 7. Próximos Passos de Testes (seção 5 do backlog — itens pendentes)

| Item | Prioridade | Observação |
|---|---|---|
| `OrderViewControllerTest` — criação, avanço de status, cancelamento via formulário | Alta | Depende de `@SpringBootTest` + MockMvc autenticado com `@WithMockUser` |
| `GlobalExceptionHandlerTest` — JSON e ModelAndView | Alta | Crítico para v1.0 — erros sem tratamento correto chegam ao usuário |
| `SecurityTest` — login, logout, acesso sem autenticação | Alta | Base para área pública (seção 1 do backlog) |
| `OrderServiceTest` — cenários de busca com filtros | Média | Cobrir os 4 casos do `Specification` |
| `StoreControllerTest` (futuro) — catálogo público sem login | Média | Será criado junto com a área pública (item 1.3 do backlog) |
| `CartControllerTest` (futuro) — adicionar, remover, atualizar | Média | Será criado junto com o carrinho (item 1.4 do backlog) |
| `CheckoutServiceTest` / `CheckoutControllerTest` (futuro) | Média | Checkout é o fluxo crítico de negócio da v1.0 |
