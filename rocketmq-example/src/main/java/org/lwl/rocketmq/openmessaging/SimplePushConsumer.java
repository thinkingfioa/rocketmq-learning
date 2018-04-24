package org.lwl.rocketmq.openmessaging;

import io.openmessaging.*;
import io.openmessaging.rocketmq.domain.NonStandardKeys;
import org.lwl.rocketmq.common.TopicName;

/**
 * @author thinking_fioa
 * @createTime 2018/4/23
 * @description
 */


public class SimplePushConsumer {
    public static void main(String [] args) {
        final MessagingAccessPoint messagingAccessPoint = MessagingAccessPointFactory
                .getMessagingAccessPoint("openmessaging:rocketmq://127.0.0.1:9876/namespace");

        final PushConsumer consumer = messagingAccessPoint
                .createPushConsumer(OMS.newKeyValue().put(NonStandardKeys.CONSUMER_GROUP, "OMS_CONSUMER"));

        messagingAccessPoint.startup();
        System.out.printf("MessagingAccessPoint startup OK%n");

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                consumer.shutdown();
                messagingAccessPoint.shutdown();
            }
        }));

        consumer.attachQueue(TopicName.OPEN_MESSAGING.getTopicName(), new MessageListener() {
            @Override
            public void onMessage(Message message, ReceivedMessageContext receivedMessageContext) {
                System.out.printf("Received one message: %s%n", message.headers().getString(MessageHeader.MESSAGE_ID));
                receivedMessageContext.ack();
            }
        });

        consumer.startup();
        System.out.printf("Consumer startup OK%n");
    }
}
