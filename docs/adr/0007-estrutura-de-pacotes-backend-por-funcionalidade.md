# Estrutura de pacotes do backend organizada por funcionalidade

O backend Spring Boot (ADR-0003) cresce por domínio: Applications, master data (Vendors, Departments, Business Areas), Catalog Users, autenticação e autorização. Precisávamos de uma convenção de organização de pacotes Java que fosse consistente, fácil de evoluir e que os agentes pudessem seguir sem inventar padrões próprios a cada fatia. Decidimos organizar o código **por funcionalidade (package-by-feature)** e não por camada técnica (package-by-layer). Cada pacote de domínio reúne todos os seus artefatos — entidade, `Repository`, `Controller`, `Service`, DTOs de request e normalizadores — em vez de separá-los em pastas globais `controller/`, `service/`, `repository/`, `model/`.

A estrutura resultante é:

```
com.eatool.backend
├── applications/     entidade + Controller + Service + Repository + Request + Normalizer
├── masterdata/       Department, Vendor, BusinessArea (cada um com Controller/Repository/Request)
├── catalogusers/     CatalogUser, Role, provisionamento
├── security/         SecurityConfig, filtros
├── common/           exceções compartilhadas + ApiExceptionHandler
└── web/              controllers transversais (ex.: CurrentUserController)
```

Esta decisão formaliza, de forma retroativa, o padrão que o backend já vinha seguindo desde as primeiras fatias (`catalogusers/`, `security/`, `web/`) e que foi mantido ao migrar master data e Applications para a API (fatias 023/024).

## Motivos

- **Coesão e localidade de mudança:** tudo que muda junto fica junto; ao mexer numa funcionalidade, todos os arquivos relevantes estão em um único pacote.
- **Encapsulamento:** permite visibilidade *package-private* (ex.: manter `Repository` acessível só dentro da feature) em vez de forçar `public` para cruzar pacotes de camada.
- **Consistência:** evita misturar dois estilos de organização no mesmo projeto.
- **Alinhamento com Spring Boot:** package-by-feature é a recomendação moderna; package-by-layer escala mal conforme o número de entidades cresce.

## Consequences

- Novos domínios devem ganhar seu próprio pacote (`com.eatool.backend.<feature>`) reunindo entidade, repository, controller, service e DTOs, em vez de arquivos espalhados por pacotes de camada.
- Código genuinamente transversal (exceções, tratamento de erro, config de segurança, controllers utilitários) vive em `common/`, `security/` ou `web/`, não duplicado por feature.
- Dependências entre pacotes de domínio são aceitáveis quando o domínio exige (ex.: `masterdata` depende de `applications.ApplicationRepository` para bloquear exclusão de master data referenciada); não se deve quebrar essa regra apenas para evitar o acoplamento entre pacotes.
- Agentes futuros não precisam decidir a organização a cada fatia: seguem esta convenção por padrão.
