# Changelog — SnapDog Delivery

Índice de versões lançadas. Cada versão a partir da **0.3.0** possui documentação completa
com regras de negócio, fluxos de trabalho e detalhes das alterações.

---

## [Não lançado] — develop

### Novas Funcionalidades

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
