# 013 - Lifecycle E Regras Condicionais Da Application

## Tipo

AFK

## Pai

[001 - Application Portfolio MVP PRD](./001-application-portfolio-mvp-prd.md)

## O Que Construir

Adicione Lifecycle Status as Applications e aplique as regras condicionais de data para planned, retiring e retired.

## Criterios De Aceite

- [ ] Application permite Lifecycle Status planned, active, retiring e retired.
- [ ] Planned Date e obrigatorio quando Lifecycle Status e planned.
- [ ] Retirement Date e obrigatorio quando Lifecycle Status e retiring.
- [ ] Retirement Date e obrigatorio quando Lifecycle Status e retired.
- [ ] Nenhuma data de lifecycle e obrigatoria quando Lifecycle Status e active.
- [ ] As validacoes condicionais aparecem no fluxo de criacao e edicao.

## Bloqueado Por

[012 - CRUD Basico De Applications Com Identidade E Referencias](./012-crud-applications-identidade-referencias.md)

## Verificacao

Tentar salvar Applications planned, retiring e retired sem as respectivas datas e confirmar bloqueio; salvar os mesmos casos com datas validas e confirmar sucesso.
