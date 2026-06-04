# 001 - PRD Painel De Obrigacoes Contabeis

## Metadata

Label: `needs-triage`

## Problem Statement

Escritorios contabeis precisam acompanhar muitas **Entregas De Obrigacao** por **Cliente**, **Periodo De Referencia**, **Prazo**, **Responsavel** e **Status Da Entrega**. Quando esse acompanhamento fica espalhado em planilhas, memoria individual ou conversas, a equipe perde visibilidade sobre o que esta vencido, o que esta proximo do prazo e quem esta responsavel por cada entrega.

A primeira versao precisa ser simples o bastante para um workshop: uma aplicacao pequena, inspecionavel, sem backend obrigatorio e sem etapa de build, que deixe claro o modelo de dominio e permita praticar desenvolvimento agentico em fatias revisaveis.

## Solution

Construir um **Painel Principal** em HTML, CSS e JavaScript puros para cadastrar manualmente **Entregas De Obrigacao**, visualizar prioridades de prazo e alterar o **Status Da Entrega**. A experiencia deve seguir o vocabulario de `CONTEXT.md` e a decisao de design registrada no ADR aceito: canvas branco, tipografia leve, divisorias sutis, cantos arredondados e uso contido de `#ff385c`.

O produto deve priorizar o trabalho operacional diario: mostrar primeiro **Entregas Vencidas**, depois **Entregas Proximas Do Prazo**, depois demais entregas pendentes. **Historico De Entregas** deve permitir encontrar entregas concluidas, dispensadas ou antigas sem competir visualmente com o painel de risco.

## User Stories

1. As an escritorio contabil staff member, I want to cadastrar uma **Entrega De Obrigacao** manualmente, so that ela entre no acompanhamento operacional.
2. As an escritorio contabil staff member, I want to informar o **Cliente** por nome, so that a entrega fique associada a quem recebe o servico contabil.
3. As an escritorio contabil staff member, I want to informar CNPJ ou CPF opcionalmente, so that clientes com nomes parecidos possam ser diferenciados quando necessario.
4. As an escritorio contabil staff member, I want to informar a **Obrigacao Acessoria**, so that a equipe saiba qual exigencia legal esta sendo acompanhada.
5. As an escritorio contabil staff member, I want to informar o **Periodo De Referencia** como texto livre, so that periodos mensais, trimestrais, anuais ou especificos sejam aceitos na primeira versao.
6. As an escritorio contabil staff member, I want to informar o **Prazo** manualmente, so that a primeira versao nao dependa de regras legais automatizadas.
7. As an escritorio contabil staff member, I want to informar um **Responsavel** por nome livre, so that a equipe veja quem deve acompanhar a entrega.
8. As an escritorio contabil staff member, I want to criar uma entrega com **Status Da Entrega** pendente por padrao, so that novos cadastros entrem imediatamente no fluxo de trabalho.
9. As an escritorio contabil staff member, I want to ver **Entregas Vencidas** destacadas, so that riscos imediatos recebam atencao primeiro.
10. As an escritorio contabil staff member, I want to ver **Entregas Proximas Do Prazo** destacadas, so that a equipe possa agir antes de atrasar.
11. As an escritorio contabil staff member, I want to ver demais entregas pendentes em ordem de **Prazo**, so that o trabalho restante fique organizado.
12. As an escritorio contabil staff member, I want to marcar uma entrega como concluida, so that ela saia do foco do **Painel Principal**.
13. As an escritorio contabil staff member, I want to marcar uma entrega como dispensada, so that entregas que nao precisam ser realizadas fiquem registradas sem parecerem pendentes.
14. As an escritorio contabil staff member, I want to reabrir uma entrega concluida ou dispensada como pendente, so that correcoes operacionais sejam possiveis.
15. As an escritorio contabil staff member, I want to filtrar por **Cliente**, so that eu possa revisar rapidamente a situacao de um cliente especifico.
16. As an escritorio contabil staff member, I want to filtrar por **Responsavel**, so that cada pessoa veja sua carteira de acompanhamento.
17. As an escritorio contabil staff member, I want to filtrar por **Status Da Entrega**, so that eu possa separar pendentes, concluidas e dispensadas.
18. As an escritorio contabil staff member, I want to acessar um **Historico De Entregas**, so that entregas concluidas, dispensadas ou antigas continuem consultaveis.
19. As an escritorio contabil staff member, I want to ver contadores resumidos de vencidas, proximas do prazo e pendentes, so that a situacao geral seja entendida em poucos segundos.
20. As an escritorio contabil staff member, I want to editar uma entrega cadastrada, so that erros de prazo, responsavel ou identificacao possam ser corrigidos.
21. As an escritorio contabil staff member, I want to excluir uma entrega cadastrada por engano, so that dados de teste ou duplicados nao poluam o painel.
22. As an workshop participant, I want to abrir a aplicacao diretamente no navegador, so that eu possa revisar o resultado sem configurar tooling.
23. As an workshop participant, I want to entender as regras de prioridade lendo a interface e os testes, so that o exercicio ensine dominio e implementacao.
24. As an reviewer, I want small and inspectable changes, so that cada fatia do trabalho possa ser revisada em uma sessao focada.
25. As an future agent, I want clear module boundaries around dominio, persistencia local and UI rendering, so that later changes can be made without rewriting the whole app.

