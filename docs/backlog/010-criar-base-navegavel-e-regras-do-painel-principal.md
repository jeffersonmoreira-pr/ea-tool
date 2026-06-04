# 010 - Criar Base Navegavel E Regras Do Painel Principal

## Metadata

Label: `needs-triage`

## Tipo

AFK

## Pai

[001 - PRD Painel De Obrigacoes Contabeis](001-prd-painel-obrigacoes-contabeis.md)

## O Que Construir

Criar a primeira versao navegavel do **Painel Principal** em HTML, CSS e JavaScript puros, ja com a regra de dominio que classifica e ordena **Entregas De Obrigacao** pendentes por risco de **Prazo**.

A fatia deve entregar uma tela aberta diretamente no navegador, com dados de exemplo, visual alinhado ao ADR aceito e comportamento suficiente para demonstrar **Entregas Vencidas**, **Entregas Proximas Do Prazo** e demais pendentes.

## Criterios De Aceite

- [ ] A aplicacao abre diretamente no navegador sem etapa obrigatoria de build.
- [ ] A tela inicial mostra o nome **Painel Principal** e usa o vocabulario do dominio para **Entregas De Obrigacao**, **Prazo**, **Responsavel** e **Status Da Entrega**.
- [ ] A interface segue a direcao visual do ADR: canvas branco, divisorias sutis, cantos arredondados, tipografia leve e uso contido de `#ff385c`.
- [ ] Existem dados de exemplo suficientes para revisar visualmente **Entregas Vencidas**, **Entregas Proximas Do Prazo** e demais pendentes.
- [ ] **Entrega Vencida** e calculada a partir de **Prazo** anterior a data atual e **Status Da Entrega** pendente.
- [ ] **Entrega Proxima Do Prazo** e calculada para entregas pendentes com **Prazo** entre a data atual e os proximos 7 dias corridos.
- [ ] Entregas concluidas e dispensadas nao aparecem como risco operacional no **Painel Principal**.
- [ ] A ordenacao do painel prioriza vencidas, proximas do prazo e demais pendentes, pelo **Prazo** mais antigo dentro de cada grupo.
- [ ] Existem testes automatizados ou uma verificacao equivalente documentada para os cenarios principais da regra.

## Bloqueado Por

Nenhum - pode comecar imediatamente

## Verificacao

Abrir a aplicacao no navegador e confirmar que o **Painel Principal** aparece sem servidor obrigatorio, sem erros no console e com layout legivel em desktop e mobile. Executar a verificacao definida para as regras de dominio e conferir, com uma data controlada ou exemplos previsiveis, que cada entrega recebe a classificacao e posicao esperadas.

