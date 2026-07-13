# 016 - Qualidade Do Catalogo E Diagnostic URL

## Tipo

AFK

## Pai

[001 - Application Portfolio MVP PRD](./001-application-portfolio-mvp-prd.md)

## O Que Construir

Adicione Information Status, Last Verification Date e Diagnostic URL para tornar visivel a qualidade da informacao de cada Application e a referencia ao diagnostico externo.

## Criterios De Aceite

- [ ] Information Status aceita Draft, Verified e Needs Review.
- [ ] Last Verification Date e obrigatorio quando Information Status e Verified.
- [ ] Last Verification Date nao e obrigatorio quando Information Status e Draft.
- [ ] Last Verification Date nao e obrigatorio quando Information Status e Needs Review.
- [ ] Diagnostic URL e opcional em qualquer Information Status.
- [ ] Applications com Draft, Needs Review, Unknown ou Unclassified continuam salvaveis para evidenciar trabalho pendente.

## Bloqueado Por

[012 - CRUD Basico De Applications Com Identidade E Referencias](./012-crud-applications-identidade-referencias.md)

## Verificacao

Tentar salvar uma Application Verified sem Last Verification Date e confirmar bloqueio; salvar com data, salvar Draft/Needs Review sem data e confirmar sucesso.
