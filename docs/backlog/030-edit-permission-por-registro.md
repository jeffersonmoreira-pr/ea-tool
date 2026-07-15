# 030 - Edit Permission Por Registro

## Tipo

AFK

## Pai

[001 - Application Portfolio MVP PRD](./001-application-portfolio-mvp-prd.md)

## O Que Construir

Implementar Edit Permission: um Admin concede ou revoga, por registro individual (uma Application, um Vendor, um Department ou uma Business Area especificos), a permissao de edicao para um Catalog User com Role Editor. Aplicar a restricao nos endpoints de escrita: um Editor sem Edit Permission no registro recebe 403 ao tentar editar/excluir aquele registro, mesmo tendo Role Editor. A decisao esta registrada em [ADR-0006](../adr/0006-edit-permission-por-registro.md).

## Criterios De Aceite

- [ ] Um Admin consegue conceder e revogar Edit Permission de um Editor para um registro especifico (Application, Vendor, Department ou Business Area), seguindo o design da fatia 026.
- [ ] Um Editor sem Edit Permission em um registro recebe 403 ao tentar editar ou excluir esse registro especifico.
- [ ] Um Editor com Edit Permission no registro consegue editar/excluir normalmente esse registro.
- [ ] Um Catalog User com Role Viewer continua sem poder editar nenhum registro, mesmo que uma Edit Permission seja concedida a ele por engano (Role continua sendo pre-requisito).
- [ ] Um Catalog User com Role Admin continua podendo editar qualquer registro, independente de Edit Permission.
- [ ] Testes automatizados cobrindo concessao, revogacao e enforcement de Edit Permission.

## Bloqueado Por

- [025 - Autorizacao Por Role Nos Endpoints Da API](./025-autorizacao-role-endpoints-api.md)
- [027 - Tela De Administracao De Catalog Users](./027-tela-administracao-catalog-users.md)

## Verificacao

Como Admin, conceder Edit Permission de uma Application especifica a um Editor de teste; logar como esse Editor e confirmar que ele consegue editar apenas essa Application, recebendo 403 ao tentar editar outra; revogar a permissao e confirmar que a edicao passa a ser bloqueada; confirmar que um Viewer com Edit Permission concedida por engano ainda nao consegue editar.
