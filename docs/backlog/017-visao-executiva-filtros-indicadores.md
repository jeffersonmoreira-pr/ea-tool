# 017 - Visao Executiva Com Filtros E Indicadores

## Tipo

AFK

## Pai

[001 - Application Portfolio MVP PRD](./001-application-portfolio-mvp-prd.md)

## O Que Construir

Crie a visao executiva do Application Portfolio com indicadores simples e filtros para inspecionar rapidamente o landscape de Applications.

## Criterios De Aceite

- [ ] A visao mostra total de Applications.
- [ ] A visao mostra contagens por TIME, PACE, Business Area, Lifecycle Status e Criticality.
- [ ] A visao mostra contagens de Personal Data Handling Yes e Unknown.
- [ ] A visao mostra contagens de Sensitive Business Data Handling Yes e Unknown.
- [ ] A visao mostra Catalog Quality Measure para Verified, Draft, Needs Review, Unclassified e dados Unknown em relacao ao total.
- [ ] A lista de Applications pode ser filtrada por Department, Vendor, Business Area, Lifecycle Status, TIME, PACE e Criticality.
- [ ] Os indicadores refletem os dados atuais apos criacao, edicao ou exclusao de Applications.

## Bloqueado Por

- [011 - CRUD De Master Data Com Consistencia Referencial](./011-crud-master-data-consistencia-referencial.md)
- [012 - CRUD Basico De Applications Com Identidade E Referencias](./012-crud-applications-identidade-referencias.md)
- [013 - Lifecycle E Regras Condicionais Da Application](./013-lifecycle-regras-condicionais-application.md)
- [014 - Fit Assessments E TIME Derivado](./014-fit-assessments-time-derivado.md)
- [015 - PACE Criticality E Indicadores De Dados](./015-pace-criticality-indicadores-dados.md)
- [016 - Qualidade Do Catalogo E Diagnostic URL](./016-qualidade-catalogo-diagnostic-url.md)

## Verificacao

Usar seed data e edicoes manuais para mudar TIME, PACE, Business Area, Lifecycle Status, Criticality e qualidade do catalogo, confirmando que filtros e indicadores acompanham as alteracoes.
