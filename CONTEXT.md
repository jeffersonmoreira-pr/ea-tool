# Painel De Obrigacoes Contabeis

Este contexto descreve um produto para escritorios contabeis acompanharem entregas de obrigacoes acessorias por cliente, com visibilidade sobre prazos, responsaveis e status.

## Language

**Obrigacao Acessoria**:
Um tipo de exigencia legal que o escritorio contabil deve acompanhar para clientes.
_Avoid_: entrega, tarefa, declaracao quando usado de forma generica

**Entrega De Obrigacao**:
Uma ocorrencia concreta de uma **Obrigacao Acessoria** para um **Cliente** e periodo, com prazo, responsavel e status.
_Avoid_: obrigacao acessoria quando se referir a uma ocorrencia individual

**Prazo**:
A data limite informada manualmente para concluir uma **Entrega De Obrigacao**.
_Avoid_: vencimento quando usado sem distinguir data de status

**Periodo De Referencia**:
O periodo ao qual uma **Entrega De Obrigacao** se refere, informado como texto na primeira versao.
_Avoid_: competencia quando exigir formato unico

**Responsavel**:
A pessoa nomeada para acompanhar ou concluir uma **Entrega De Obrigacao**.
_Avoid_: usuario, equipe, departamento na primeira versao

**Status Da Entrega**:
A situacao operacional manual de uma **Entrega De Obrigacao**: pendente, concluida ou dispensada.
_Avoid_: vencida quando usado como status manual

**Entrega Vencida**:
Uma **Entrega De Obrigacao** pendente cujo **Prazo** e anterior a data atual.
_Avoid_: status vencido

**Entrega Proxima Do Prazo**:
Uma **Entrega De Obrigacao** pendente cujo **Prazo** esta entre a data atual e os proximos 7 dias corridos.
_Avoid_: vence em breve sem definir janela

**Painel Principal**:
A visao de trabalho que prioriza **Entregas De Obrigacao** pendentes, vencidas e proximas do prazo.
_Avoid_: historico, relatorio completo

**Historico De Entregas**:
A visao filtravel de **Entregas De Obrigacao** concluidas, dispensadas ou antigas.
_Avoid_: painel principal

**Cadastro Manual**:
A criacao individual de **Entregas De Obrigacao** por uma pessoa do escritorio contabil.
_Avoid_: importacao de planilha na primeira versao

**Cliente**:
Uma pessoa juridica ou pessoa fisica atendida pelo escritorio contabil, identificada por nome e opcionalmente por CNPJ ou CPF.
_Avoid_: conta, empresa quando usado sem precisao

## Relationships

- Uma **Obrigacao Acessoria** pode gerar muitas **Entregas De Obrigacao**.
- Uma **Entrega De Obrigacao** pertence a exatamente um **Cliente**.
- Uma **Entrega De Obrigacao** tem exatamente um **Prazo**.
- Uma **Entrega De Obrigacao** tem exatamente um **Periodo De Referencia**.
- Uma **Entrega De Obrigacao** tem no maximo um **Responsavel**.
- Uma **Entrega De Obrigacao** tem exatamente um **Status Da Entrega**.
- Uma **Entrega Vencida** e derivada do **Prazo** e do **Status Da Entrega**.
- Uma **Entrega Proxima Do Prazo** e derivada do **Prazo** e do **Status Da Entrega**.
- O **Painel Principal** mostra **Entregas De Obrigacao** abertas com prioridade para risco de prazo.
- O **Painel Principal** prioriza **Entregas Vencidas**, depois **Entregas Proximas Do Prazo**, depois demais pendentes, sempre pelo **Prazo** mais antigo dentro de cada grupo.
- O **Historico De Entregas** preserva **Entregas De Obrigacao** concluidas, dispensadas ou antigas.
- O **Cadastro Manual** cria uma **Entrega De Obrigacao** por vez.

## Example dialogue

> **Dev:** "Quando falamos que a DCTFWeb venceu, estamos falando da **Obrigacao Acessoria** ou da **Entrega De Obrigacao**?"
> **Domain expert:** "Da **Entrega De Obrigacao** de um **Cliente** em um periodo especifico; a DCTFWeb em si e o tipo."

## Flagged ambiguities

- "obrigacao acessoria" foi usado tanto para o tipo legal quanto para a ocorrencia concreta; resolvido: **Obrigacao Acessoria** e o tipo, **Entrega De Obrigacao** e a ocorrencia acompanhada no painel.
- Na primeira versao, o **Prazo** nao e calculado por regras legais; ele e informado manualmente na **Entrega De Obrigacao**.
- Na primeira versao, o **Periodo De Referencia** e texto livre para acomodar periodos mensais, trimestrais, anuais ou formatos especificos.
- Na primeira versao, o **Responsavel** e um nome informado livremente, nao uma conta de usuario, equipe ou departamento cadastrado.
- Na primeira versao, **Cliente** nao exige cadastro completo; nome e obrigatorio, CNPJ ou CPF e opcional.
- Na primeira versao, **Entregas De Obrigacao** entram por **Cadastro Manual**; importacao de planilhas e uma evolucao futura.
- Na primeira versao, nao ha alertas ou notificacoes automaticas; a visibilidade acontece pelo **Painel Principal**.
- "vencida" nao e um **Status Da Entrega** manual; e uma condicao calculada para **Entregas De Obrigacao** pendentes com **Prazo** passado.
- Na primeira versao, **Entrega Proxima Do Prazo** usa uma janela fixa de 7 dias corridos a partir da data atual.
