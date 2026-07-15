# Issue Tracker

Este repositorio usa duas superficies de rastreamento de trabalho:

- **GitHub Issues do repositorio `jeffersonmoreira-pr/ea-tool`** para itens publicos e compartilhaveis de trabalho (fatias verticais geradas por `to-issues`, bugs, features).
- `docs/backlog/` para PRDs, exercicios locais do workshop, exemplos e prompts de pratica em etapas que devem ficar versionados junto com o material de treinamento.

O projeto anteriormente publicava issues publicas em `paulocorcino-recv/workshop_v1`; o trabalho foi migrado para o repositorio proprio `jeffersonmoreira-pr/ea-tool`, que agora e o destino correto.

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

Crie uma issue no GitHub Issues de `jeffersonmoreira-pr/ea-tool`, aplicando a label de triagem correta. Use `docs/backlog/` apenas para exercicios locais do workshop.

## Quando Uma Skill Diz "Fetch The Relevant Ticket"

Busque a issue referenciada no GitHub Issues de `jeffersonmoreira-pr/ea-tool`, ou leia o arquivo referenciado em `docs/backlog/` se for um exercicio local.
