package com.chimpi.parents;
import android.util.Base64;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class SosCrypto {
 private SosCrypto(){}
 public static JSONObject decrypt(String key,String encoded)throws Exception{
   byte[] secret=Base64.decode(key,Base64.URL_SAFE|Base64.NO_WRAP|Base64.NO_PADDING);
   if(secret.length!=32)throw new SecurityException("Chave inválida");
   byte[] raw=Base64.decode(encoded,Base64.URL_SAFE|Base64.NO_WRAP|Base64.NO_PADDING);
   if(raw.length<29||raw.length>8000)throw new SecurityException("Pacote inválido");
   byte[] iv=new byte[12];System.arraycopy(raw,0,iv,0,12);
   Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
   cipher.init(Cipher.DECRYPT_MODE,new SecretKeySpec(secret,"AES"),new GCMParameterSpec(128,iv));
   byte[] plain=cipher.doFinal(raw,12,raw.length-12);
   return new JSONObject(new String(plain,StandardCharsets.UTF_8));
 }
}
