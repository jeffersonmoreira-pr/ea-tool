# Guia de Desenvolvimento Agentico

Este repositorio esta configurado para aprender e praticar desenvolvimento de software com agentes de IA. Trate este arquivo como o guia operacional para agentes e participantes.

Estas regras sao intencionalmente leves. Adapte os detalhes especificos do projeto conforme o repositorio evolui, em vez de apagar o scaffold.

## O Que Adaptar Por Projeto

- Objetivo do projeto e vocabulario de dominio: atualize `CONTEXT.md` quando ele existir.
- Rastreamento de trabalho: atualize `docs/agents/issue-tracker.md` se o repositorio deixar de usar GitHub Issues ou `docs/backlog/`.
- Labels de triagem: atualize `docs/agents/triage-labels.md` se o projeto usar labels diferentes.
- Decisoes de arquitetura: adicione ADRs em `docs/adr/` quando decisoes precisarem orientar trabalhos futuros.
- Convencoes do backlog: atualize `docs/backlog/README.md` quando os exercicios do workshop precisarem de outro formato.

## Como Agentes Devem Trabalhar Aqui

1. Leia este arquivo primeiro.
2. Leia os arquivos relevantes em `docs/agents/` antes de usar um fluxo agentico.
3. Mantenha as mudancas pequenas o suficiente para uma pessoa revisar.
4. Explique decisoes importantes em linguagem simples.
5. Prefira issues, itens de backlog e ADRs em vez de suposicoes escondidas.
6. Nao publique secrets, tokens, credenciais ou dados privados.

## Principios De Desenvolvimento

- Otimize para aprendizado e revisao, nao para velocidade.
- Deixe a intencao visivel antes de fazer mudancas amplas.
- Use fatias tracer-bullet: um incremento pequeno, util e ponta a ponta por vez.
- Mantenha commits revisaveis e nomeados pelo objetivo de aprendizado ou pela issue.
- Quando uma tarefa for ambigua, declare a suposicao e mantenha a implementacao reversivel.

## Agent Skills

### Issue Tracker

Itens publicos e compartilhaveis vivem no GitHub Issues de `paulocorcino-recv/workshop_v1`; exercicios locais do workshop podem viver em `docs/backlog/`. Veja `docs/agents/issue-tracker.md`.

### Labels De Triagem

Este repositorio usa o vocabulario padrao de labels de triagem das skills Matt Pocock para GitHub Issues. Veja `docs/agents/triage-labels.md`.

### Docs De Dominio

Este repositorio atualmente usa layout de contexto unico: use `CONTEXT.md` na raiz e `docs/adr/` quando existirem. Veja `docs/agents/domain.md`.

## Revisao Humana Esperada

Para exercicios de workshop e mudancas feitas por agentes, a pessoa revisora deve verificar:

- O agente entendeu o objetivo?
- A mudanca e pequena e inspecionavel?
- As suposicoes foram registradas?
- Testes ou passos de verificacao foram incluidos quando relevante?
- O agente preservou arquivos nao relacionados e trabalho do usuario?
