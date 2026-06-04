# ADR-0001 - HTML, CSS, JavaScript E Design Airbnb

## Status

Aceita

## Contexto

O projeto e um exercicio de workshop para construir um painel simples de acompanhamento de entregas de obrigacoes contabeis. A primeira versao deve ser pequena, inspecionavel e facil de revisar, sem framework ou etapa de build obrigatoria.

Tambem existe um guia visual em `docs/adr/DESIGN-airbnb.md`, com tokens e principios inspirados no design da Airbnb: canvas branco, tipografia leve, muito espaco, cantos arredondados, uso contido da cor primaria `#ff385c` e componentes simples.

## Decisao

Usaremos HTML, CSS e JavaScript puros para a primeira versao do produto.

O design deve seguir `docs/adr/DESIGN-airbnb.md` como referencia visual, adaptando os principios ao dominio de um painel operacional para escritorio contabil. Em especial:

- usar canvas branco, texto em `#222222` e divisorias leves;
- usar `#ff385c` apenas para acoes primarias, destaques importantes e estados de risco quando fizer sentido;
- preferir componentes arredondados, limpos e com pouca elevacao;
- manter a interface densa o suficiente para leitura operacional, sem transformar o painel em landing page;
- usar fonte do sistema ou Inter como substituta pratica quando Airbnb Cereal VF nao estiver disponivel.

## Consequencias

- Fica mais facil abrir, revisar e alterar a aplicacao durante o workshop.
- Fica mais facil manter as mudancas pequenas e sem dependencia de tooling.
- Fica mais dificil escalar para uma aplicacao grande com roteamento, estado complexo ou sistema de componentes robusto.
- Agentes futuros devem evitar introduzir frameworks, bundlers ou bibliotecas de UI sem uma nova decisao explicita.
- Agentes futuros devem consultar `docs/adr/DESIGN-airbnb.md` antes de criar ou alterar telas.
