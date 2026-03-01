# Roadmap — SnapDog Delivery

Itens planejados e pendentes para o lançamento da v1.0 e além.
Quando um item for concluído, mova-o para `CHANGELOG.md` com data e detalhes de implementação.

---

## Melhorias

### Melhorar design da logo para maior integração com o layout

A logo atual (`/image/logo.png`) foi integrada nos templates, mas seu design visual não se
harmoniza bem com o layout do sistema. Reavaliar cores, proporções e estilo da logo para
que ela se integre naturalmente tanto no fundo branco (área pública, cadastro, mobile) quanto
no fundo vermelho (sidebar admin, painel esquerdo do login).

---

## Features Planejadas

### Area Admin

#### Criar um editor de imagem para produtos

Criar um editor de imagem se abre quando o upload de imagem e aberto, desse editor tem um preview da imagem, e tem as seguintes opções:

- Retirada de background (fundo transparente)
- Preview de como ficaria do card da area publica
- Aproximar ou afastar a imagem para melhor enquadramento

---

#### Informações da empresa no painel admin

Criar uma seção de "Configurações da Empresa" no painel admin para armazenar informações como:

- Copyright
- telefone de contato
- email de contato
- endereço físico
- e outros

---

### Área Pública

#### Categoria de tipos de produtos

Criar categorias diferentes de produtos como HotDog e Bebidas, e mostras no cardapio separado por categoria.

---

#### Flag de produto em destaque no catálogo público

**Arquivos:** `domain/admin/product/Product.java`, `domain/admin/product/ProductService.java`,
`templates/admin/products/form.html`, `templates/public/index.html`

Atualmente, os destaques da landing page exibem os 6 primeiros produtos ativos em ordem
alfabética. A melhoria propõe permitir que o admin marque explicitamente quais produtos devem
aparecer em destaque.

**Regra de negócio:**
- Cada produto terá um campo booleano `featured` (padrão `false`).
- No máximo **6 produtos** podem ter `featured = true` simultaneamente.
- Ao tentar marcar um 7º produto como destaque, o sistema deve lançar `BusinessException`
  informando que o limite foi atingido.
- Se o número de produtos com `featured = true` for menor que 6, a landing page complementa
  a exibição com produtos ativos em ordem alfabética até totalizar 6 itens.
- Produtos inativos nunca aparecem nos destaques, mesmo que `featured = true`.

**Solução planejada:**
- Adicionar coluna `featured boolean NOT NULL DEFAULT false` à entidade `Product`
- Em `ProductService`: validar limite de 6 ao salvar/atualizar um produto com `featured = true`
- Atualizar `ProductService.findFeatured()`: buscar primeiro os `featured = true` e ativos;
  se forem menos de 6, complementar com ativos ordenados por nome até atingir 6
- Adicionar checkbox "Produto em destaque" no formulário admin de produto
- Exibir badge visual "Destaque" na listagem admin de produtos para produtos marcados

---

#### Upload de arquivo no formulário admin de produto
**Arquivos:** `templates/admin/products/form.html`

Backend de upload já implementado (`StorageService`, `ProductViewController`, `WebMvcConfig`).
Ainda faltam:
- Campo `<input type="file">` no formulário admin como alternativa à URL externa
- JavaScript vanilla para alternar entre os dois modos (upload de arquivo vs. URL externa)

---

#### Modal de quantidade ao adicionar ao carrinho
**Arquivos:** `templates/public/store/catalog.html`, `templates/public/store/product-detail.html`

O botão "Adicionar" posta diretamente com `quantity=1` fixo.

**Solução planejada:**
- Substituir o `<form>` de POST direto por um botão que abre um modal JS
- Modal exibe: imagem, nome, descrição e campo numérico de quantidade (min 1, max 20, padrão 1)
- Botão "Adicionar ao carrinho" dentro do modal submete `POST /cart/add` com `productId` e `quantity`
- Acessível via teclado (fechar com `Esc`, foco retorna ao botão de origem)
- JavaScript vanilla + Tailwind, sem dependências externas

---

#### Máscaras de telefone e CEP no cadastro de cliente
**Arquivos:** `templates/public/auth/register.html`, `templates/public/account/profile.html`

**Solução planejada:**
- Máscara JavaScript no campo `#phone` → `(00) 00000-0000`
- Máscara JavaScript no campo `#zipCode` → `00000-000`
- Evento `input` + regex, JavaScript vanilla, sem bibliotecas externas

---

#### Auto-preenchimento de endereço por CEP (ViaCEP)
**Arquivos:** `templates/public/auth/register.html`, `templates/public/account/profile.html`

**Solução planejada:**
- No `blur` do campo `#zipCode` com 8 dígitos: `fetch('https://viacep.com.br/ws/{cep}/json/')`
- Preencher automaticamente: `street`, `neighborhood`, `city`, `state`
- Spinner discreto durante a requisição; mensagem de erro inline se CEP não encontrado
- Campos preenchidos permanecem editáveis
- JavaScript vanilla, sem dependências externas

