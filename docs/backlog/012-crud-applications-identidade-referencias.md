# 012 - CRUD Basico De Applications Com Identidade E Referencias

## Tipo

AFK

## Pai

[001 - Application Portfolio MVP PRD](./001-application-portfolio-mvp-prd.md)

## O Que Construir

Permita criar, editar, listar e excluir Applications com identidade clara, owners e referencias obrigatorias a Vendor, Department e Business Area.

## Criterios De Aceite

- [ ] Application exige nome unico e descricao curta.
- [ ] Application aceita multiplos Application Aliases.
- [ ] Application URL e Diagnostic URL sao opcionais.
- [ ] Business Owner Name e Tech Owner Name sao obrigatorios.
- [ ] Business Owner Email e Tech Owner Email sao opcionais.
- [ ] Toda Application referencia exatamente um Vendor, um Department e uma Business Area.
- [ ] O app permite editar e excluir Applications sem quebrar as referencias dos master data.

## Bloqueado Por

- [010 - Bootstrap Do MVP Web Com Persistencia Local](./010-bootstrap-mvp-web-persistencia-local.md)
- [011 - CRUD De Master Data Com Consistencia Referencial](./011-crud-master-data-consistencia-referencial.md)

## Verificacao

Criar uma Application completa usando master data existentes, tentar salvar outra Application com o mesmo nome, editar aliases e owners, recarregar a pagina e confirmar que os dados persistem.
