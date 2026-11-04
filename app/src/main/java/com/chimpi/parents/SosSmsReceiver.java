package com.chimpi.parents;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.*;
import android.provider.Telephony;
import android.telephony.SmsMessage;

public class SosSmsReceiver extends BroadcastReceiver {
 private static final String CHANNEL="chimpi_sms_eventosos_v12";
 @Override public void onReceive(Context context,Intent intent){
   if(intent==null||!Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction()))return;
   if(context.checkSelfPermission(Manifest.permission.RECEIVE_SMS)!=PackageManager.PERMISSION_GRANTED)return;
   android.content.SharedPreferences prefs=context.getSharedPreferences("smsfamily",Context.MODE_PRIVATE);
   if(!prefs.getBoolean("sms_enabled",false))return;
   String allowed=prefs.getString("child_phone","");
   String caregiver=prefs.getString("caregiver_phone","");
   String role=prefs.getString("role","caregiver");
   if(allowed.isEmpty())return;
   SmsMessage[] messages=Telephony.Sms.Intents.getMessagesFromIntent(intent);
   if(messages==null||messages.length==0)return;
   String sender=messages[0].getOriginatingAddress();

   boolean senderIsChild=authorized(sender,allowed);
   boolean senderIsCaregiver=authorized(sender,caregiver);
   StringBuilder text=new StringBuilder();
   for(SmsMessage part:messages){
     if(part==null)return;
     String from=part.getOriginatingAddress();
     if(!authorized(from,allowed)&&!authorized(from,caregiver))return;
     String fragment=part.getMessageBody();
     if(fragment!=null)text.append(fragment);
   }
   String body=text.toString();
   if(body.length()>1600)return;
   if("parent".equals(role)){
     // Pais recebem apenas o andamento, NÃO recebem conteúdo sensível do SOS
     if(body.startsWith("CHIMPI-STATUS | AGUARDANDO")&&senderIsChild){
       notifyStatus(context,body,false);
     }else if(body.startsWith("CHIMPI-STATUS | CONFIRMADO")&&senderIsCaregiver){
       notifyStatus(context,body,true);
     }
     return;
   }
   if(!"caregiver".equals(role))return;
   if(!senderIsChild||!body.startsWith("CHIMPI-SOS | NÍVEL: "))return;
   String incident=parseId(body);
   if(incident.isEmpty())return;
   prefs.edit().putString("last_incident",incident).putLong("last_incident_at",System.currentTimeMillis()).apply();
   String level=body.contains("NÍVEL: EXTREMO")?"EXTREMO":body.contains("NÍVEL: ATENÇÃO")?"ATENÇÃO":"MÍNIMO";
   notifyEmergency(context,level,body);

 }

 private static String parseId(String body){
   java.util.regex.Matcher m=java.util.regex.Pattern.compile("(?m)^ID: ([A-Z0-9]{8})$").matcher(body);
   return m.find()?m.group(1):"";
 }
 private static void notifyStatus(Context c,String body,boolean confirmed){
   NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
   if(nm==null)return;
   String channelId="chimpi_status_v13";
   NotificationChannel ch=new NotificationChannel(channelId,"Andamento da ajuda",NotificationManager.IMPORTANCE_DEFAULT);
   ch.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
   nm.createNotificationChannel(ch);
   String headline=confirmed?"Um familiar confirmou que está ajudando":"Um familiar foi acionado";
   String content=confirmed?"Ajuda confirmada pelo próprio familiar":"Aguardando confirmação de ajuda";
   Intent open=new Intent(c,MainActivity.class);
   open.setAction("com.chimpi.parents.STATUS_UPDATE");
   open.putExtra("status_message",headline+"\n"+content+"\n"+parseId(body));
   open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
   PendingIntent pi=PendingIntent.getActivity(c,(int)(System.currentTimeMillis()%1000000000L),open,
       PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
   if(Build.VERSION.SDK_INT>=33&&c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return;
   nm.notify((int)(System.currentTimeMillis()%1000000000L),
     new Notification.Builder(c,channelId).setSmallIcon(android.R.drawable.ic_dialog_info)
       .setContentTitle(headline).setContentText(content)
       .setCategory(Notification.CATEGORY_STATUS).setVisibility(Notification.VISIBILITY_PRIVATE)
       .setContentIntent(pi).setAutoCancel(true).build());
 }

 private boolean authorized(String sender,String expected){
   if(sender==null)return false;
   String a=sender.replaceAll("[^0-9]","");
   String b=expected.replaceAll("[^0-9]","");
   if(a.length()<10||b.length()<10)return false;
   if(a.equals(b))return true;
   // Algumas operadoras exibem um número brasileiro sem o DDI +55
   return b.startsWith("55")&&a.equals(b.substring(2));
 }
 static void notifyEmergency(Context c,String level,String body){
   NotificationManager manager=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
   if(manager==null)return;
   NotificationChannel channel=new NotificationChannel(CHANNEL,"EventoSOS por chip",NotificationManager.IMPORTANCE_HIGH);
   channel.enableVibration(true);
   channel.setVibrationPattern(new long[]{0,450,150,450,150,500});
   Uri sound=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
   if(sound!=null)channel.setSound(sound,new AudioAttributes.Builder()
     .setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());
   channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
   manager.createNotificationChannel(channel);
   Intent open=new Intent(c,MainActivity.class);
   open.setAction("com.chimpi.parents.SMS_SOS_ALERT");
   open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
   open.putExtra("sos_body",body);
   open.putExtra("sos_level",level);
   PendingIntent pi=PendingIntent.getActivity(c,(int)(System.currentTimeMillis()%1000000),open,
     PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
   if(Build.VERSION.SDK_INT>=33&&c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return;
   Notification n=new Notification.Builder(c,CHANNEL)
     .setSmallIcon(android.R.drawable.ic_dialog_alert)
     .setContentTitle("ALERTA! SEU FILHO/A ESTÁ EM APUROS!")
     .setContentText("SOS por chip • "+level+" • toque para ouvir")
     .setStyle(new Notification.BigTextStyle().bigText("EventoSOS por SMS de número cadastrado. Nível: "+level+". Toque para verificar e ouvir"))
     .setCategory(Notification.CATEGORY_ALARM).setContentIntent(pi).setAutoCancel(true)
     .setVisibility(Notification.VISIBILITY_PRIVATE).build();
   manager.notify((int)(System.currentTimeMillis()%1000000000),n);
 }
}
