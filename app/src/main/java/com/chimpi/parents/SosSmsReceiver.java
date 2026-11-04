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
   if(allowed.isEmpty())return;
   SmsMessage[] messages=Telephony.Sms.Intents.getMessagesFromIntent(intent);
   if(messages==null||messages.length==0)return;
   String sender=messages[0].getOriginatingAddress();
   if(!authorized(sender,allowed))return;
   StringBuilder text=new StringBuilder();
   for(SmsMessage part:messages){
     if(part==null||!authorized(part.getOriginatingAddress(),allowed))return;
     String t=part.getMessageBody();
     if(t!=null)text.append(t);
   }
   String body=text.toString();
   if(!body.startsWith("CHIMPI-SOS | NÍVEL: ")||body.length()>1600)return;
   String level=body.contains("NÍVEL: EXTREMO")?"EXTREMO":body.contains("NÍVEL: ATENÇÃO")?"ATENÇÃO":"MÍNIMO";
   notifyEmergency(context,level,body);
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
