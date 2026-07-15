# 019 - Redesenho Da Tela Inicial Com Dashboard Executivo

## Tipo

AFK

## Pai

[001 - Application Portfolio MVP PRD](./001-application-portfolio-mvp-prd.md)

## O Que Construir

Aproximar a tela inicial (Executive Overview) do design de referencia em `design/executive_dashboard/`, reapresentando os indicadores existentes como um dashboard executivo visual, sem introduzir backend nem dependencia de rede. A decisao tecnica que habilita esta entrega esta registrada em [ADR-0002](../adr/0002-tailwind-ui-build-e-dashboard-executivo.md).

## Criterios De Aceite

- [x] O Tailwind CSS e adotado como ferramenta de build local, com `src/tailwind.css` gerado e commitado, sem CDN nem URLs externas em `src/index.html`.
- [x] A tela inicial exibe uma linha de KPIs com Total de Applications e o card TIME (Invest, Tolerate, Migrate, Eliminate) com contagem, percentual e barras semanticas.
- [x] A tela inicial exibe o Catalog Quality Measure como donut de percentual verificado mais a lista das medidas de qualidade.
- [x] A tela inicial exibe a distribuicao do portfolio por Business Area em barras horizontais.
- [x] A tela inicial exibe a tabela Strategic Quality Gaps derivada de dados reais (PACE nao classificado, data handling desconhecido ou Needs Review).
- [x] As demais telas (Application Catalog, Master Data) permanecem sem regressao visual (preflight do Tailwind desabilitado).
- [x] A suite `node --test tests/bootstrap.test.js` continua passando (19 testes).

## Bloqueado Por

- [017 - Visao Executiva Com Filtros E Indicadores](./017-visao-executiva-filtros-indicadores.md)

## Verificacao

Rodar `npm run build:css`, abrir `src/index.html` no navegador em largura desktop e confirmar o bento grid (KPIs, TIME, donut de qualidade, distribuicao por Business Area e tabela de lacunas); reduzir a largura para confirmar o empilhamento responsivo; rodar `node --test tests/bootstrap.test.js` e confirmar 19 testes passando.

## Reflexao

Como restricoes de arquitetura (offline/local-first, testes que proibem URLs externas, `styles.css` legado) moldam a forma de adotar uma biblioteca de UI: neste caso, Tailwind entrou como etapa de build e nao de runtime, e o reset global foi desligado para preservar as telas ja estilizadas.
