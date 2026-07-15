# 023 - Migrar Departments Para API Backend Persistida

## Tipo

HITL

## Pai

[001 - Application Portfolio MVP PRD](./001-application-portfolio-mvp-prd.md)

## O Que Construir

Migrar Departments de `localStorage` para uma API REST no backend Spring Boot, persistida em PostgreSQL, exigindo sessao autenticada para leitura e escrita. Esta fatia estabelece o padrao (schema, endpoints REST, integracao com o frontend existente) que sera repetido para Vendors, Business Areas e Applications na proxima fatia. A mudanca de armazenamento esta registrada em [ADR-0003](../adr/0003-backend-compartilhado-java-spring-postgres.md).

## Criterios De Aceite

- [ ] Entidade Department persistida em tabela PostgreSQL via JPA, respeitando a definicao de nome unico em `CONTEXT.md`.
- [ ] Endpoints REST (listar, criar, editar, excluir) protegidos por autenticacao (qualquer usuario logado pode ler; regra de escrita pode ser provisoria nesta fatia, refinada na fatia 026).
- [ ] Frontend (`catalog.js`/`app.js`) passa a ler e escrever Departments via essa API em vez de `localStorage`.
- [ ] Regra existente de bloqueio de exclusao de Department referenciado por Application continua funcionando, agora validada no backend.
- [ ] Testes automatizados cobrindo a API de Departments.

## Bloqueado Por

- [022 - Auto-Provisionamento De Catalog User E Bootstrap Do Primeiro Admin](./022-auto-provisionamento-catalog-user-admin-seed.md)

## Verificacao

Criar, editar e excluir um Department pela UI e confirmar que os dados persistem apos reiniciar o backend (nao apenas no navegador); tentar excluir um Department referenciado por uma Application e confirmar que o backend bloqueia a exclusao; rodar a suite de testes da API.
