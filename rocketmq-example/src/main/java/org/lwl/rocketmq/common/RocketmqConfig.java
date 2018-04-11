package org.lwl.rocketmq.common;

/**
 * @author thinking_fioa
 * @createTime 2018/4/10
 * @description
 */


public final class RocketmqConfig {

    private RocketmqConfig() {
        throw new IllegalAccessError("can't use constructor");
    }

    private static final int PORT = 9876;
    private static final String IP = "127.0.0.1";

    public static int getPort() {
        return PORT;
    }

    public static String getIp() {
        return IP;
    }

    /**
     * return "ip:port"
     */
    public static String getIpAndPort(){
        return IP+":"+PORT;
    }
}
