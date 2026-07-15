# 022 - Auto-Provisionamento De Catalog User E Bootstrap Do Primeiro Admin

## Tipo

AFK

## Pai

[001 - Application Portfolio MVP PRD](./001-application-portfolio-mvp-prd.md)

## O Que Construir

No primeiro login bem-sucedido via SSO, criar automaticamente um Catalog User persistido no banco com Role Viewer por padrao. Suportar uma lista de e-mails "seed admin" via configuracao (ex. `application.yml`/variavel de ambiente): se o e-mail do usuario autenticado estiver nessa lista, o Catalog User e criado/atualizado como Admin em vez de Viewer. O vocabulario de Catalog User e Role esta definido no `CONTEXT.md`.

## Criterios De Aceite

- [ ] Um usuario que faz login pela primeira vez via SSO passa a existir como Catalog User no banco com Role Viewer.
- [ ] Um segundo login do mesmo usuario nao cria um novo registro, apenas reutiliza o Catalog User existente.
- [ ] Um e-mail presente na lista de configuracao "seed admin" resulta em um Catalog User com Role Admin no primeiro login.
- [ ] A lista de seed admins e configuravel sem alterar codigo (arquivo de configuracao ou variavel de ambiente).

## Bloqueado Por

- [021 - Login Via SSO (OIDC/Keycloak) Com Sessao De Cookie](./021-login-sso-oidc-keycloak.md)

## Verificacao

Configurar um e-mail de teste como seed admin, fazer login com esse usuario no Keycloak de dev e confirmar via banco/API que o Catalog User foi criado com Role Admin; fazer login com outro usuario nao listado e confirmar que ele recebe Role Viewer.
