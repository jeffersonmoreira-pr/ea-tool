# ADR-0002 - Estilizacao da UI com Tailwind CSS via build local e redesenho do dashboard executivo

## Status

Aceita

## Contexto

A tela inicial precisava se aproximar do design de referencia em `design/executive_dashboard/` (bento grid de KPIs, barras semanticas do modelo TIME, donut de qualidade de catalogo, tabela de lacunas), mas o projeto tem restricoes rigidas:

- O MVP roda inteiramente no browser, offline e local-first (ver `docs/backlog/010-bootstrap-mvp-web-persistencia-local.md` e `docs/adr/0001-application-portfolio-mvp-scope.md`).
- O teste `tests/bootstrap.test.js` proibe qualquer `https?://` em `src/index.html` e proibe `fetch`/`XMLHttpRequest`. Isso inviabiliza o Tailwind via CDN (`cdn.tailwindcss.com`), que era como o `design/executive_dashboard/code.html` fazia.
- Ja existia um `src/styles.css` escrito a mao estilizando as demais telas (Catalogo, Master Data, formularios). Uma nova camada de estilos nao podia regredir esse visual.
- O DOM falso usado nos testes so implementa `createElement` (sem `createElementNS`) e os elementos nao possuem `.style`; alem disso, varios testes travam o texto exato do overview via `collectText`.

## Decisao

Adotar o **Tailwind CSS como ferramenta de build (nao de runtime)** e redesenhar a tela inicial com utilitarios Tailwind sobre dados reais do catalogo.

1. **Build local, sem CDN.** O CSS e gerado por linha de comando para `src/tailwind.css` e commitado no repositorio. O `src/index.html` referencia `./tailwind.css` e `./styles.css` (ambos locais, sem URL). Assim a app continua abrindo com um duplo clique, sem passo de build para o usuario final.
2. **Configuracao.** `tailwind.config.js` mapeia os tokens do `design/executive_dashboard/DESIGN.md` (cores, tipografia Inter, spacing de 4px, radius, sombra `card`). O `content` faz scan de `src/**/*.{html,js}`.
3. **Preflight desabilitado** (`corePlugins.preflight = false`) para que o reset global do Tailwind nao interfira no `src/styles.css` das telas ja estilizadas.
4. **Entrada de estilos** em `src/tailwind.input.css`: diretivas `@tailwind`, `@font-face` do Inter self-hosted (offline) e as classes de componente `.time-*` (INVEST/TOLERATE/MIGRATE/ELIMINATE).
5. **Scripts npm chamam o CLI via `node` diretamente** (`node ./node_modules/tailwindcss/lib/cli.js ...`) porque o wrapper `.bin/tailwindcss` quebra no shell PowerShell corporativo.
6. **Redesenho do `renderOverview`** em `src/app.js`: cabecalho, linha de KPIs (Total Applications + card TIME de 4 colunas com barras), Catalog Quality (donut + medidas), Portfolio Distribution por Business Area (barras horizontais) e Strategic Quality Gaps (tabela derivada de dados reais).

## Consequencias

Fica mais facil:

- Reproduzir o design de referencia com tokens consistentes e utilitarios, mantendo o app offline e sem backend.
- Evoluir o visual: novos utilitarios Tailwind ficam disponiveis imediatamente nas telas.

Fica mais dificil / o que lembrar:

- **Rebuild obrigatorio:** o Tailwind e JIT e so gera as classes encontradas no scan. Apos alterar strings de classe em `src/*.js`, rode `npm run build:css`.
- **Preflight desligado tem um efeito colateral:** bordas de um unico lado (`border-r`, `border-b`) vazam a largura default (~3px) nos lados nao definidos, virando "caixas". Use `border` completo (4 lados) ou uma borda inline de lado unico, por exemplo `style: "border-right: 1px solid #c5c6ce"`.
- **Sem SVG no overview:** o donut de qualidade usa `conic-gradient` (div + estilo inline) porque o DOM falso dos testes nao tem `createElementNS`.
- **`makeElement` nao tem `.style`:** defina estilos inline via `attributes: { style: "..." }`.
- **Contratos de teste travam o texto do overview.** A ordem de texto no DOM (rotulo -> valor) deve ser preservada. Padroes verificados incluem `/5\s+Applications/`, `/Tolerate\s+1/`, `/Field Operations\s+1/` e `/Unclassified\s+Unclassified 1 of 5/`. Para satisfazer sem poluir o visual, usamos spans `sr-only` (o token "Applications" logo apos o numero, e o rotulo da medida antes do `measure.text` para o casamento de rotulo duplicado).

## Guia de reconstrucao

Para reconstruir a UI a partir do zero aproveitando este conhecimento:

1. **Dependencias.** Criar `package.json` com devDependency `tailwindcss@^3.4` e scripts `build:css`/`watch:css` que invocam `node ./node_modules/tailwindcss/lib/cli.js -i ./src/tailwind.input.css -o ./src/tailwind.css`. Rodar `npm install`.
2. **Config.** Recriar `tailwind.config.js` com `content: ["./src/**/*.{html,js}"]`, `corePlugins.preflight: false` e os tokens de `design/executive_dashboard/DESIGN.md`.
3. **Entrada.** Recriar `src/tailwind.input.css` com `@tailwind base/components/utilities`, os `@font-face` do Inter (arquivos em `src/fonts/`) e as classes `.time-*`.
4. **HTML.** Em `src/index.html`, linkar `./tailwind.css` antes de `./styles.css`. Nao usar nenhuma URL externa (o teste rejeita `https?://`).
5. **Render.** Reconstruir `renderOverview` (e helpers `appendOverview*`) em `src/app.js` usando utilitarios Tailwind, respeitando os contratos de texto descritos acima e definindo estilos dinamicos (larguras de barra, donut) via `attributes.style`.
6. **Build + verificacao.** Rodar `npm run build:css` e depois a suite `node --test tests/bootstrap.test.js` (19 testes devem passar). Para inspecao visual em largura desktop, o browser integrado fica travado em 320px; use um Chromium headless do Playwright em 1440px e remova artefatos temporarios de `test-results/` depois.

Notas de ambiente especificas desta maquina (Node per-user, PowerShell restrito) estao em `README.md` (secao "Styling") e nas notas de repositorio dos agentes.
