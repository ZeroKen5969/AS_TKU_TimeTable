package com.example.as_tku_timetable;

import android.util.Base64;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public abstract class AESCrypt {

    private static final String skey = "你是87嗎?";

    public static String decrypt(String encryptContent) {
        try {
            SecretKeySpec key = new SecretKeySpec(MessageDigest.getInstance("SHA-256")
                    .digest(skey.getBytes("UTF-8")), "AES");
            IvParameterSpec iv = new IvParameterSpec(MessageDigest.getInstance("MD5")
                    .digest(skey.getBytes("UTF-8")));

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] decryptBytes = cipher.doFinal(
                    Base64.decode(encryptContent.getBytes("UTF-8"), Base64.DEFAULT)
            );
            return new String(decryptBytes, "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String encrypt(String content) {
        try {
            SecretKeySpec key = new SecretKeySpec(MessageDigest.getInstance("SHA-256")
                    .digest(skey.getBytes("UTF-8")), "AES");
            IvParameterSpec iv = new IvParameterSpec(MessageDigest.getInstance("MD5")
                    .digest(skey.getBytes("UTF-8")));

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encryptBytes = cipher.doFinal(content.getBytes("UTF-8"));
            return Base64.encodeToString(encryptBytes, Base64.DEFAULT);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }
}
