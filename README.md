# 🐒 Chimpi Parents Find My Kids

**Prévia experimental Android para familiares**

APK em [Releases](../../releases) após compilação automática

Funções: colar/compartilhar texto CHIMPI-SOS, identificar 3 gravidades, aviso sonoro, vibração, notificação e opção silenciar

⚠️ A mensagem precisa ser copiada ou compartilhada manualmente. Não há recebimento automático de SMS ou alerta em segundo plano. O som depende das configurações do Android e o texto não é autenticado

Em perigo imediato ligue **190**

`gradle :app:assembleDebug` (Java 17, Gradle 8.9, SDK 35)

## 📶 EventoSOS por Wi-Fi (v1.1.0-preview.1)

1. Conecte os dois celulares à **mesma rede Wi-Fi**
2. No celular do responsável, abra o app e toque **ATIVAR RECEBIMENTO SOS**, permitindo notificações
3. Veja o IP local e a chave secreta de pareamento exibidos no app
4. No app da criança, informe o IP e a chave, teste a conexão, e envie um EventoSOS
5. Se a mensagem for recebida, o responsável verá notificação e ouvirá voz sintetizada: **“ALERTA! SEU FILHO OU FILHA ESTÁ EM APUROS!”**

O serviço do responsável só fica ativo após a pessoa o ligar voluntariamente e mostra notificação permanente. **Não é rastreamento**, nem chamada à polícia. Não funciona em redes diferentes, com aparelhos sem Wi-Fi local, roteadores com isolamento de dispositivos, ou se o Android encerrar o serviço. Não há entrega garantida nem confirmação humana de visualização.

A chave permite autenticar e criptografar mensagens AES-256-GCM nesta rede local; jamais compartilhe com desconhecidos. Use **Trocar chave** para revogar.

## 🌍 EventoSOS por chip/SMS v1.2.0-preview.1

Cada familiar (tia no Brasil, mãe e pai no Reino Unido, por exemplo) pode instalar este aplicativo e cadastrar o mesmo telefone da criança com DDI, como **+55**, além de conceder as permissões de **RECEIVE_SMS** e **POST_NOTIFICATIONS** no Android

Se um SMS começar com `CHIMPI-SOS | NÍVEL:` e vier do telefone cadastrado, o app tenta emitir **notificação com alarme e vibração**. Ao abrir a notificação, tenta ler em voz alta **“ALERTA! SEU FILHO OU FILHA ESTÁ EM APUROS!”**

A configuração exige autorização de cada adulto. A opção **DESATIVAR AVISO POR SMS** interrompe a detecção. O app não lê mensagens de outros números dentro do código da aplicação, mas a permissão do Android é sensível, e sua concessão é responsabilidade do adulto

**Sem garantia de entrega**: a criança deve ter SMS da operadora, os familiares precisam de sinal celular e pode haver bloqueio de SMS internacional e custo. O telefone remetente pode ser falsificado, portanto a identificação por número **não equivale a autenticação criptográfica**. Notificações podem ser silenciadas pelo Android e o aplicativo não confirma visualização

Esta versão é experimental e distribuída via GitHub para testes. O uso de permissões SMS é restrito por políticas do Google Play e não implica aceitação da loja
