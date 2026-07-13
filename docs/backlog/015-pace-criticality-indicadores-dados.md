# 015 - PACE Criticality E Indicadores De Dados

## Tipo

AFK

## Pai

[001 - Application Portfolio MVP PRD](./001-application-portfolio-mvp-prd.md)

## O Que Construir

Adicione PACE Classification, Criticality e os indicadores de tratamento de dados pessoais e dados sensiveis do negocio nas Applications.

## Criterios De Aceite

- [ ] PACE Classification aceita System of Record, System of Differentiation, System of Innovation e Unclassified.
- [ ] PACE Classification e manual e nao derivada automaticamente.
- [ ] Criticality aceita low, medium e high.
- [ ] Personal Data Handling aceita Yes, No e Unknown.
- [ ] Sensitive Business Data Handling aceita Yes, No e Unknown.
- [ ] Unknown e preservado como valor explicito para indicar trabalho de catalogo pendente.

## Bloqueado Por

[012 - CRUD Basico De Applications Com Identidade E Referencias](./012-crud-applications-identidade-referencias.md)

## Verificacao

Editar Applications com diferentes PACE, Criticality e valores de dados, salvar e confirmar que os valores aparecem corretamente na listagem/detalhe apos recarregar.
