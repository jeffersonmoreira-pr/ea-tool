# Edição autorizada por Edit Permission por registro, não por Access Scope

Considerou-se restringir a edição pelo mesmo Access Scope usado para visibilidade (por Department/Business Area), mas isso não cobre Vendors (que não têm Department/Business Area próprio) nem permite conceder edição de um registro específico sem abrir todo um Department/Business Area inteiro. Decidimos que a edição é autorizada por Edit Permission: um grant explícito por registro individual (uma Application, um Vendor, um Department ou uma Business Area específicos), concedido apenas por um Admin, para um Catalog User que já tenha o Role Editor. Role Editor continua sendo pré-requisito; Edit Permission apenas restringe quais registros específicos esse Editor pode alterar.

## Considered Options

- Restringir edição por Department/Business Area (mesmo mecanismo do Access Scope): rejeitada por não cobrir Vendors e por ser menos granular do que o necessário.
- Conceder Edit Permission por grupo/categoria (ex.: "editar qualquer Application de Finanças"): rejeitada em favor de concessão por registro individual, mais granular.

## Consequences

- Cada Application, Vendor, Department e Business Area precisa manter sua própria lista de Catalog Users com Edit Permission, aumentando a complexidade do modelo de dados e da tela de gestão de permissões em comparação com um controle só por Role.
