# 025 - Autorizacao Por Role Nos Endpoints Da API

## Tipo

AFK

## Pai

[001 - Application Portfolio MVP PRD](./001-application-portfolio-mvp-prd.md)

## O Que Construir

Aplicar autorizacao por Role (Viewer, Editor, Admin) em todos os endpoints REST de Applications, Vendors, Departments e Business Areas: Viewer so pode ler; Editor e Admin podem criar, editar e excluir. O vocabulario de Role esta definido no `CONTEXT.md` e a separacao entre Role e Access Scope/Edit Permission esta registrada em [ADR-0005](../adr/0005-autorizacao-role-access-scope.md) e [ADR-0006](../adr/0006-edit-permission-por-registro.md).

## Criterios De Aceite

- [ ] Um Catalog User com Role Viewer recebe 403 ao tentar criar, editar ou excluir qualquer entidade via API.
- [ ] Um Catalog User com Role Viewer consegue listar e visualizar normalmente.
- [ ] Catalog Users com Role Editor ou Admin conseguem criar, editar e excluir normalmente (ainda sem a restricao fina de Edit Permission, que sera tratada na fatia 027).
- [ ] Testes automatizados cobrindo os casos de autorizacao por Role (200 e 403) para cada entidade.

## Bloqueado Por

- [024 - Migrar Vendors, Business Areas E Applications Para API Backend](./024-migrar-vendors-business-areas-applications-api.md)

## Verificacao

Autenticar como um usuario com Role Viewer (definido manualmente no banco para teste) e confirmar que tentativas de escrita retornam 403; autenticar como Editor e confirmar que escrita funciona; rodar a suite de testes de autorizacao.
