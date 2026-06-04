# Labels De Triagem

O fluxo agentico usa cinco papeis canonicos de triagem. Este arquivo mapeia esses papeis para os metadados usados nos itens locais de backlog.

| Papel canonico | Metadado local | Significado |
| --- | --- | --- |
| `needs-triage` | `needs-triage` | Uma pessoa mantenedora ou instrutora precisa avaliar a issue. |
| `needs-info` | `needs-info` | A issue esta aguardando mais informacoes de quem reportou ou da pessoa participante. |
| `ready-for-agent` | `ready-for-agent` | A issue esta especifica o suficiente para um agente de IA trabalhar com pouco contexto extra. |
| `ready-for-human` | `ready-for-human` | A issue precisa de julgamento humano, ensino ou implementacao humana. |
| `wontfix` | `wontfix` | A issue nao sera executada. |

## Uso No Workshop

No treinamento, labels tambem sao uma ferramenta didatica:

- Use `needs-triage` para praticar a transformacao de pedidos vagos em tarefas claras.
- Use `needs-info` quando o agente deve pedir contexto faltante.
- Use `ready-for-agent` apenas quando a issue tiver objetivo, restricoes e criterios de pronto claros.
- Use `ready-for-human` quando a aula exigir julgamento humano de design.
- Use `wontfix` para praticar o fechamento respeitoso de trabalho com uma justificativa.

## Regra Para Agentes

Nao invente novas labels de triagem sem pedido explicito. Se um item local precisar de triagem, use um dos papeis canonicos acima.
