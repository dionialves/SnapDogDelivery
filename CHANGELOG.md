# Changelog — SnapDog Delivery

Índice de versões lançadas. Cada versão a partir da **0.3.0** possui documentação completa
com regras de negócio, fluxos de trabalho e detalhes das alterações.

---

## [Não lançado] — develop

### Novas Funcionalidades

- **Migrations com Flyway** (03/03/2026)
  Adicionadas dependências `flyway-core` e `flyway-database-postgresql` ao `pom.xml`.
  Criado `V1__create_initial_schema.sql` com o schema completo (6 tabelas: `tb_users`,
  `tb_customers`, `tb_products`, `tb_company_settings`, `tb_orders`, `tb_product_orders`).
  Perfil `dev` alterado para `ddl-auto=none` — Flyway passa a ser responsável exclusivo
  pelo schema. Perfil `test` (H2) permanece com `ddl-auto=create-drop` e Flyway desabilitado.

- **Flag de produto em destaque no catálogo público** (03/03/2026)
  Adicionado campo `featured` (boolean, padrão `false`) na entidade `Product`. O admin pode
  marcar produtos como destaque via checkbox no formulário. Limite máximo de 6 produtos em
  destaque simultâneos — ao ultrapassar, `BusinessException` é lançada. `ProductService.findFeatured()`
  prioriza produtos com `featured=true` e ativos; complementa com ativos não-destacados em
  ordem alfabética até totalizar 6. Badge âmbar "Destaque" exibido na listagem admin.
  Novos testes de serviço cobrem os cenários de limite atingido e complemento de destaques.

- **Correção do layout da listagem de produtos** (03/03/2026)
  Removida a coluna "Descrição" que quebrava as linhas da tabela. Larguras redistribuídas
  com valores fixos: Nome `w-5/12` com truncamento por reticências, Preço `w-1/8`,
  Categoria `w-1/6`, Status `w-1/8`, Ações `w-1/6` alinhada à direita.

- **Refatoração do formulário de produto com drop zone e upload AJAX** (03/03/2026)
  Layout dividido em 2 colunas: campos à esquerda e área de imagem à direita. A zona de
  imagem suporta drag-and-drop ou clique para selecionar arquivo. Após confirmar o recorte,
  a imagem é enviada imediatamente via AJAX para o novo endpoint `POST /upload-image` e o
  preview é exibido na mesma área. Hover sobre o preview exibe X vermelho no canto superior
  direito para remover a imagem. O submit do formulário deixou de ser multipart — a URL da
  imagem trafega como campo hidden padrão.

- **Remoção da opção de URL externa no formulário de produto** (03/03/2026)
  Campo de URL externa removido do formulário de produto. A imagem agora é definida
  exclusivamente via upload de arquivo com editor de recorte. Um campo hidden preserva
  a URL existente em modo de edição quando nenhum novo arquivo é enviado.

- **Editor de recorte de imagem para produtos** (03/03/2026)
  Ao selecionar um arquivo na aba "Upload de arquivo" do formulário de produto, um modal abre
  automaticamente com o editor Cropper.js (via CDN). O admin pode mover a área de recorte,
  ajustar o zoom e alternar proporção entre Livre e 1:1. Ao confirmar, a imagem recortada
  (JPEG, máx 1200px) substitui o arquivo original no input via DataTransfer API — o backend
  recebe o arquivo já recortado sem nenhuma mudança no servidor.

- **Modal de quantidade ao adicionar ao carrinho** (01/03/2026)
  Substituídos os botões "Adicionar" (quantity=1 fixo) por triggers que abrem um modal com
  thumbnail, nome, descrição e seletor de quantidade (1–20). Imagem e nome do produto também
  abrem o modal. Na landing page, "Ver mais" passa a "Adicionar" para clientes autenticados.
  Página de detalhe individual (`product-detail.html`) removida e rota `/catalog/{id}` excluída.
  JavaScript vanilla, sem dependências externas.

- **Máscaras de telefone e CEP + autocomplete ViaCEP** (01/03/2026)
  Adicionadas máscaras JavaScript nos campos `#phone` (`(00) 00000-0000`) e `#zipCode`
  (`00000-000`) nas telas de cadastro e perfil do cliente. No blur do CEP com 8 dígitos,
  busca automática na API ViaCEP preenche rua, bairro, cidade e estado. Spinner discreto
  durante a requisição e mensagem de erro inline se CEP não encontrado. JavaScript vanilla,
  sem dependências externas.

- **Categorias de produtos no catálogo público** (01/03/2026)
  Adicionado enum `ProductCategory` (HOT_DOG, BEBIDA) e campo `category` na entidade `Product`.
  Catálogo público exibe duas seções separadas (Hot Dog acima, Bebidas abaixo) quando nenhum filtro
  está selecionado; tabs permitem filtrar por categoria com grid paginado. Painel admin exibe coluna
  "Categoria" na listagem e select no formulário de produto. DataSeeder atualizado com categorias.

- **Configurações da Empresa no painel admin** (01/03/2026)
  Criada seção `/admin/settings` com formulário para editar nome, e-mail, telefone, endereço,
  horário de funcionamento e copyright. Dados armazenados em `tb_company_settings` (singleton,
  ID sempre 1). Footer da área pública passa a exibir os dados da entidade em vez de valores
  hardcoded. `CompanySettingsAdvice` injeta o objeto em todos os controllers do storefront via
  `@ModelAttribute`. DataSeeder inicializa os dados padrão.

---

## [0.3.1] — 01/03/2026

Release de correções de bugs. Estabilização pós v0.3.0: páginas de erro HTTP por contexto
(admin/público), exclusão segura de produto com pedidos, ajustes de sessão, UI e PT-BR.

→ [Documentação completa](docs/v0.3.1.md)

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
