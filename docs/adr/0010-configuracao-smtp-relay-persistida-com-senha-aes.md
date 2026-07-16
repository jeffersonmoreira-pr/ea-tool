# ADR-0010 - Configuração de SMTP relay persistida no banco com senha AES em repouso

## Status

Aceita. Concretizada pela fatia #25: o envio real do convite de Local Login passou
a ser dirigido por esta configuração de banco (`RelayInvitationMailer` resolve a
config em runtime, descriptografa a senha e envia via SMTP; sem config, faz
fallback para log). A partir daí a config de banco tem precedência sobre
`spring.mail.*`.

## Contexto

A fatia dedicada de entrega de e-mail (issue #20, prevista pela ADR-0009) exige uma
tela Admin em runtime para configurar o SMTP relay usado nos convites de Local
Login. Diferente do desenho anterior (ADR-0008/ADR-0009), em que a ativação de SMTP
dependia de `spring.mail.*` no ambiente, o usuário decidiu que a configuração deve
ser feita pela aplicação, por um Admin, e persistida — sem exigir redeploy ou acesso
ao servidor.

Isso levanta duas questões:

1. **Onde persistir a configuração.** As opções eram manter `spring.mail.*` como
   fonte de verdade (config estática por ambiente) ou persistir no banco (config
   dinâmica em runtime).
2. **Como guardar a senha do relay.** A senha SMTP é um segredo; guardá-la em texto
   claro no banco é inaceitável, e devolvê-la ao frontend também.

## Decisão

- A configuração de SMTP relay é persistida no banco, na entidade singleton
  `SmtpRelayConfig` (tabela `smtp_relay_config`), e o **banco passa a ser a fonte de
  verdade** para o envio de convites. Quando não há configuração persistida, o envio
  cai no comportamento de dev (link no log, via `LoggingInvitationMailer`); a adoção
  dinâmica dessa config pelo mailer é tratada na fatia #25.
- A senha do relay é **criptografada em repouso com AES-GCM** (`SmtpPasswordEncryptor`),
  usando uma chave de 256 bits fornecida por variável de ambiente
  (`EMAIL_DELIVERY_ENCRYPTION_KEY`, propriedade `app.email-delivery.encryption-key`).
  A chave nunca vive no schema; um valor de dev descartável é usado apenas quando a
  variável não é definida.
- A senha **nunca** é retornada em nenhuma resposta da API. O read model
  (`SmtpRelayConfigResponse`) expõe apenas um booleano `passwordSaved`. Ao salvar,
  enviar o campo de senha em branco mantém a senha já persistida; enviar um novo valor
  a substitui.
- O endpoint de escrita (`PUT /api/email-delivery`) é Admin-only, coberto pela regra
  central `hasRole("ADMIN")` em `/api/email-delivery/**` (SecurityConfig), e valida
  host, porta (1–65535) e e-mail (from), exigindo Username somente quando a
  autenticação está habilitada.

## Consequências

- A configuração de SMTP em runtime substitui o uso de `spring.mail.*` como fonte de
  verdade para o envio de convites; a fatia #25 fará o mailer ler dinamicamente a
  config do banco em vez do condicional `spring.mail.host`.- A chave de criptografia é operacionalmente crítica: perdê-la torna a senha
  persistida irrecuperável (será necessário re-salvar a configuração), e vazá-la
  compromete o segredo. Produção **deve** definir `EMAIL_DELIVERY_ENCRYPTION_KEY` e
  mantê-la fora do controle de versão.
- O uso de AES-GCM (com IV aleatório por operação e tag de autenticação) garante
  confidencialidade e integridade do ciphertext; o formato armazenado é
  `Base64(iv || ciphertext+tag)`.
- Como a senha nunca trafega de volta ao frontend, a UX de edição adota o padrão
  "deixe em branco para manter a senha atual" (frame `Active_State` do design
  canônico).
- A entidade é singleton (resolvida por `findFirstByOrderByUpdatedAtDesc`); a tela
  edita sempre a mesma configuração, sem histórico de versões nesta fatia.
