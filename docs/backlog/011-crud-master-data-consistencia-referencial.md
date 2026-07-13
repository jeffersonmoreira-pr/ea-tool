# 011 - CRUD De Master Data Com Consistencia Referencial

## Tipo

AFK

## Pai

[001 - Application Portfolio MVP PRD](./001-application-portfolio-mvp-prd.md)

## O Que Construir

Permita criar, editar, listar e excluir Vendors, Departments e Business Areas, mantendo nomes unicos por cadastro e impedindo a exclusao de master data referenciado por Applications.

## Criterios De Aceite

- [ ] Vendors podem ser criados, editados, listados e excluidos quando nao referenciados.
- [ ] Vendors possuem nome unico e indicador obrigatorio de Internal Vendor.
- [ ] Departments podem ser criados, editados, listados e excluidos quando nao referenciados.
- [ ] Business Areas podem ser criadas, editadas, listadas e excluidas quando nao referenciadas.
- [ ] O app bloqueia nomes duplicados dentro de cada cadastro.
- [ ] O app bloqueia exclusao de Vendor, Department ou Business Area em uso por uma Application.

## Bloqueado Por

[010 - Bootstrap Do MVP Web Com Persistencia Local](./010-bootstrap-mvp-web-persistencia-local.md)

## Verificacao

Criar registros em cada master data, tentar criar duplicados, associar registros a uma Application seed e confirmar que a exclusao dos registros associados e bloqueada.
