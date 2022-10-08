package com.bloom.util;

import com.bloom.Template.isOK;
import com.bloom.util.Functions;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import ysoserial.payloads.templates.*;


import java.util.concurrent.TimeUnit;


public class Cache {
    private static ExpiringMap<String, byte[]> map = ExpiringMap.builder()
            .maxSize(1000)
            .expiration(30, TimeUnit.SECONDS)
            .variableExpiration()
            .expirationPolicy(ExpirationPolicy.CREATED)
            .build();

    static{
        try {
            //过期时间100年，永不过期的简单方法
            map.put("SpringInterceptorMemShell", Functions.getClassBytes(SpringInterceptorMemShell.class), 365 * 100, TimeUnit.DAYS);
            map.put("TomcatFilterMemShellFromJMX", Functions.getClassBytes(TomcatFilterMemShellFromJMX.class), 365 * 100, TimeUnit.DAYS);
            map.put("TomcatFilterMemShellFromThread", Functions.getClassBytes(TomcatFilterMemShellFromThread.class), 365 * 100, TimeUnit.DAYS);
            map.put("TomcatListenerMemShellFromJMX", Functions.getClassBytes(TomcatListenerMemShellFromJMX.class), 365 * 100, TimeUnit.DAYS);
            map.put("TomcatListenerMemShellFromThread", Functions.getClassBytes(TomcatListenerMemShellFromThread.class), 365 * 100, TimeUnit.DAYS);
            map.put("TomcatListenerNeoRegFromThread", Functions.getClassBytes(TomcatListenerNeoRegFromThread.class), 365 * 100, TimeUnit.DAYS);
            map.put("TomcatServletMemShellFromJMX", Functions.getClassBytes(TomcatServletMemShellFromJMX.class), 365 * 100, TimeUnit.DAYS);
            map.put("TomcatServletMemShellFromThread", Functions.getClassBytes(TomcatServletMemShellFromThread.class), 365 * 100, TimeUnit.DAYS);
            map.put("TomcatCmdEcho", Functions.getClassBytes(TomcatCmdEcho.class), 365 * 100, TimeUnit.DAYS);
            map.put("isOK", Functions.getClassBytes(isOK.class), 365 * 100, TimeUnit.DAYS);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] get(String key){
        return map.get(key);
    }

    public static void set(String key, byte[] bytes){
        map.put(key, bytes);
    }

    public static boolean contains(String key){
        return map.containsKey(key);
    }

    public static void remove(String key){
        map.remove(key);
    }
}
