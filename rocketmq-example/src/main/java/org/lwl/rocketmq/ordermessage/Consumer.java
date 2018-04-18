package org.lwl.rocketmq.ordermessage;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.lwl.rocketmq.common.GroupName;
import org.lwl.rocketmq.common.RocketmqConfig;
import org.lwl.rocketmq.common.TopicName;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author thinking_fioa
 * @createTime 2018/4/17
 * @description
 */


public class Consumer {

    public static void main(String [] args) throws MQClientException {
        final DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(GroupName.ORDER_MESSAGE.getGroupName());

        consumer.setNamesrvAddr(RocketmqConfig.getIpAndPort());
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

        consumer.subscribe(TopicName.TOPIC_TEST.getTopicName(), "TagA || TagB || TagC");

        consumer.registerMessageListener(new MessageListenerOrderly() {
            AtomicLong consumeTimes = new AtomicLong(0);

            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
                context.setAutoCommit(true);
                System.out.printf("Receive New Messages: %s size: %d%n", Thread.currentThread().getName(), msgs.size());

                for(MessageExt msg : msgs) {
                    System.out.println(msg+", content: "+new String(msg.getBody()));
                }
                this.consumeTimes.incrementAndGet();

                return ConsumeOrderlyStatus.SUCCESS;
            }
        });

        consumer.start();
        System.out.printf("Consumer Started.%n");
    }
}
