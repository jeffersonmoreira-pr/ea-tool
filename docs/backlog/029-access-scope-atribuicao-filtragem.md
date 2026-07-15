# 029 - Access Scope: Atribuicao E Filtragem De Visibilidade

## Tipo

AFK

## Pai

[001 - Application Portfolio MVP PRD](./001-application-portfolio-mvp-prd.md)

## O Que Construir

Implementar o Access Scope: um Admin atribui, pela tela de gestao de usuarios, um conjunto de Departments e/ou Business Areas que um Catalog User pode ver. Aplicar a filtragem nas listagens de Applications, Departments e Business Areas (um Catalog User sem Access Scope configurado nao ve nenhum desses registros); Vendors permanecem visiveis para todos, sem filtragem; o Role Admin ignora Access Scope e sempre ve o catalogo completo. O vocabulario e as decisoes estao em `CONTEXT.md` e [ADR-0005](../adr/0005-autorizacao-role-access-scope.md).

## Criterios De Aceite

- [ ] Um Admin consegue atribuir um Access Scope (lista de Departments/Business Areas) a um Catalog User pela tela de gestao de usuarios, seguindo o design da fatia 026.
- [ ] Um Catalog User sem Access Scope configurado nao ve nenhuma Application, Department ou Business Area nas listagens.
- [ ] Um Catalog User com Access Scope configurado ve apenas Applications, Departments e Business Areas dentro do escopo atribuido.
- [ ] Vendors continuam visiveis para qualquer Catalog User autenticado, independente de Access Scope.
- [ ] Um Catalog User com Role Admin sempre ve o catalogo completo, independente de Access Scope.
- [ ] Testes automatizados cobrindo a filtragem de visibilidade para os diferentes cenarios de Access Scope.

## Bloqueado Por

- [024 - Migrar Vendors, Business Areas E Applications Para API Backend](./024-migrar-vendors-business-areas-applications-api.md)
- [027 - Tela De Administracao De Catalog Users](./027-tela-administracao-catalog-users.md)

## Verificacao

Criar um Catalog User de teste sem Access Scope e confirmar que ele nao ve nenhuma Application/Department/Business Area; atribuir um Access Scope limitado a um Department e confirmar que apenas os registros daquele Department aparecem; confirmar que Vendors continuam visiveis; confirmar que um Admin sem Access Scope configurado ainda ve tudo.
