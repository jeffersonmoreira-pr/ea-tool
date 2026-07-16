# Email Delivery (SMTP Relay) — Nota canônica de design

Registro de decisão de design da tela de configuração do SMTP relay (issue #20),
gerada no Google Stitch em 6 frames. Este arquivo define **qual frame é a fonte de
verdade** e o que foi **adotado** ou **descartado** de cada um, para que a
implementação futura não reintroduza escopo indevido.

> Contexto de produto e decisões relacionadas: ADR-0004 (Local Login), ADR-0008
> (estratégia do mailer / autorização independente do método de login), ADR-0009
> (envio real de e-mail adiado para fatia dedicada) e issue #20.

## Propósito da tela

Tela de administração (Admin-only) para configurar, em runtime e persistido no
banco, o SMTP relay usado para enviar os e-mails de convite de definição de senha
do Local Login. Substitui a configuração estática de `application.yml`/variáveis de
ambiente.

## Decisões de produto que a UI deve refletir

- **Fonte de verdade é o banco.** A config salva no banco substitui totalmente
  `spring.mail.*` do `application.yml`/env. Sem config no banco, o envio cai no
  fallback de log em dev (`LoggingInvitationMailer`).
- **Senha do SMTP:** criptografada em repouso (AES, chave via variável de ambiente)
  e **nunca retornada ao frontend**. A UI mostra `•••••••• (saved)` quando há senha
  salva, com o texto "Leave blank to keep the current password".
- **Enviar e-mail de teste:** botão que usa a config atual e reporta sucesso/erro.
- **Limpar configuração:** volta ao modo sem relay (log em dev).
- **Somente Admin** visualiza e altera esta tela.

## Frame canônico

**`Email_Delivery_Empty_State/` é o layout base (fonte de verdade).** É o frame mais
fiel ao escopo decidido: pill de status de aviso, formulário com os campos corretos,
os três botões de ação e os dois cards de apoio ("Development Mode" e "Security Note
/ AES-256"), sem escopo indevido.

Campos do formulário (todos no frame Empty_State):

- **Host** (texto, ex.: `smtp.company.com`)
- **Port** (numérico, ex.: `587`)
- **Encryption** — controle segmentado: `None` · `STARTTLS` (padrão) · `SSL/TLS`
- **Authentication** — toggle "Require credentials" (ligado por padrão)
- **Username** (desabilitado quando Authentication está desligado)
- **Password** (estado `•••••••• (saved)` quando já existe; "leave blank to keep")
- **From address** (ex.: `no-reply@ea-tool.local`)

Ações (rodapé do card): **Save configuration** (primário) · **Send test email…**
(secundário) · **Clear configuration** (ghost/destrutivo, com confirmação).

## O que adotar dos demais frames

- **`Email_Delivery_Active_State/`** — adotar **apenas**: o pill verde "SMTP relay
  active — invites are emailed" e o comportamento do campo de senha
  (`•••••••• (saved)` + "Leave blank to keep the current password").
- **`Email_Delivery_Test_Email_Popover/`** — adotar **apenas** o popover
  "Send a test message to: Recipient Email" com ações `Cancel` / `Send`.
- **`Email_Delivery_Test_Results/`** — adotar **apenas** o toast de sucesso
  ("Test email sent to <recipient>"); e o equivalente de erro (alerta com o motivo
  da falha, ex.: "Connection refused: check host and port").
- **`Email_Delivery_Validation_Errors/`** — adotar **apenas** os erros inline por
  campo: "Enter a valid hostname", "Port must be between 1 and 65535", "Enter a
  valid email address".

## O que descartar (não implementar)

Estes elementos apareceram nos frames mas **não pertencem** a esta feature (relay
de e-mails transacionais de convite) e devem ser removidos:

- Métricas/reputação de e-mail: "Relay Performance", "Deliverability/Bounce Rate",
  "Monthly Success Rate", quotas, "Health Status / Workers".
- Marketing/rastreamento: "Open Tracking", "Click Tracking", pixel invisível,
  "Tracking & Analytics".
- Autenticação de domínio: "SPF / DKIM / DMARC" ("Domain Authentication").
- Logs de entrega: "Recent Delivery Logs" (Sent/Queued/Bounced), "Download CSV",
  "View Detailed Audit".
- Painel de deploy do frame `Saving_State/`: "Deployment Status", "Syncing with
  global delivery endpoints", "5 nodes", "Deployment History", "CDN". Para o estado
  "salvando", usar apenas o botão primário com spinner/desabilitado.
- Ajuda/suporte: "Configuration Help", "Open Support Ticket".
- Campo "Display Name" (frame Validation_Errors): não decidido; o escopo prevê
  apenas **From address**.

## Inconsistências a resolver (padronização)

Os frames divergem entre si; a implementação deve fixar:

- **Encryption:** usar **controle segmentado** (`None`/`STARTTLS`/`SSL/TLS`), como no
  Empty_State — não usar dropdown.
- **Authentication:** usar **toggle** "Require credentials", como no Empty_State —
  não usar rádios `OAuth2 / Plain Login` (que introduzem OAuth2, fora de escopo).
- **Branding:** os frames vieram com "Portfolio Admin / Strategic Enterprise Ledger /
  Enterprise Control". O produto é **"Application Portfolio"**; renomear títulos e
  marca ao integrar.
- **Artefatos de renderização:** ignorar o `letter-spacing` quebrado do
  `Saving_State/` e o header duplicado no rodapé do `Test_Results/`.

## Estados a implementar

1. **Vazio / sem relay** — pill de aviso; formulário vazio. (base: Empty_State)
2. **Ativo / salvo** — pill verde; senha `•••••••• (saved)`. (base: Active_State)
3. **Erros de validação** — erros inline por campo. (base: Validation_Errors)
4. **Popover de e-mail de teste** aberto. (base: Test_Email_Popover)
5. **Resultado do teste** — toast de sucesso e variante de erro. (base: Test_Results)
6. **Salvando** — botão primário com spinner/desabilitado (sem painel de deploy).
