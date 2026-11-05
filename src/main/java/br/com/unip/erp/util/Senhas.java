package br.com.unip.erp.util;
import java.security.*;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
public final class Senhas  {
    private static byte[] derive(char[] senha,byte[] sal)throws Exception {
        PBEKeySpec spec=new PBEKeySpec(senha,sal,120000,256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        }
        finally {
            spec.clearPassword();
        }
    }
    public static String hash(String senha)throws Exception {
        byte[] sal=new byte[16];
        new SecureRandom().nextBytes(sal);
        return "pbkdf2$"+Base64.getEncoder().encodeToString(sal)+"$"+Base64.getEncoder().encodeToString(derive(senha.toCharArray(),sal));
    }
    public static boolean confere(char[] senha,String hash)throws Exception {
        // Compatibilidade com as senhas antigas de varchar(20). Novas senhas
        // são sempre gravadas com PBKDF2, após executar 02_senhas_seguras.sql.
        if(hash==null || hash.isEmpty()) return false;
        if(!hash.startsWith("pbkdf2$")) return MessageDigest.isEqual(
            new String(senha).getBytes(java.nio.charset.StandardCharsets.UTF_8),
            hash.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String[] p=hash.split("\\$");
        return p.length==3&&p[0].equals("pbkdf2")&&MessageDigest.isEqual(derive(senha,Base64.getDecoder().decode(p[1])),Base64.getDecoder().decode(p[2]));
    }
}
