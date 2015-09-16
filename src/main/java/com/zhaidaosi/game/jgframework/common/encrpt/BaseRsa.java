package com.zhaidaosi.game.jgframework.common.encrpt;

import com.zhaidaosi.game.jgframework.Boot;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class BaseRsa {

    private static PublicKey publicKey;
    private static PrivateKey privateKey;

    static {
        if (publicKey == null) {
            publicKey = getPublicKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCifAl6RlbG1PVOowZJ2niVlijq9FC19jEIaA2WVm+En64roRsTjlWpAKOfYBHIwEYWvI7rObyobTIPyOkBOCx5Sbopq2ME7FUQEwI2IeEBGHwnIBPzkhTEt9kMT88g8hZRBV6D/p6J8Z1u2WU0q88Xpd4o7VDxFRmGUTSePOGcjQIDAQAB");
        }
        if (privateKey == null) {
            privateKey = getPrivateKey("MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKJ8CXpGVsbU9U6jBknaeJWWKOr0ULX2MQhoDZZWb4SfriuhGxOOVakAo59gEcjARha8jus5vKhtMg/I6QE4LHlJuimrYwTsVRATAjYh4QEYfCcgE/OSFMS32QxPzyDyFlEFXoP+nonxnW7ZZTSrzxel3ijtUPEVGYZRNJ484ZyNAgMBAAECgYA3SHCJE8mOmQJloP4Qvq5sZszBNCMJ5hvEunJ1Bi+nNhUybvwhaTon6DnDjhI+9XxjXABcdCaGP7DawgbVDWHDzjgQG0xQle2ryrZFa0thgQDYM4iraMgxMN/5kTXD9DlZcf871N0DeI8dTpxJhVMcM5d95sml6pJxxqwyzABiAQJBAO1rvTpHS6OgDVRpHV4HksyEcKETEayVknAIbTGP3VH3L4X50CpwOwPsvaHi5sENkSEA2JtKj0qF0CbP3sAdDcECQQCvMxhuq2V+dRyMkFLot3YZbMffr2dJi6o4XwxDtI/nhHzS5bQIzKX7L1m8ZJ0GR6CSjZ6X+LBRW6h3tLTsNdnNAkANsK+5o5DN/5WlL2Z9HIyvdFeWQiY7wGgwQ5wgRn5pkopP/Gave8c7Y7RPmGjb6u9aatUSp0r57hthkYzzoPlBAkBG6sfZBEfxCDamL0VgLeMAJ6hAQx/sBTzB1LeCMHSPonFkbNaTOUN2iZQpThDBmfzFVc38dg3o4NEwo1UYyDOBAkAsfnopb+Msp8pMBw7mcX/CeCXNIVpBNWSLtT0AssDf9ofaQ+bIsFIsy2GaPt/UuZmltXgTP5J3iWqFIKQDRstH");
        }
    }

    public static void init(String publicKeyString, String privateKeyString) {
        publicKey = getPublicKey(publicKeyString);
        privateKey = getPrivateKey(privateKeyString);
    }

    public static String encrypt(String data) {
        if (data == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return DatatypeConverter.printBase64Binary(cipher.doFinal(data.getBytes(Boot.getCharset())));
        } catch (Exception e) {
            return null;
        }
    }

    public static String decrypt(String data) {
        if (data == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(cipher.doFinal(DatatypeConverter.parseBase64Binary(data)), Boot.getCharset());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 得到公钥
     *
     * @param key 密钥字符串（经过base64编码）
     * @throws Exception
     */
    private static PublicKey getPublicKey(String key) {
        byte[] keyBytes;
        PublicKey publicKey = null;
        try {
            keyBytes = DatatypeConverter.parseBase64Binary(key);

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            return null;
        }
        return publicKey;
    }

    /**
     * 得到私钥
     *
     * @param key 密钥字符串（经过base64编码）
     * @throws Exception
     */
    private static PrivateKey getPrivateKey(String key) {
        byte[] keyBytes;
        PrivateKey privateKey = null;
        try {
            keyBytes = DatatypeConverter.parseBase64Binary(key);

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            return null;
        }
        return privateKey;
    }

    /**
     * 得到密钥字符串（经过base64编码）
     *
     * @return
     */
    private static String getKeyString(Key key) throws Exception {
        byte[] keyBytes = key.getEncoded();
        return DatatypeConverter.printBase64Binary(keyBytes);
    }

    public static void main(String[] args) throws Exception {

        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        // 密钥位数
        keyPairGen.initialize(1024);
        // 密钥对
        KeyPair keyPair = keyPairGen.generateKeyPair();
        // 公钥
        PublicKey publicKey = keyPair.getPublic();
        // 私钥
        PrivateKey privateKey = keyPair.getPrivate();

        String publicKeyString = getKeyString(publicKey);
        System.out.println("public:\n" + publicKeyString);

        String privateKeyString = getKeyString(privateKey);
        System.out.println("private:\n" + privateKeyString);

        BaseRsa.init(publicKeyString, privateKeyString);
        String test = "1231232123_2013-01-01 00:00:00";

        String t = BaseRsa.encrypt(test);
        System.out.println(t);

        String a = BaseRsa.decrypt(t);
        System.out.println(a);
    }

}
