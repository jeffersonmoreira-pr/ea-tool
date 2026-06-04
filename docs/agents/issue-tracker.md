# Issue Tracker

Este repositorio usa duas superficies de rastreamento de trabalho porque e um repositorio de treinamento:

- GitHub Issues para itens publicos e compartilhaveis.
- `docs/backlog/` para exercicios locais do workshop, exemplos e prompts de pratica em etapas.

Use GitHub Issues quando o trabalho deve ficar visivel no repositorio publicado. Use `docs/backlog/` quando o trabalho fizer parte de um exercicio de aula ou de uma sequencia local de aprendizado.

## Repositorio GitHub

`paulocorcino-recv/workshop_v1`

## Convencoes Para GitHub Issues

Execute comandos `gh` a partir da raiz do repositorio para que o repositorio seja inferido por `git remote -v`.

- Criar uma issue: `gh issue create --title "..." --body "..."`
- Ler uma issue: `gh issue view <number> --comments`
- Listar issues: `gh issue list --state open --json number,title,body,labels,comments`
- Comentar em uma issue: `gh issue comment <number> --body "..."`
- Aplicar uma label: `gh issue edit <number> --add-label "..."`
- Remover uma label: `gh issue edit <number> --remove-label "..."`
- Fechar uma issue: `gh issue close <number> --comment "..."`

## Convencoes Para Backlog

Use `docs/backlog/` para prompts de workshop que devem ser versionados com o material de treinamento.

Itens de backlog devem ser pequenos o suficiente para uma sessao focada com agente. Prefira este formato:

```markdown
# 001 - Objetivo Curto De Aprendizado

## Objetivo

O que a pessoa participante e o agente devem realizar.

## Contexto Inicial

Quais arquivos, conceitos ou restricoes importam.

## Pronto Quando

- Resultado observavel 1
- Resultado observavel 2
- Passo de verificacao
```

## Quando Uma Skill Diz "Publish To The Issue Tracker"

Crie uma GitHub Issue, exceto quando o usuario disser explicitamente que isto e um exercicio local do workshop. Para exercicios de workshop, crie ou atualize um arquivo em `docs/backlog/`.

## Quando Uma Skill Diz "Fetch The Relevant Ticket"

- Para GitHub: execute `gh issue view <number> --comments`.
- Para backlog local: leia o arquivo referenciado em `docs/backlog/`.
