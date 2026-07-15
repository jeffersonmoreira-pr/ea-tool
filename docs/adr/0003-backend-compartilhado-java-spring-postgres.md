# Backend compartilhado em Java/Spring Boot com PostgreSQL

O catálogo precisa suportar autenticação corporativa via SSO e múltiplos usuários vendo e editando o mesmo portfólio, o que é incompatível com o modelo browser-only/localStorage por navegador (ADR-0001). Decidimos introduzir um backend em Java com Spring Boot que expõe uma API REST e também serve os arquivos estáticos do frontend (`src/`) na mesma origem, com PostgreSQL como armazenamento compartilhado do catálogo (Applications, Vendors, Departments, Business Areas, Catalog Users). A escolha de Java/Spring Boot segue a familiaridade da equipe e o suporte de primeira classe do Spring Security para OIDC/SAML; servir tudo na mesma origem evita problemas de CORS e simplifica o fluxo de login OIDC via redirecionamento.

## Consequences

- O app deixa de ser "browser-only local-first"; passa a exigir o backend rodando para funcionar.
- O `localStorage` deixa de ser a fonte de verdade dos dados do catálogo.
