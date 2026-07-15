# 021 - Login Via SSO (OIDC/Keycloak) Com Sessao De Cookie

## Tipo

HITL

## Pai

[001 - Application Portfolio MVP PRD](./001-application-portfolio-mvp-prd.md)

## O Que Construir

Configurar Spring Security como cliente OIDC apontando para o Keycloak de desenvolvimento (fatia 020), com sessao baseada em cookie no servidor. Ao acessar o app, um usuario nao autenticado deve ser redirecionado ao login do Keycloak; apos autenticar, a UI deve mostrar o e-mail do usuario logado e permitir logout. As decisoes de protocolo e sessao estao registradas em [ADR-0004](../adr/0004-autenticacao-dupla-sso-local-login.md).

## Criterios De Aceite

- [ ] Acessar qualquer rota do app sem sessao valida redireciona para o login OIDC do Keycloak.
- [ ] Apos login bem-sucedido, o usuario retorna ao app com uma sessao de cookie valida.
- [ ] A UI exibe o e-mail (ou nome) do usuario autenticado em algum ponto visivel (ex. cabecalho).
- [ ] Existe uma acao de logout que encerra a sessao e exige novo login para acessar o app novamente.

## Bloqueado Por

- [020 - Esqueleto Do Backend Spring Boot Com Docker Compose](./020-backend-spring-boot-docker-compose.md)

## Verificacao

Com o Keycloak de dev rodando, criar um usuario de teste no realm, acessar o app pelo navegador, confirmar o redirecionamento para o login, autenticar, confirmar que o e-mail aparece na UI, fazer logout e confirmar que o acesso volta a exigir login.