## Implementation Decisions

- Implementar a primeira versao com HTML, CSS e JavaScript puros, sem framework, bundler ou biblioteca de UI.
- Usar o vocabulario do dominio definido em `CONTEXT.md`, especialmente **Obrigacao Acessoria**, **Entrega De Obrigacao**, **Prazo**, **Periodo De Referencia**, **Responsavel**, **Status Da Entrega**, **Painel Principal** e **Historico De Entregas**.
- Respeitar o ADR aceito de HTML, CSS, JavaScript e design inspirado na Airbnb.
- Tratar **Entrega Vencida** e **Entrega Proxima Do Prazo** como condicoes calculadas, nao como valores manuais de **Status Da Entrega**.
- Usar a janela fixa de 7 dias corridos para calcular **Entrega Proxima Do Prazo**.
- Manter **Prazo** informado manualmente e nao calculado por regras legais.
- Manter **Periodo De Referencia** e **Responsavel** como texto livre na primeira versao.
- Criar uma fronteira de dominio testavel para classificar entregas por prioridade, calcular condicoes derivadas e ordenar o **Painel Principal**.
- Criar uma fronteira de armazenamento local para salvar, listar, atualizar e remover **Entregas De Obrigacao** sem acoplar a interface ao mecanismo de persistencia.
- Criar uma fronteira de apresentacao responsavel por renderizar o **Painel Principal**, filtros, formulario de **Cadastro Manual** e **Historico De Entregas**.
- Usar armazenamento local do navegador como persistencia suficiente para o workshop, salvo decisao futura em contrario.
- Adaptar o design Airbnb para um painel operacional, mantendo densidade de leitura e evitando aparencia de landing page.
- Usar `#ff385c` com contencao para acoes primarias e estados de risco, sem transformar toda a UI em uma paleta de vermelho.
- Assumir que o guia visual correto esta em `docs/adr/DESIGN-airbnb.md`, pois esse e o arquivo existente no repositorio.

## Testing Decisions

- Testes devem cobrir comportamento externo e regras de dominio observaveis, nao detalhes internos de implementacao.
- Priorizar testes para o modulo de dominio que classifica **Entregas Vencidas**, **Entregas Proximas Do Prazo** e demais pendentes.
- Testar a ordenacao do **Painel Principal**: vencidas primeiro, proximas do prazo depois, demais pendentes por ultimo, sempre pelo **Prazo** mais antigo dentro do grupo.
- Testar que entregas concluidas e dispensadas nao aparecem como risco operacional no **Painel Principal**.
- Testar a fronteira de armazenamento local quando houver comportamento alem de chamadas triviais ao navegador.
- Testar fluxos principais de UI de forma leve quando a estrutura permitir: cadastro, alteracao de status, filtro e consulta ao **Historico De Entregas**.
- Como o repositorio ainda nao tem suite de testes configurada, escolher uma estrategia simples e compativel com HTML, CSS e JavaScript puros antes de adicionar testes extensos.
- Um bom teste neste projeto deve ajudar participantes do workshop a entender a regra de negocio sem precisar ler toda a implementacao.

## Out of Scope

- Importacao de planilhas.
- Calculo automatico de prazos por regras legais.
- Alertas, notificacoes, emails ou integracoes externas.
- Login, permissoes, usuarios, equipes ou departamentos.
- Cadastro completo de clientes.
- Backend, banco de dados remoto ou sincronizacao multiusuario.
- Relatorios fiscais completos.
- Frameworks frontend, bibliotecas de componentes, bundlers ou etapa obrigatoria de build.
- Transformar **Entrega Vencida** em **Status Da Entrega** manual.

## Further Notes

Este PRD entra no backlog como `needs-triage` para revisao humana ou de instrucao. Apos triagem, ele pode ser quebrado em fatias tracer-bullet: base visual, modelo de dominio, cadastro manual, painel priorizado, alteracao de status, historico e verificacao.

O repositorio mostra arquivos de contexto e ADR ainda nao commitados. Este PRD foi escrito para trabalhar com esse estado atual sem modificar esses arquivos.
