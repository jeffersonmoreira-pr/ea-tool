# 026 - Design De UI/UX Para Telas De Autenticacao E Gestao De Usuarios

## Tipo

HITL

## Pai

[001 - Application Portfolio MVP PRD](./001-application-portfolio-mvp-prd.md)

## O Que Construir

Gerar, via Google Stitch, o design visual (mockup + `code.html` + estilo) das telas novas necessarias para autenticacao e gestao de usuarios: tela de login SSO, gestao de Catalog Users e Roles, criacao/gestao de contas Local Login, atribuicao de Access Scope e concessao de Edit Permission por registro. Seguir o mesmo padrao ja usado em `design/` (pasta por tela com `DESIGN.md`, `code.html` e `screen.png`), mantendo consistencia com o design system existente ("Strategic Enterprise Ledger").

## Criterios De Aceite

- [ ] Uma nova pasta em `design/` por tela (ex. `design/Login_SSO/`, `design/User_Management/`, `design/Local_Login_Account/`, `design/Access_Scope_Assignment/`, `design/Edit_Permission_Grant/`), cada uma com `DESIGN.md`, `code.html` e `screen.png`.
- [ ] O design reutiliza a paleta de cores, tipografia (Inter) e componentes ja definidos nos `DESIGN.md` existentes (ex. `design/Master_data_Management/DESIGN.md`), sem introduzir um novo sistema visual do zero.
- [ ] Os designs cobrem os estados relevantes ja decididos: usuario sem Access Scope (sem Applications visiveis), Admin gerenciando Roles e Edit Permission, tela de convite/definicao de senha do Local Login.

## Bloqueado Por

Nenhum - pode comecar imediatamente (pode ser feito em paralelo as fatias de backend)

## Verificacao

Revisao humana comparando os designs gerados com os termos e regras do `CONTEXT.md` e dos ADRs 0004, 0005 e 0006, confirmando que a nomenclatura das telas usa o vocabulario do projeto (Catalog User, Role, Local Login, Access Scope, Edit Permission).
