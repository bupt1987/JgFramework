package com.zhaidaosi.game.jgframework.common;

import java.util.Random;

public class BaseGeneratePassword {

    private static char arrayString[] = new char[62]; // 候选字符数组

    static {
        int j = 0;
        for (int i = 48; i <= 57; i++) { // 0-9
            arrayString[j] = (char) i;
            j++;
        }
        for (int i = 65; i <= 90; i++) { // A-Z
            arrayString[j] = (char) i;
            j++;
        }
        for (int i = 97; i <= 122; i++) { // a-z
            arrayString[j] = (char) i;
            j++;
        }
    }

    /**
     * 生成密码
     * @param intPassLength 密码长度
     * @return String
     */
    public static String doGenerate(final int intPassLength) {
        int intTemp;
        String strPassword = "";
        Random rand = new Random();
        for (int i = 0; i < intPassLength; i++) {
            intTemp = rand.nextInt(62);
            strPassword += arrayString[intTemp];
        }
        return strPassword;
    }

}
