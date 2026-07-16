# Autorização independente do método de login e envio de convite por log em dev

O Local Login (ADR-0004) introduz um segundo método de autenticação ao lado do SSO/OIDC. Isso expôs duas decisões de implementação que orientam fatias futuras.

## Resolução do usuário atual independente do método de login

Antes do Local Login, todo o backend obtinha o usuário autenticado injetando `@AuthenticationPrincipal OidcUser` — controllers de Applications/master data, `AccessScopeService`, `EditPermissionService` e `CurrentUserController`. Um usuário de Local Login **não** é um `OidcUser` (é um `UserDetails`), então o principal viria `null` para ele, quebrando o Access Scope e o Edit Permission.

Decidimos centralizar a resolução da identidade em `security/CurrentUserService`, que deriva e-mail, nome, Role e flag de Admin a partir do `Authentication` do Spring Security, tratando tanto `OidcUser` (SSO) quanto `UserDetails` (Local Login). Controllers passaram a receber `Authentication` em vez de `OidcUser`, e os serviços de autorização passaram a resolver o usuário via `CurrentUserService`. Assim a autorização (Role, Access Scope, Edit Permission) fica **independente do método de login**, como exige o ADR-0004.

## Envio do convite: SMTP em produção, log em ambiente de dev

O convite de definição de senha exige infraestrutura de e-mail (ADR-0004), indisponível no ambiente de desenvolvimento/testes. Modelamos o envio atrás da interface `InvitationMailer`:

- `SmtpInvitationMailer` (via `JavaMailSender`) é ativado apenas quando `spring.mail.host` está configurado.
- `LoggingInvitationMailer` é o fallback padrão e **loga o link de convite**, o que o critério de aceite da fatia permite explicitamente ("ou log do envio em ambiente de dev").

## Consequences

- Novo código de autorização deve resolver o usuário atual via `CurrentUserService`/`Authentication`, nunca assumindo `OidcUser`.
- O token de convite (`local_login_invitations`) é de uso único e expira; a senha é armazenada como hash BCrypt em `catalog_users.password_hash`, nunca exposto por read models.
- Para habilitar SMTP real, basta definir `spring.mail.*` (e opcionalmente `app.local-login.mail-from`); sem isso, o fluxo continua utilizável via log.
- Existe uma página de login própria (`/login.html`) que oferece o formulário local e o botão SSO; usuários não autenticados passam a ser direcionados a ela em vez de irem direto ao Keycloak.
