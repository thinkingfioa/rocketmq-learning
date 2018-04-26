package org.lwl.rocketmq.broadcast;

import org.apache.rocketmq.client.exception.MQClientException;

/**
 * @author thinking_fioa
 * @createTime 2018/4/26
 * @description
 */


public class ProducerWrapper {
    public static void main(String [] args) throws MQClientException, InterruptedException {
        org.lwl.rocketmq.quickstart.Producer.main(args);
    }
}
