package com.chimpi.parents;
import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.media.*;
import android.net.Uri;
import android.os.*;
import android.widget.*;

public class MainActivity extends Activity {
 final Handler h=new Handler(Looper.getMainLooper()); Ringtone tone; Runnable stopTask; EditText text; TextView status;
 static final String CHANNEL="chimpi-local-alertas";
 @Override public void onCreate(Bundle b){super.onCreate(b);
 ScrollView scroll=new ScrollView(this);LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(24,32,24,32);scroll.addView(box);setContentView(scroll);
 title(box,"🐒 Chimpi Parents Find My Kids");setupSmsChip(box);setupWifi(box);
 note(box,"PRÉVIA DE TESTES • O aplicativo não lê SMS automaticamente, nem valida a identidade de quem enviou");
 title(box,"Recebi um CHIMPI-SOS");
 text=new EditText(this);text.setHint("Cole o SMS recebido da criança");text.setMinLines(3);box.addView(text);
 button(box,"🔔 Analisar mensagem",0xff2458e7).setOnClickListener(v->analyze(text.getText().toString()));
 title(box,"Testar avisos locais");
 note(box,"Os botões abaixo só testam o som e a vibração NESTE aparelho");
 button(box,"🟢 Testar MÍNIMO",0xff258963).setOnClickListener(v->alarm("MÍNIMO","Aviso local de teste"));
 button(box,"🟠 Testar ATENÇÃO",0xffc87918).setOnClickListener(v->alarm("ATENÇÃO","Aviso local de teste"));
 button(box,"🚨 Testar EXTREMO",0xffc92b46).setOnClickListener(v->alarm("EXTREMO","Aviso local de teste"));
 button(box,"🔕 Silenciar",0xff63738c).setOnClickListener(v->stop());
 status=new TextView(this);status.setText("Nenhum alerta analisado");status.setTextSize(17);box.addView(status);
 title(box,"Como usar com Chimpi Find My Kids");
 note(box,"1 • Cadastre o telefone de um familiar no app da criança\n2 • A criança prepara o SMS e toca em Enviar no aplicativo de mensagens\n3 • Ao receber o SMS, copie e cole aqui ou use Compartilhar\n4 • Analise o aviso e entre em contato com a criança");
 note(box,"⚠️ Não existe pareamento automático, recebimento em segundo plano ou alarme remoto offline. Qualquer pessoa pode escrever CHIMPI-SOS; confirme a origem do aviso");
 button(box,"📞 LIGAR 190",0xffc92b46).setOnClickListener(v->{
 try{startActivity(new Intent(Intent.ACTION_DIAL,Uri.parse("tel:190")));}catch(Exception ignored){}
 });
 NotificationManager m=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
 if(m!=null){NotificationChannel c=new NotificationChannel(CHANNEL,"Avisos Chimpi locais",NotificationManager.IMPORTANCE_HIGH);m.createNotificationChannel(c);}
 if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
   requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},10);
 shared(getIntent());
 }
 @Override protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);shared(i);}
 void shared(Intent i){if(i==null||!Intent.ACTION_SEND.equals(i.getAction()))return;
 CharSequence s=i.getCharSequenceExtra(Intent.EXTRA_TEXT);
 if(s!=null&&text!=null){text.setText(s);analyze(s.toString());}
 if("com.chimpi.parents.SMS_SOS_ALERT".equals(i.getAction())){
   String message=i.getStringExtra("sos_body");
   String level=i.getStringExtra("sos_level");
   if(message!=null&&text!=null&&status!=null){
     text.setText(message);
     alarm(level==null?"EXTREMO":level,"SOS recebido pelo chip de contato cadastrado. A identidade não é verificada por criptografia:\n"+message);
     speakSmsAlert();
   }
 }}
 void analyze(String s){
 if(s==null||!s.contains("CHIMPI-SOS | NÍVEL:")){status.setText("Mensagem não reconhecida");return;}
 String level=s.contains("NÍVEL: EXTREMO")?"EXTREMO":s.contains("NÍVEL: ATENÇÃO")?"ATENÇÃO":"MÍNIMO";
 alarm(level,"Mensagem compartilhada MANUALMENTE. Origem NÃO VERIFICADA:\n"+s);
 }
 void alarm(String lvl,String content){
 stop();status.setText(lvl+"\n"+content);
 Uri uri=RingtoneManager.getDefaultUri(lvl.equals("EXTREMO")?RingtoneManager.TYPE_ALARM:RingtoneManager.TYPE_NOTIFICATION);
 if(uri!=null)try{tone=RingtoneManager.getRingtone(this,uri);if(tone!=null)tone.play();}catch(Exception ignored){}
 try{Vibrator vibr=(Vibrator)getSystemService(VIBRATOR_SERVICE);
 if(vibr!=null&&vibr.hasVibrator()){
 long[] p=lvl.equals("EXTREMO")?new long[]{0,450,150,450,150,450}:new long[]{0,200,150,200};
 vibr.vibrate(VibrationEffect.createWaveform(p,-1));
 }}catch(Exception ignored){}
 if(Build.VERSION.SDK_INT<33||checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED){
 NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
 if(nm!=null)nm.notify(2404,new Notification.Builder(this,CHANNEL)
  .setContentTitle("Chimpi Parents • "+lvl).setContentText("Alerta analisado localmente").setAutoCancel(true)
  .setSmallIcon(android.R.drawable.ic_dialog_alert).build());}
 stopTask=()->stop();h.postDelayed(stopTask,12000);
 }
 void stop(){if(stopTask!=null)h.removeCallbacks(stopTask);if(tone!=null)try{tone.stop();}catch(Exception ignored){}tone=null;}
 void title(LinearLayout box,String s){TextView t=new TextView(this);t.setText(s);t.setTextSize(21);t.setTypeface(Typeface.DEFAULT,1);t.setPadding(0,18,0,8);box.addView(t);}
 void note(LinearLayout box,String s){TextView t=new TextView(this);t.setText(s);t.setTextSize(14);box.addView(t);}
 Button button(LinearLayout box,String s,int color){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(-1);b.setBackgroundTintList(ColorStateList.valueOf(color));box.addView(b);return b;}
 @Override protected void onDestroy(){stop();if(voice!=null){voice.stop();voice.shutdown();voice=null;}super.onDestroy();}

 // ================== EventoSOS na rede Wi-Fi local ==================
 void setupWifi(LinearLayout box){
   title(box,"📶 EventoSOS | Parear celular da criança");
   note(box,"Os dois aparelhos precisam estar na mesma rede Wi-Fi. Para receber SOS, ative a escuta e mantenha a notificação de serviço visível. Não garante entrega");
   android.content.SharedPreferences prefs=getSharedPreferences("pair",MODE_PRIVATE);
   if(!prefs.contains("secret")){byte[] bytes=new byte[32];new java.security.SecureRandom().nextBytes(bytes);prefs.edit().putString("secret",android.util.Base64.encodeToString(bytes,android.util.Base64.NO_WRAP|android.util.Base64.NO_PADDING|android.util.Base64.URL_SAFE)).apply();}
   String ip=localIp();
   TextView address=new TextView(this);address.setText("IP deste celular: "+ip+"\nPorta: 45881");address.setTextSize(19);address.setTextIsSelectable(true);box.addView(address);
   TextView codeView=new TextView(this);codeView.setText("Chave de pareamento (32 bytes):\n"+prefs.getString("secret",""));codeView.setTextIsSelectable(true);codeView.setTextSize(15);box.addView(codeView);
   note(box,"Copie o IP e a chave no app da criança, somente com autorização do responsável. Não divulgue a chave");
   button(box,"✅ ATIVAR RECEBIMENTO SOS",0xff258963).setOnClickListener(v->{
     try{
       Intent i=new Intent(this,SosListenerService.class);i.setAction("START");
       if(Build.VERSION.SDK_INT>=26)startForegroundService(i);else startService(i);
       Toast.makeText(this,"Recebimento ativado: confira notificação",Toast.LENGTH_LONG).show();
     }catch(Exception e){Toast.makeText(this,"Não foi possível ativar: "+e.getMessage(),Toast.LENGTH_LONG).show();}
   });
   button(box,"⛔ Parar recebimento",0xff63738c).setOnClickListener(v->{
     stopService(new Intent(this,SosListenerService.class));
     Toast.makeText(this,"Recebimento interrompido",Toast.LENGTH_LONG).show();
   });
   button(box,"🔁 Trocar chave de segurança",0xff63738c).setOnClickListener(v->
     new AlertDialog.Builder(this).setTitle("Revogar a chave antiga?")
     .setMessage("Os celulares que usam a chave anterior deixarão de conseguir enviar SOS até serem pareados novamente")
     .setNegativeButton("Cancelar",null).setPositiveButton("Trocar chave",(d,w)->{
       byte[] bytes=new byte[32];new java.security.SecureRandom().nextBytes(bytes);
       String newKey=android.util.Base64.encodeToString(bytes,android.util.Base64.URL_SAFE|android.util.Base64.NO_WRAP|android.util.Base64.NO_PADDING);
       prefs.edit().putString("secret",newKey).apply();codeView.setText("Chave de pareamento (32 bytes):\n"+newKey);
     }).show());
 }
 String localIp(){
   try{
     java.util.Enumeration<java.net.NetworkInterface> interfaces=java.net.NetworkInterface.getNetworkInterfaces();
     String found="";
     while(interfaces.hasMoreElements()){
       java.net.NetworkInterface n=interfaces.nextElement();
       if(!n.isUp()||n.isLoopback())continue;
       java.util.Enumeration<java.net.InetAddress> ips=n.getInetAddresses();
       while(ips.hasMoreElements()){
         java.net.InetAddress addr=ips.nextElement();
         if(addr instanceof java.net.Inet4Address&&addr.isSiteLocalAddress()){
           if(n.getName().startsWith("wlan"))return addr.getHostAddress();
           if(found.isEmpty())found=addr.getHostAddress();
         }
       }
     }
     if(!found.isEmpty())return found;
   }catch(Exception ignored){}
   return "Wi-Fi não encontrado";
 }


 // ================== EventoSOS por chip/SMS ==================
 EditText childNumber;
 android.speech.tts.TextToSpeech voice;
 void setupSmsChip(LinearLayout root){
   title(root,"📱 EventoSOS via chip (Brasil e exterior)");
   note(root,"Cadastre somente o número da criança autorizada, com DDI (+55 no Brasil). O app não monitora SMS de outros números");
   android.content.SharedPreferences p=getSharedPreferences("smsfamily",MODE_PRIVATE);
   childNumber=new EditText(this);
   childNumber.setHint("Número da criança: +55 21 99999-9999");
   childNumber.setSingleLine(true);
   childNumber.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
   childNumber.setText(p.getString("child_phone",""));
   root.addView(childNumber);
   button(root,"✅ ATIVAR AVISO SOS POR SMS",0xff258963).setOnClickListener(v->{
     String digits=childNumber.getText().toString().trim().replaceAll("[^+0-9]","");
     if(!digits.matches("\\+\\d{10,15}")){
       Toast.makeText(this,"Digite o número com DDI, por exemplo +5521999999999",Toast.LENGTH_LONG).show();
       return;
     }
     new AlertDialog.Builder(this).setTitle("Ativar alerta do chip?")
       .setMessage("Autorizar avisos apenas de "+digits+"? O aplicativo receberá a notificação de SOS se houver sinal celular e se o Android permitir. SMS pode ser falsificado. Confirme com a criança")
       .setNegativeButton("Cancelar",null)
       .setPositiveButton("Ativar",(d,w)->{
         p.edit().putString("child_phone",digits).putBoolean("sms_enabled",true).apply();
         if(checkSelfPermission(Manifest.permission.RECEIVE_SMS)!=PackageManager.PERMISSION_GRANTED)
           requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS},88);
         else Toast.makeText(this,"Avisos por chip ativados",Toast.LENGTH_LONG).show();
       }).show();
   });
   button(root,"⛔ DESATIVAR AVISO POR SMS",0xff63738c).setOnClickListener(v->{
     p.edit().putBoolean("sms_enabled",false).apply();
     Toast.makeText(this,"Leitura de SOS por chip desativada",Toast.LENGTH_LONG).show();
   });
   note(root,"Os alertas recebidos pelo chip usam notificação com som e vibração. Ao abrir o aviso, o app também tentará falar ALERTA! SEU FILHO OU FILHA ESTÁ EM APUROS!");
   note(root,"No Reino Unido, cadastre o mesmo número brasileiro da criança. Com sinal de operadora e SMS internacional funcionando, a tia no Brasil e os pais no exterior podem receber mensagens separadas");
 }
 void speakSmsAlert(){
   try{
     if(voice!=null){voice.stop();voice.shutdown();voice=null;}
     voice=new android.speech.tts.TextToSpeech(this,statusCode->{
       if(statusCode!=android.speech.tts.TextToSpeech.SUCCESS||voice==null)return;
       voice.setLanguage(new java.util.Locale("pt","BR"));
       voice.setAudioAttributes(new android.media.AudioAttributes.Builder()
         .setUsage(android.media.AudioAttributes.USAGE_ALARM)
         .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH).build());
       voice.speak("ALERTA! SEU FILHO OU FILHA ESTÁ EM APUROS!",
         android.speech.tts.TextToSpeech.QUEUE_FLUSH,null,"chimpi-sms-alert");
     });
   }catch(Exception ignored){}
 }
 @Override public void onRequestPermissionsResult(int code,String[] requested,int[] grants){
   super.onRequestPermissionsResult(code,requested,grants);
   if(code==88){
     if(grants.length==0||grants[0]!=PackageManager.PERMISSION_GRANTED){
       getSharedPreferences("smsfamily",MODE_PRIVATE).edit().putBoolean("sms_enabled",false).apply();
       Toast.makeText(this,"Sem permissão de SMS. Aviso por chip não ativado",Toast.LENGTH_LONG).show();
     }else Toast.makeText(this,"Avisos por chip ativados",Toast.LENGTH_LONG).show();
   }
 }

}
