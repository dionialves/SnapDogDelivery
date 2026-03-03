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

---

### Área Pública


---

### Infraestrutura

---

## Bugs Conhecidos


---

## Funcionalidades Futuras (pós v1.0)

| Funcionalidade | Descrição |
|---|---|
| **Retirada de background de imagem** | No editor de imagem de produto, opção para remover o fundo automaticamente (fundo transparente). |
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

*Atualizado em: 03/03/2026 (Flyway migrations concluído)*
