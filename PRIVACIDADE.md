# Privacidade | Chimpi Parents Find My Kids 1.1.0-preview.1

Esta versão experimental não usa servidor próprio, anúncios, telemetria nem rastreamento contínuo

- O responsável precisa **ativar voluntariamente** a escuta por Wi-Fi, que mantém uma notificação persistente e pode ser interrompida a qualquer momento
- O app aceita mensagens EventoSOS autenticadas e criptografadas com uma chave de pareamento de 256 bits, somente por conexão na rede local
- A chave de pareamento é mantida nas preferências privadas do app Android e é exibida na tela do responsável para cadastramento manual; não deve ser compartilhada com terceiros
- O responsável pode **trocar a chave**, revogando os dispositivos que usavam a chave anterior
- As notificações de alerta podem conter mensagens ou localização que a criança escolheu compartilhar, e podem aparecer na tela bloqueada conforme as configurações do Android
- Não há servidor remoto, identificação real verificada, chave guardada em Keystore, garantia de que uma pessoa ouviu o alerta ou integração com a polícia

A instalação e o pareamento devem ocorrer com conhecimento e consentimento de um responsável legal

Antes de distribuição como serviço de segurança infantil, são necessários auditoria independente, testes de confiabilidade, proteção de dados, autenticação de identidade e medidas de recuperação de falhas

## SMS do número autorizado (v1.2)

Ao tocar **ATIVAR AVISO SOS POR SMS**, o adulto informa um único número da criança. A permissão RECEIVE_SMS permite que o Android entregue eventos de recebimento ao aplicativo. O app descarta mensagens que não tenham o prefixo `CHIMPI-SOS | NÍVEL:` e não correspondam ao número registrado, mas a permissão do sistema é sensível. O número cadastrado e a opção de ativação permanecem armazenados somente no aparelho do adulto. O conteúdo de um alerta pode aparecer como notificação privada, e a pessoa pode desligar o recebimento a qualquer momento. Remetentes SMS podem ser falsificados e o app não verifica identidade por criptografia
