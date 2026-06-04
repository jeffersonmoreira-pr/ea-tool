# Backlog Do Workshop

Use este diretorio para exercicios locais do workshop, prompts de aprendizado em etapas, PRDs e issues locais geradas por skills agenticas.

Use este diretorio como fonte local de rastreamento de trabalho. O objetivo e manter todo o material inspecionavel no Git e independente de servicos externos.

## Nome Dos Arquivos

Use prefixos numericos para que o backlog fique ordenado por ordem de execucao:

- `001-objetivo-curto-de-aprendizado.md` para exercicios de workshop.
- `010-adicionar-validacao-basica.md` para issues de implementacao geradas.
- `020-revisar-saida-do-agente.md` para tarefas de revisao humana.

## Template De Exercicio

Use este formato ao escrever um prompt para uma pessoa participante e um agente:

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

## Reflexao

O que a pessoa participante deve perceber depois que o agente terminar?
```

## Template De Issue Gerada

Use este formato quando uma skill como `to-issues` publicar issues locais no backlog:

```markdown
# 010 - Fatia Curta De Implementacao

## Tipo

AFK

## Pai

Referencie o item de backlog pai, PRD, issue ou documento de origem. Omita esta secao se nao houver pai.

## O Que Construir

Uma descricao concisa desta fatia vertical. Descreva o comportamento ponta a ponta, nao uma lista de tarefas camada por camada.

## Criterios De Aceite

- [ ] Criterio 1
- [ ] Criterio 2
- [ ] Criterio 3

## Bloqueado Por

Nenhum - pode comecar imediatamente

## Verificacao

Como a pessoa participante ou o agente deve provar que a fatia funciona.
```

## Regras Para `to-issues`

- Quebre planos em fatias verticais tracer-bullet.
- Prefira issues AFK quando o trabalho puder ser concluido sem julgamento humano.
- Marque issues como HITL quando exigirem decisao humana, revisao ou ensino.
- Publique bloqueadores primeiro para que issues posteriores possam referencia-los.
- Mantenha cada issue pequena o suficiente para uma sessao focada com agente.
