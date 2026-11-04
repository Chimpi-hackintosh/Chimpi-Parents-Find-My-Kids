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
 title(box,"🐒 Chimpi Parents Find My Kids");
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
 if(s!=null&&text!=null){text.setText(s);analyze(s.toString());}}
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
 @Override protected void onDestroy(){stop();super.onDestroy();}
}