# 012 - Consultar Filtrar Editar E Excluir Entregas

## Metadata

Label: `needs-triage`

## Tipo

AFK

## Pai

[001 - PRD Painel De Obrigacoes Contabeis](001-prd-painel-obrigacoes-contabeis.md)

## O Que Construir

Adicionar o **Historico De Entregas**, filtros por **Cliente**, **Responsavel** e **Status Da Entrega**, alem de edicao e exclusao de **Entregas De Obrigacao** cadastradas.

A fatia deve completar o fluxo operacional basico: consultar entregas fora do foco principal, encontrar registros especificos, corrigir dados e remover cadastros feitos por engano sem deixar o painel, o historico ou a persistencia local em estado incoerente.

## Criterios De Aceite

- [ ] Existe uma forma clara de alternar entre **Painel Principal** e **Historico De Entregas**.
- [ ] O historico mostra entregas concluidas e dispensadas com seus dados principais.
- [ ] O historico preserva entregas antigas sem exibi-las como risco operacional quando elas nao estao pendentes.
- [ ] O usuario consegue reabrir uma entrega do historico como pendente quando necessario.
- [ ] O usuario consegue filtrar entregas por texto de **Cliente**.
- [ ] O usuario consegue filtrar entregas por texto de **Responsavel**.
- [ ] O usuario consegue filtrar entregas por **Status Da Entrega**.
- [ ] Os filtros funcionam no **Painel Principal** sem quebrar a ordenacao por prioridade.
- [ ] Os filtros funcionam no **Historico De Entregas** para encontrar entregas concluidas ou dispensadas.
- [ ] Existe uma forma clara de limpar os filtros aplicados.
- [ ] O usuario consegue editar **Cliente**, CNPJ ou CPF, **Obrigacao Acessoria**, **Periodo De Referencia**, **Prazo** e **Responsavel**.
- [ ] Ao alterar o **Prazo**, a entrega e reclassificada conforme as regras de **Entrega Vencida** e **Entrega Proxima Do Prazo**.
- [ ] O usuario consegue excluir uma entrega cadastrada por engano.
- [ ] A exclusao exige confirmacao antes de remover a entrega.
- [ ] Edicao e exclusao permanecem consistentes apos recarregar a pagina.
- [ ] Filtros ativos e visoes atuais nao entram em estado incoerente apos editar ou excluir.
- [ ] A tela continua legivel em desktop e mobile.

## Bloqueado Por

- [011 - Cadastrar Entregas E Alterar Status](011-cadastrar-entregas-e-alterar-status.md)

## Verificacao

Marcar entregas como concluida e dispensada, abrir o **Historico De Entregas**, confirmar que elas aparecem ali e reabrir uma delas para verificar que volta ao **Painel Principal**. Aplicar filtros por cliente, responsavel e status, editar uma entrega para mudar o **Prazo**, excluir uma entrega com confirmacao e recarregar a pagina para validar a persistencia.

