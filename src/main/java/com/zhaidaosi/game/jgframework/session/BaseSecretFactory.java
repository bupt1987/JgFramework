package com.zhaidaosi.game.jgframework.session;

import com.zhaidaosi.game.jgframework.common.encrpt.BaseRsa;
import com.zhaidaosi.game.jgframework.common.excption.BaseException;

public class BaseSecretFactory implements IBaseSecretFactory {

    @Override
    public String createSecret(int userId) throws BaseException {
        String input = userId + "_" + System.currentTimeMillis();
        String secret = BaseRsa.encrypt(input);
        if (secret == null) {
            throw new BaseException("秘钥生成失败", 100);
        }
        return secret;
    }

    @Override
    public int checkSecret(String secret) throws Exception {
        secret = BaseRsa.decrypt(secret);
        if (secret == null) {
            throw new BaseException("非法秘钥", 101);
        }
        String[] arr = secret.split("_");
        if (arr.length != 2) {
            throw new BaseException("非法秘钥", 101);
        }
        int userId = Integer.valueOf(arr[0]);
        if (userId <= 0) {
            throw new BaseException("非法秘钥", 101);
        }
        //有效期1分钟
        if (Long.parseLong(arr[1]) - System.currentTimeMillis() > 60000) {
            throw new BaseException("秘钥已失效", 102);
        }
        return userId;
    }

}
