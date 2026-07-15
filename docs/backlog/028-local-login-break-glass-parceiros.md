# 028 - Local Login Para Break-Glass E Parceiros Externos

## Tipo

HITL

## Pai

[001 - Application Portfolio MVP PRD](./001-application-portfolio-mvp-prd.md)

## O Que Construir

Implementar o Local Login como segundo `AuthenticationProvider` no Spring Security, coexistindo com o login SSO/OIDC. Um Admin cria a conta de Local Login (nome, e-mail, Role inicial) pela tela de gestao de usuarios; o sistema envia um e-mail de convite via o relay SMTP corporativo com um link para o proprio usuario definir sua senha. A decisao e as razoes deste mecanismo estao registradas em [ADR-0004](../adr/0004-autenticacao-dupla-sso-local-login.md).

## Criterios De Aceite

- [ ] Um Admin consegue criar uma conta de Local Login pela tela de gestao de usuarios, informando nome, e-mail e Role inicial.
- [ ] O sistema envia um e-mail de convite (via SMTP corporativo) com um link de definicao de senha para a conta recem-criada.
- [ ] O link de convite permite ao usuario definir sua senha uma unica vez; links ja usados ou expirados sao rejeitados.
- [ ] Apos definir a senha, o usuario consegue autenticar por usuario/senha em uma tela de login local, coexistindo com o botao de login SSO.
- [ ] Contas de Local Login nunca sao criadas por autocadastro, apenas por um Admin.

## Bloqueado Por

- [027 - Tela De Administracao De Catalog Users](./027-tela-administracao-catalog-users.md)

## Verificacao

Como Admin, criar uma conta de Local Login de teste, confirmar recebimento do e-mail de convite (ou log do envio em ambiente de dev), definir a senha pelo link, fazer login com usuario/senha e confirmar acesso ao catalogo com o Role atribuido; tentar reutilizar o mesmo link de convite e confirmar rejeicao.
