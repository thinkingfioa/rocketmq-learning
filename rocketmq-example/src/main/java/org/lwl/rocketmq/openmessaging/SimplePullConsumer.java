package org.lwl.rocketmq.openmessaging;

import io.openmessaging.*;
import io.openmessaging.rocketmq.domain.NonStandardKeys;
import org.lwl.rocketmq.common.TopicName;

/**
 * @author thinking_fioa
 * @createTime 2018/4/23
 * @description
 */


public class SimplePullConsumer {
    public static void main(String [] args) {
        final MessagingAccessPoint messagingAccessPoint = MessagingAccessPointFactory
                .getMessagingAccessPoint("openmessaging:rocketmq://127.0.0.1:9876/namespace");

        final PullConsumer consumer = messagingAccessPoint.createPullConsumer(TopicName.OPEN_MESSAGING.getTopicName(),
                OMS.newKeyValue().put(NonStandardKeys.CONSUMER_GROUP, "OMS_CONSUMER"));

        messagingAccessPoint.startup();
        System.out.printf("MessagingAccessPoint startup OK%n");

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                consumer.shutdown();
                messagingAccessPoint.shutdown();
            }
        }));

        consumer.startup();
        System.out.printf("Consumer startup OK%n");

        while(true) {
            Message message = consumer.poll();
            if(null != message) {
                String msgId = message.headers().getString(MessageHeader.MESSAGE_ID);
                System.out.printf("Received one message: %s%n", message);
                consumer.ack(msgId);
            }
        }
    }
}
