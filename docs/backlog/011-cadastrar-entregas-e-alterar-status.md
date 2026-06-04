# 011 - Cadastrar Entregas E Alterar Status

## Metadata

Label: `needs-triage`

## Tipo

AFK

## Pai

[001 - PRD Painel De Obrigacoes Contabeis](001-prd-painel-obrigacoes-contabeis.md)

## O Que Construir

Permitir **Cadastro Manual** de uma **Entrega De Obrigacao** por vez, salvar os dados no armazenamento local do navegador e alterar o **Status Da Entrega** entre pendente, concluida e dispensada.

A fatia deve manter o **Painel Principal** atualizado imediatamente apos cadastro ou mudanca de status, preservando os dados apos recarregar a pagina.

## Criterios De Aceite

- [ ] O formulario permite informar **Cliente**, CNPJ ou CPF opcional, **Obrigacao Acessoria**, **Periodo De Referencia**, **Prazo** e **Responsavel**.
- [ ] Uma entrega nova recebe **Status Da Entrega** pendente por padrao.
- [ ] A entrega cadastrada aparece no **Painel Principal** sem recarregar a pagina.
- [ ] A entrega permanece disponivel apos recarregar o navegador.
- [ ] Campos obrigatorios impedem cadastro incompleto com mensagens claras e discretas.
- [ ] Uma entrega pendente pode ser marcada como concluida.
- [ ] Uma entrega pendente pode ser marcada como dispensada.
- [ ] Uma entrega concluida ou dispensada pode ser reaberta como pendente.
- [ ] Entregas concluidas e dispensadas deixam de aparecer no foco de risco do **Painel Principal**.
- [ ] A alteracao de status permanece apos recarregar a pagina.

## Bloqueado Por

- [010 - Criar Base Navegavel E Regras Do Painel Principal](010-criar-base-navegavel-e-regras-do-painel-principal.md)

## Verificacao

Cadastrar uma entrega pendente, recarregar a pagina e confirmar que ela continua listada no **Painel Principal** com os dados informados e a classificacao correta de prazo. Alterar o status para concluida, dispensada e pendente novamente, conferindo em cada passo que o painel e o armazenamento local refletem a mudanca.

