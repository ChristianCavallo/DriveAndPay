/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.drivepay.Connection;



import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AES {

    private final String ALGO;
    public String KeyValue;
    private final SecretKeySpec Key;

    public AES(String KeyValue) {
        this.KeyValue = KeyValue;
        this.ALGO = "AES";
        this.Key = new SecretKeySpec(KeyValue.getBytes(), ALGO);
    }

    public byte[] decryptAES(byte[] message) throws Exception {

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, Key);
        // return cipher.doFinal(message.getBytes());
        return cipher.doFinal(message);
    }

    
    public byte[] encryptAES(byte[] message) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, Key);
        return cipher.doFinal(message);
    }
    
    public byte[] encryptAES(String message) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, Key);
        return cipher.doFinal(message.getBytes());
    }

    /**
     * Encrypt a string with AES algorithm.
     *
     * @param data is a string
     * @return the encrypted string
     * @throws Exception
     */
    public String encrypt(String data) throws Exception {
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, Key);
        byte[] encVal = c.doFinal(data.getBytes());
        return Base64.encodeBase64String(encVal);
    }

    /**
     *
     * @param bytes
     * @return
     * @throws Exception
     */
    public byte[] encrypt(byte[] bytes) throws Exception {
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, Key);
        byte[] encVal = c.doFinal(bytes);
        return encVal;
    }

    /**
     * Decrypt a string with AES algorithm.
     *
     * @param encryptedData is a string
     * @return the decrypted string
     * @throws Exception
     */
    public String decrypt(String encryptedData) throws Exception {
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, Key);
        byte[] decordedValue = Base64.decodeBase64(encryptedData.trim());
        byte[] decValue = c.doFinal(decordedValue);
        return new String(decValue);
    }

    public byte[] decrypt(byte[] bytes) throws Exception {
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, Key);
        return c.doFinal(bytes);
    }

}
