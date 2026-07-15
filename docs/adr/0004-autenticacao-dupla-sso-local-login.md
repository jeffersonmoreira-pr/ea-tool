# Autenticação dupla: SSO como principal, Local Login para break-glass e parceiros

A autenticação corporativa via SSO (OIDC) é o caminho principal de login, mas dois cenários não são cobertos por ela: acesso de emergência caso o Identity Provider corporativo fique indisponível (break-glass), e usuários parceiros externos que não possuem conta no IdP corporativo. Em vez de criar dois mecanismos distintos, decidimos manter um único mecanismo secundário de autenticação, o Local Login (usuário/senha com hash), configurado via Spring Security como um segundo `AuthenticationProvider` ao lado do OIDC. Contas de Local Login são sempre criadas por um Admin (nunca autocadastro) e ativadas por um link de convite por e-mail para definição da senha.

## Considered Options

- Conta de break-glass hardcoded/config separada de um cadastro local completo para parceiros: rejeitada por manter dois sistemas de autenticação diferentes para o mesmo problema (login sem SSO).

## Consequences

- O backend precisa de infraestrutura de envio de e-mail (SMTP) para os convites de definição de senha.
- Catalog Users passam a ter mais de um método de login possível, e a autorização (Role, Access Scope) deve ser independente do método usado.
