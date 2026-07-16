# ADR-0009 - Envio real de e-mail de convite adiado para fatia dedicada

## Status

Aceita. Atualizada pela fatia #25 (envio real implementado): o envio de convite
passou a ser dirigido pela configuração de SMTP relay persistida no banco (ver
ADR-0010). O `LoggingInvitationMailer` permanece como fallback quando não há
relay configurado no banco.

## Contexto

O Local Login (ADR-0004, ADR-0008) precisa entregar ao usuário convidado um link
de definição de senha. A ADR-0008 modelou o envio atrás da interface
`InvitationMailer`, com duas implementações:

- `SmtpInvitationMailer`, ativado apenas quando `spring.mail.host` está definido;
- `LoggingInvitationMailer`, fallback padrão que **apenas registra o link no log**,
  o que o critério de aceite da fatia #9 permite explicitamente ("ou log do envio
  em ambiente de dev").

Na verificação manual da fatia #9, o convite não gerou e-mail porque nenhum relay
SMTP está configurado no ambiente de dev — o comportamento esperado, mas que deixa
o fluxo ponta a ponta (recebimento do e-mail pelo convidado) sem cobertura real.
Configurar um relay corporativo de produção e/ou uma sandbox de e-mail para dev
(por exemplo MailHog/Mailpit) tem escopo, credenciais e verificação próprios,
maiores do que caberia na fatia #9.

## Decisão

O envio **real** de e-mail de convite não faz parte da fatia #9. Ele será tratado
numa fatia dedicada, rastreada pela issue #20 no GitHub Issues de
`jeffersonmoreira-pr/ea-tool`. Até lá, o `LoggingInvitationMailer` permanece como
comportamento padrão em dev, e a ativação de SMTP continua sendo apenas questão de
configuração (`spring.mail.*`), sem mudança de código.

## Consequências

- O contrato `InvitationMailer` e o `SmtpInvitationMailer` já existentes são o ponto
  de extensão; a fatia dedicada deve focar em configuração, entrega real e
  verificação (sandbox de e-mail em dev, relay em produção), não em reprojetar o envio.
- Enquanto a fatia dedicada não for concluída, o fluxo de convite em dev depende de
  ler o link no log da aplicação; isso deve ser comunicado a quem testa o Local Login.
- Agentes futuros não devem assumir que o e-mail é realmente enviado em dev; para
  validar entrega real é necessário configurar `spring.mail.host` (e afins) e,
  idealmente, uma sandbox de e-mail.
- A decisão mantém a fatia #9 pequena e revisável, adiando dependências de
  infraestrutura de e-mail sem bloquear o restante do Local Login.
