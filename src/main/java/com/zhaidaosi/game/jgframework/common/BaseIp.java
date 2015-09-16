package com.zhaidaosi.game.jgframework.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseIp {

    private static final Logger log = LoggerFactory.getLogger(BaseIp.class);

    public static Long[] stringToIp(String ips) {
        Long[] longArr;
        String[] ipArr = ips.split("-");
        if (ipArr.length == 2) {
            longArr = new Long[2];
            longArr[0] = ipToLong(ipArr[0]);
            longArr[1] = ipToLong(ipArr[1]);
        } else {
            longArr = new Long[1];
            longArr[0] = ipToLong(ipArr[0]);
        }
        return longArr;
    }

    public static long ipToLong(String ip) {
        String[] ipArr = ip.split("\\.");
        if (ipArr.length != 4) {
            log.error("ip格式错误：" + ip);
            return 0L;
        }
        long s = 0;
        for (int i = 0; i < ipArr.length; i++) {
            s += (long) (Long.parseLong(ipArr[i]) * Math.pow(1000, 3 - i));
        }
        return s;
    }

    public static boolean checkIp(String ip, Long[] ipArr) {
        long ipLong = ipToLong(ip);
        if (ipArr.length == 1) {
            if (ipLong == ipArr[0]) {
                return true;
            }
        } else {
            if (ipArr[0] <= ipLong && ipLong <= ipArr[1]) {
                return true;
            }
        }
        return false;
    }

}
