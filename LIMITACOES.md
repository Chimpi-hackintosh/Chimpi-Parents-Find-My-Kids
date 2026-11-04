# Prévia experimental, NÃO é um sistema de segurança certificado

Este APK é **debug de testes**

- O app somente reconhece e toca alertas fornecidos manualmente ou compartilhados por outro app
- Não monitora SMS, não recebe automaticamente avisos com app fechado e não garante alerta remoto offline
- Texto SMS não autenticado: confirme a identidade da criança antes de confiar
- Sons podem ser silenciados pelo Android; não ultrapassam o modo Não Perturbe
- Não existe sincronização automática com app infantil ou com autoridades
- **Em perigo imediato no Brasil, ligue 190**

## v1.1.0 Wi-Fi local

- Destinado a testes dentro de uma mesma rede local, sem servidor externo
- Necessário ativar serviço em primeiro plano manualmente no telefone dos responsáveis
- Som e voz dependem de volume, idioma da síntese de fala, modo silencioso/Não Perturbe e restrições do Android
- Confirmação "ACK" significa que o aplicativo recebeu os dados, **não** que um adulto ouviu ou respondeu
- Sem localização remota, ligação automática para polícia, comunicação entre redes Wi-Fi, backup ou garantias de entrega
- Teste com o responsável antes de usar e não dependa deste protótipo como único meio de pedir ajuda

## EventoSOS pelo chip (1.2)

- O responsável precisa cadastrar manualmente o número do remetente com DDI e autorizar RECEIVE_SMS e notificações
- O app inspeciona eventos SMS_RECEIVED para identificar mensagens CHIMPI-SOS do número registrado; números podem ser falsificados
- O Android pode negar permissão de SMS, impedir a notificação ou silenciar o alarme devido a configurações e restrições de bateria
- A fala em voz alta só é tentada quando o responsável abre a notificação; receber SMS em segundo plano não garante fala automática
- O funcionamento à distância depende da operadora, do envio internacional e da conectividade celular; não depende do mesmo Wi-Fi
- Não oferece leitura confirmada, despacho policial, rastreamento automático ou garantia de que um adulto recebeu o aviso
- Google Play restringe RECEIVE_SMS; este código é experimental e não está aprovado para distribuição na loja
