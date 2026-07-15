# 027 - Tela De Administracao De Catalog Users

## Tipo

AFK

## Pai

[001 - Application Portfolio MVP PRD](./001-application-portfolio-mvp-prd.md)

## O Que Construir

Construir a tela (visivel apenas para Admin) de listagem e gestao de Catalog Users, permitindo visualizar todos os usuarios provisionados, seu metodo de login (SSO ou Local Login) e promover/rebaixar seu Role (Viewer, Editor, Admin), seguindo o design gerado na fatia 026.

## Criterios De Aceite

- [ ] Apenas um Catalog User com Role Admin consegue acessar a tela de gestao de usuarios (Viewer/Editor recebem acesso negado).
- [ ] A tela lista todos os Catalog Users com nome/e-mail, metodo de login e Role atual.
- [ ] Um Admin consegue alterar o Role de outro Catalog User pela tela, e a mudanca reflete imediatamente na autorizacao da API.
- [ ] A UI segue o design gerado na fatia 026.

## Bloqueado Por

- [022 - Auto-Provisionamento De Catalog User E Bootstrap Do Primeiro Admin](./022-auto-provisionamento-catalog-user-admin-seed.md)
- [025 - Autorizacao Por Role Nos Endpoints Da API](./025-autorizacao-role-endpoints-api.md)
- [026 - Design De UI/UX Para Telas De Autenticacao E Gestao De Usuarios](./026-design-ui-ux-autenticacao-gestao-usuarios.md)

## Verificacao

Logar como Admin, acessar a tela de gestao de usuarios, promover um usuario Viewer para Editor, deslogar e logar como esse usuario confirmando que ele agora consegue editar; tentar acessar a tela como Viewer/Editor e confirmar bloqueio.
