package io.sim;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;

public class CryptoUtils {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    
    // Chave e IV fixos para fins de teste
    private static final String STATIC_KEY = "8hxU*H#fNgyPu^Cx6bMC%7nRvFvP*+qT";
    private static final String STATIC_IV = "1aG9$2pE8b4!sMxP";

    public static byte[] getStaticKey() {
        return STATIC_KEY.getBytes();
    }

    public static byte[] getStaticIV() {
        return STATIC_IV.getBytes();
    }

    public static byte[] encrypt(byte[] key, byte[] iv, byte[] input) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        return cipher.doFinal(input);
    }

    public static byte[] decrypt(byte[] key, byte[] iv, byte[] input) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        return cipher.doFinal(input);
    }
    
}

