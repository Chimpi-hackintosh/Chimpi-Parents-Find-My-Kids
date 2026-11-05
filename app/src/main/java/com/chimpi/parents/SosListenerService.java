package com.chimpi.parents;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.media.*;
import android.net.Uri;
import android.os.*;
import android.speech.tts.TextToSpeech;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Receptor experimental de rede local, iniciado voluntariamente pelo responsável.
 * Nenhuma confirmação de leitura/escuta do alerta é possível.
 */
public class SosListenerService extends Service {
 static final int PORT=45881;
 static final String MONITOR="chimpi_wifi_monitor";
 static final String ALERTS="chimpi_wifi_alerts";
 volatile boolean active=false;
 ServerSocket server;
 Thread thread;
 TextToSpeech tts;
 boolean ttsReady=false;
 Handler main=new Handler(Looper.getMainLooper());
 Ringtone ringtone;
 final LinkedHashSet<String> seen=new LinkedHashSet<>();
 volatile long lastAlarm=0;

 @Override public void onCreate(){
   super.onCreate();
   NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
   nm.createNotificationChannel(new NotificationChannel(MONITOR,"Escuta EventoSOS por Wi-Fi",NotificationManager.IMPORTANCE_LOW));
   NotificationChannel alertChannel=new NotificationChannel(ALERTS,"Emergências EventoSOS",NotificationManager.IMPORTANCE_HIGH);
   alertChannel.enableVibration(true);
   nm.createNotificationChannel(alertChannel);
   tts=new TextToSpeech(this,s->{
     if(s==TextToSpeech.SUCCESS&&tts!=null){
       int result=tts.setLanguage(new Locale("pt","BR"));
       ttsReady=result!=TextToSpeech.LANG_MISSING_DATA&&result!=TextToSpeech.LANG_NOT_SUPPORTED;
       if(ttsReady){
         tts.setAudioAttributes(new AudioAttributes.Builder()
           .setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build());
       }
     }
   });
 }
 @Override public int onStartCommand(Intent intent,int flags,int startId){
   if(intent!=null&&"STOP".equals(intent.getAction())){stopSelf();return START_NOT_STICKY;}
   if(active)return START_NOT_STICKY;
   Intent open=new Intent(this,MainActivity.class);
   PendingIntent pi=PendingIntent.getActivity(this,0,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
   Intent stopIntent=new Intent(this,SosListenerService.class).setAction("STOP");
   PendingIntent stopPi=PendingIntent.getService(this,1,stopIntent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
   Notification ongoing=new Notification.Builder(this,MONITOR)
     .setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Chimpi Parents: escuta ativa")
     .setContentText("Aguardando EventoSOS na mesma rede Wi-Fi")
     .setContentIntent(pi).addAction(android.R.drawable.ic_delete,"Desativar",stopPi).setOngoing(true).build();
   if(Build.VERSION.SDK_INT>=29)startForeground(45881,ongoing,ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
   else startForeground(45881,ongoing);
   active=true;
   thread=new Thread(this::listen,"ChimpiSOS-Receiver");
   thread.start();
   return START_NOT_STICKY;
 }
 void listen(){
   try(ServerSocket ss=new ServerSocket(PORT)){
     server=ss;
     ss.setSoTimeout(4000);
     while(active){
       try(Socket client=ss.accept()){process(client);}
       catch(SocketTimeoutException ignored){}
       catch(Exception ignored){}
     }
   }catch(IOException e){
     if(active)main.post(()->{
       NotificationManager m=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
       m.notify(45883,new Notification.Builder(this,ALERTS)
         .setSmallIcon(android.R.drawable.stat_notify_error)
         .setContentTitle("Chimpi: escuta indisponível")
         .setContentText("Não foi possível abrir a porta local. Abra o app e reinicie a escuta")
         .build());
       stopSelf();
     });
   }
 }
 void process(Socket sock)throws Exception{
   sock.setSoTimeout(5000);
   DataInputStream in=new DataInputStream(new BufferedInputStream(sock.getInputStream()));
   DataOutputStream out=new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
   String packet=in.readUTF();
   if(packet.length()>7000)return;
   String key=getSharedPreferences("pair",MODE_PRIVATE).getString("secret","");
   JSONObject event=SosCrypto.decrypt(key,packet);
   String id=event.optString("id","");
   String type=event.optString("type","");
   long when=event.optLong("time",0L);
   if(!id.matches("[a-f0-9\\-]{36}")||Math.abs(System.currentTimeMillis()-when)>300000)return;
   if("TEST".equals(type)){out.writeUTF("TEST_OK:"+id);out.flush();return;}
   if(!"SOS".equals(type))return;
   if("parent".equals(getSharedPreferences("smsfamily",MODE_PRIVATE).getString("role","caregiver")))return; // Não mostrar detalhes ao perfil dos pais
   String severity=event.optString("level","EXTREMO");
   if(!"MÍNIMO".equals(severity)&&!"ATENÇÃO".equals(severity)&&!"EXTREMO".equals(severity))return;
   String message=event.optString("message","Preciso de ajuda");
   if(message.length()>800)message=message.substring(0,800);
   if(seen.contains(id)){out.writeUTF("ACK:"+id);out.flush();return;}
   seen.add(id);
   if(seen.size()>300)seen.remove(seen.iterator().next());
   final String summary=message, lvl=severity;
   main.post(()->showAlert(lvl,summary));
   out.writeUTF("ACK:"+id);out.flush();
 }
 void showAlert(String level,String message){
   String heading="ALERTA! SEU FILHO/A ESTÁ EM APUROS!";
   NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
   Intent open=new Intent(this,MainActivity.class);
   PendingIntent pi=PendingIntent.getActivity(this,2,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
   nm.notify((int)(System.currentTimeMillis()%2000000000L),
     new Notification.Builder(this,ALERTS)
       .setSmallIcon(android.R.drawable.ic_dialog_alert)
       .setContentTitle(heading)
       .setContentText("Nível: "+level+" | "+message)
       .setStyle(new Notification.BigTextStyle().bigText("Nível: "+level+"\n"+message))
       .setCategory(Notification.CATEGORY_ALARM).setContentIntent(pi).setAutoCancel(true)
       .build());
   // Rate limit de alarmes repetidos, mas sempre registra a notificação
   if(System.currentTimeMillis()-lastAlarm<4000)return;
   lastAlarm=System.currentTimeMillis();
   try{
     Vibrator v=(Vibrator)getSystemService(VIBRATOR_SERVICE);
     if(v!=null&&v.hasVibrator())v.vibrate(VibrationEffect.createWaveform(new long[]{0,420,180,420,180,420},-1));
   }catch(Exception ignored){}
   try{
     if(ttsReady)tts.speak("ALERTA! SEU FILHO OU FILHA ESTÁ EM APUROS!",TextToSpeech.QUEUE_FLUSH,null,"eventosos-"+lastAlarm);
   }catch(Exception ignored){}
   main.postDelayed(()->{
     try{
       Uri ring=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
       if(ring!=null){
         if(ringtone!=null&&ringtone.isPlaying())ringtone.stop();
         ringtone=RingtoneManager.getRingtone(this,ring);
         if(ringtone!=null)ringtone.play();
         main.postDelayed(()->{try{if(ringtone!=null)ringtone.stop();}catch(Exception ignored){}},7000);
       }
     }catch(Exception ignored){}
   },3000);
 }
 @Override public void onDestroy(){
   active=false;
   try{if(server!=null)server.close();}catch(Exception ignored){}
   if(thread!=null)thread.interrupt();
   main.removeCallbacksAndMessages(null);
   try{if(ringtone!=null)ringtone.stop();}catch(Exception ignored){}
   if(tts!=null){tts.stop();tts.shutdown();}
   super.onDestroy();
 }
 @Override public IBinder onBind(Intent i){return null;}
}
