# Docs De Dominio

Este repositorio e um ambiente de treinamento com contexto unico para desenvolvimento agentico. A documentacao de dominio existe para ensinar agentes e participantes a usar o mesmo vocabulario do projeto.

## Layout

Use este layout:

```text
/
|-- AGENTS.md
|-- CONTEXT.md
|-- docs/
|   |-- agents/
|   |-- adr/
|   `-- backlog/
`-- src/
```

O repositorio pode nao ter todos os arquivos o tempo todo. Docs de dominio ausentes sao aceitaveis nos primeiros exercicios.

## Antes De Explorar

Antes de fazer mudancas de design ou arquitetura, leia:

- `AGENTS.md`
- `docs/agents/*.md`
- `CONTEXT.md`, se existir
- ADRs relevantes em `docs/adr/`, se existirem
- o item de backlog relevante em `docs/backlog/`, se a tarefa veio do backlog local

Se `CONTEXT.md` ou ADRs nao existirem, prossiga silenciosamente e evite fingir que o projeto tem decisoes que ainda nao foram registradas.

## Documento De Contexto

Use `CONTEXT.md` para vocabulario compartilhado:

- objetivo do produto ou do workshop
- termos importantes de dominio
- termos a evitar
- visao geral da arquitetura atual
- restricoes que participantes devem entender

Agentes devem reutilizar os nomes definidos em `CONTEXT.md` ao escrever titulos de issues, testes, propostas de refatoracao e explicacoes.

## ADRs

Use `docs/adr/` para decisoes que devem sobreviver a um unico exercicio.

Crie um ADR quando uma decisao mudar como agentes futuros devem trabalhar, por exemplo:

- escolher um framework ou biblioteca
- mudar a estrategia de testes
- definir uma fronteira persistente de modulo
- decidir como itens de backlog viram GitHub Issues

Nao crie um ADR para detalhes pequenos de implementacao que so importam dentro de um exercicio.

## Regra Do Workshop

Na duvida, mantenha a trilha de aprendizado visivel: registre a suposicao, a decisao ou o passo de verificacao no menor lugar apropriado.