---

#### Campo `origin` visível na área admin
**Arquivos:** `templates/admin/orders/list.html`, `templates/admin/orders/form.html`

Backend já implementado (enum `OrderOrigin`, campo `origin` em `Order` e DTOs). Ainda faltam:
- Coluna ou badge "Origem" (Online / Manual) em `admin/orders/list.html`
- Exibição do origin na visualização do pedido em `admin/orders/form.html`
- Filtro opcional por origin na listagem

---

#### Crédito do criador no footer público
**Arquivo:** `templates/public/fragments/layout.html` (~linha 187)

Adicionar abaixo do copyright:
```
Desenvolvido por <a href="https://www.dionialves.com" target="_blank" rel="noopener">Dioni Alves</a>
```
Estilo discreto (`text-xs`, `text-gray-400`), alinhado com o visual atual do footer.

---

### Infraestrutura

#### Migrations com Flyway
**Arquivo:** `src/main/resources/application.properties`

**Problema crítico:** `ddl-auto=create-drop` destrói e recria todas as tabelas a cada restart.

**Solução planejada:**
1. Alterar para `validate` em produção
2. Adicionar `flyway-core` + `flyway-database-postgresql` ao `pom.xml`
3. Criar scripts em `src/main/resources/db/migration/`:
   - `V1__create_initial_schema.sql`
   - `V2__add_product_image_url.sql`
   - `V3__add_product_active.sql`
   - `V4__add_order_origin.sql`
   - `V5__add_order_delivery_address.sql`
   - (migrations seguintes conforme novas features)

---

## Bugs Conhecidos


### Criar page 400 404 e 500 para a area publica

Customizar paginas exclusivas para a area publica, seguindo o layout atual, para os erros HTTP 400, 404 e 500. Atualmente, o sistema esta utilizando as paginas em /error, que foram estilizadas para serem usadas no admin.

Etapas

- Crias as paginas `400.html`, `404.html` e `500.html` em `templates/public/error/`
- Modificar as paginas em `/error` para `templates/admin/error/`, pois forame stilizadas para o admin
- Ajustar sistema para buscar as paginas corretamente, dependendo da origem do erro (admin vs público).

---

### Erro 500 ao tentar excluir produto com pedidos associados

**Arquivo:** `src/main/java/com/dionialves/snapdogdelivery/domain/admin/product/ProductService.java`

**Sintoma:** Ao tentar excluir um produto que possui pedidos associados, o sistema retorna erro
HTTP 500 em vez de uma mensagem de negócio amigável.

**Causa:** `ProductService.delete()` chama `deleteById()` sem verificar se o produto está
referenciado em `tb_product_orders`. O banco lança uma `ConstraintViolationException` que não
é tratada.

**Regra de negócio esperada:** Produtos com pedidos associados **não podem ser excluídos**,
apenas desativados (`active = false`). A exclusão deve ser bloqueada com uma `BusinessException`
clara para o usuário.

**Solução planejada:**
- Em `ProductService.delete()`: verificar se existe algum `ProductOrder` com o `product_id`
  antes de deletar, e lançar `BusinessException` caso exista
- Atualizar a UI (`admin/products/list.html`) para exibir o botão "Desativar" como alternativa
  ao "Excluir" quando o produto tiver pedidos associados

---


## Funcionalidades Futuras (pós v1.0)

| Funcionalidade | Descrição |
|---|---|
| **Login com Google (OAuth2)** | `spring-boot-starter-oauth2-client` + Google Cloud Console. Cria/vincula `Customer` automaticamente. |
| **Campo `notes` em `Order`** | Campo `String` (max 500, nullable) para observações do cliente (ex.: "sem cebola"). |
| **E-mail de confirmação de pedido** | Spring Mail + template HTML enviado após checkout. |
| **Notificação de mudança de status** | E-mail ou push notification ao avançar status do pedido. |
| **Taxa de entrega dinâmica** | Campo `deliveryFee` em `Order`, calculada por CEP ou valor fixo por bairro. |
| **Cupom de desconto** | Entidade `Coupon` com código, tipo (percentual/fixo), validade e limite de usos. |
| **Status do pedido em tempo real** | WebSocket ou polling para acompanhar `PENDING → DELIVERED` sem recarregar. |
| **Relatórios admin** | Exportação de pedidos em CSV/PDF, gráficos de faturamento por período. |
| **Múltiplos endereços por cliente** | `Address` como entidade separada. Cliente escolhe no checkout. |
| **Avaliação de produtos** | `Review` com nota (1–5) e comentário, visível no catálogo. |
| **Estoque** | Campo `stock` em `Product`, decremento no checkout, alerta quando zerado. |
| **PWA / app mobile** | Progressive Web App com manifest e service worker. |
| **Gestão de área de entrega** | Definir bairros/CEPs atendidos; validar no checkout. |

---

*Atualizado em: 01/03/2026*
