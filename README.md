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


## Aviso de suporte

O aplicativo compara a versão instalada com a prévia realmente publicada v1.1.0-preview.1 e usa o idioma do Android — português ou inglês como alternativa

Antes de 04/11/2026 às 00h UTC o texto informa a aproximação do fim do suporte e, a partir desse instante, informa que versões anteriores não recebem mais atualizações de segurança

Versões antigas que ainda não possuem o verificador precisam de uma atualização manual inicial pela página oficial de Releases

Código em PR não é um novo APK publicado nem uma atualização já instalada
