# 024 - Migrar Vendors, Business Areas E Applications Para API Backend

## Tipo

AFK

## Pai

[001 - Application Portfolio MVP PRD](./001-application-portfolio-mvp-prd.md)

## O Que Construir

Repetir o padrao estabelecido na fatia 023 (schema JPA, endpoints REST, integracao com o frontend) para Vendors, Business Areas e Applications, migrando-os de `localStorage` para PostgreSQL via o backend Spring Boot. Preservar todas as regras de dominio ja existentes: referencias entre Application e Vendor/Department/Business Area, calculo derivado de TIME Classification a partir de Business Fit e Tech Fit, e bloqueio de exclusao de master data referenciada.

## Criterios De Aceite

- [ ] Vendors, Business Areas e Applications persistidos em PostgreSQL via JPA, com os relacionamentos existentes preservados.
- [ ] Endpoints REST (listar, criar, editar, excluir) para as tres entidades, seguindo o mesmo padrao de autenticacao da fatia 023.
- [ ] Frontend passa a ler e escrever essas entidades via API em vez de `localStorage`.
- [ ] TIME Classification continua sendo recalculada no backend a partir de Business Fit e Tech Fit ao editar uma Application.
- [ ] Bloqueio de exclusao de Vendor/Business Area referenciado por Application validado no backend.
- [ ] Testes automatizados cobrindo as APIs das tres entidades.

## Bloqueado Por

- [023 - Migrar Departments Para API Backend Persistida](./023-migrar-departments-api-backend.md)

## Verificacao

Repetir o roteiro de validacao do MVP (README) para Vendors, Business Areas e Applications usando o backend real: criar, editar, excluir, confirmar persistencia entre reinicios do backend, confirmar recalculo de TIME e bloqueio de exclusao de referenciados; rodar a suite de testes.
