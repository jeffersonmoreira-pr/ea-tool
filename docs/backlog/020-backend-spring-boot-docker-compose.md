# 020 - Esqueleto Do Backend Spring Boot Com Docker Compose

## Tipo

AFK

## Pai

[001 - Application Portfolio MVP PRD](./001-application-portfolio-mvp-prd.md)

## O Que Construir

Criar o esqueleto de um backend em Java com Spring Boot que sirva os arquivos estaticos existentes em `src/` (mesma origem, sem CORS) e exponha um endpoint de health-check. Fornecer um `docker-compose.yml` de desenvolvimento com PostgreSQL e Keycloak, prontos para os proximos passos de autenticacao e persistencia. A decisao arquitetural que fundamenta esta fatia esta registrada em [ADR-0003](../adr/0003-backend-compartilhado-java-spring-postgres.md).

## Criterios De Aceite

- [ ] Projeto Spring Boot criado com Maven/Gradle, servindo `src/index.html` e demais arquivos estaticos na raiz da aplicacao.
- [ ] Endpoint de health-check (ex. `/actuator/health` ou equivalente) responde 200.
- [ ] `docker-compose.yml` sobe PostgreSQL e Keycloak localmente com um comando (`docker compose up`).
- [ ] README atualizado com instrucoes de como subir o backend e o `docker-compose.yml` localmente.

## Bloqueado Por

Nenhum - pode comecar imediatamente

## Verificacao

Rodar `docker compose up`, iniciar o backend Spring Boot, abrir a URL raiz do backend no navegador e confirmar que a tela atual do catalogo carrega normalmente; chamar o endpoint de health-check e confirmar resposta 200.
