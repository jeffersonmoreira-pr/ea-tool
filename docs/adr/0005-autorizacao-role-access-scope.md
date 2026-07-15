# Autorização por Role e Access Scope, com padrão nega-tudo

Com múltiplos Catalog Users (internos via SSO e externos via Local Login), o catálogo precisa controlar tanto o que cada um pode fazer (autorização por ação) quanto o que pode ver (autorização por dado). Decidimos separar essas duas dimensões: Role (Viewer, Editor, Admin) controla ações permitidas e é pré-requisito para editar, e Access Scope (conjunto explícito de Departments e/ou Business Areas) controla apenas a visibilidade de Applications, aplicável a qualquer método de login. Um Catalog User sem Access Scope configurado não vê nenhuma Application até um Admin atribuir um escopo; essa é uma escolha deliberada de "nega tudo por padrão" mesmo para novos usuários SSO auto-provisionados como Viewer, priorizando segurança sobre fricção zero de onboarding. O Role Admin é a única exceção e sempre enxerga o catálogo completo, independente de Access Scope. A autorização de escrita (edição) não é controlada por Access Scope; ela é tratada separadamente por Edit Permission (ver ADR-0006).

## Consequences

- Todo novo usuário (mesmo via SSO) precisa de intervenção de um Admin para configurar o Access Scope antes de ver qualquer Application, reintroduzindo fricção manual que o auto-provisionamento de Role tentava evitar.
