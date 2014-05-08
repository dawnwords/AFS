package util;

import data.Parameter;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.SecureRandom;

/**
 * Security Util Class using java-DES to encrypt and decrypt byte array
 * with given KEY store in both client and server side
 * <p/>
 * Created by Dawnwords on 2014/5/6.
 */
public class SecurityUtil {
    public static byte[] encrypt(byte[] content) {
        try {
            return getCipher(Cipher.ENCRYPT_MODE).doFinal(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt(byte[] content) {
        try {
            return getCipher(Cipher.DECRYPT_MODE).doFinal(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Cipher getCipher(int mode) throws Exception {
        SecureRandom random = new SecureRandom();
        DESKeySpec desKey = new DESKeySpec(Parameter.KEY.getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(desKey);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(mode, secretKey, random);
        return cipher;
    }


}
