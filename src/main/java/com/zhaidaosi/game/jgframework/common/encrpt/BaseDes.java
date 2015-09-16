package com.zhaidaosi.game.jgframework.common.encrpt;

import com.zhaidaosi.game.jgframework.Boot;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.SecureRandom;


public class BaseDes {


    private final static String DES = "DES";
    private static byte[] keyBytes = "1234567890!@#$qweasd".getBytes();

    public static void setDesKey(String key) {
        keyBytes = key.getBytes();
    }

    /**
     * Description 根据键值进行加密
     * @param data
     * @return
     */
    public static String encrypt(String data) {
        byte[] bt;
        try {
            bt = encrypt(data.getBytes(Boot.getCharset()), keyBytes);
        } catch (Exception e) {
            return null;
        }
        return DatatypeConverter.printBase64Binary(bt);
    }

    /**
     * Description 根据键值进行解密
     * @param data
     * @return
     */
    public static String decrypt(String data) {
        if (data == null)
            return null;
        byte[] buf;
        byte[] bt;
        try {
            buf = DatatypeConverter.parseBase64Binary(data);
            bt = decrypt(buf, keyBytes);
        } catch (Exception e) {
//			e.printStackTrace();
            return null;
        }
        return new String(bt, Boot.getCharset());
    }

    /**
     * Description 根据键值进行加密
     * @param data
     * @param key 加密键byte数组
     * @return
     * @throws Exception
     */
    private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        // 生成一个可信任的随机数源
        SecureRandom sr = new SecureRandom();

        // 从原始密钥数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);

        // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey secretKey = keyFactory.generateSecret(dks);

        // Cipher对象实际完成加密操作
        Cipher cipher = Cipher.getInstance(DES);

        // 用密钥初始化Cipher对象
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, sr);

        return cipher.doFinal(data);
    }

    /**
     * Description 根据键值进行解密
     * @param data
     * @param key 加密键byte数组
     * @return
     * @throws Exception
     */
    private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        // 生成一个可信任的随机数源
        SecureRandom sr = new SecureRandom();

        // 从原始密钥数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);

        // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey secretKey = keyFactory.generateSecret(dks);

        // Cipher对象实际完成解密操作
        Cipher cipher = Cipher.getInstance(DES);

        // 用密钥初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, secretKey, sr);

        return cipher.doFinal(data);
    }
}