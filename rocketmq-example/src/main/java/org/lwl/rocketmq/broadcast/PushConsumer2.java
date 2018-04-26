package org.lwl.rocketmq.broadcast;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.lwl.rocketmq.common.GroupName;
import org.lwl.rocketmq.common.RocketmqConfig;
import org.lwl.rocketmq.common.TagName;
import org.lwl.rocketmq.common.TopicName;

import java.util.List;

/**
 * @author thinking_fioa
 * @createTime 2018/4/26
 * @description 广播消息，PushConsumer2和PUshConsumer都会收到消息
 */


public class PushConsumer2 {
    public static void main(String [] args) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(GroupName.BROADCAST.getGroupName());
        consumer.setNamesrvAddr(RocketmqConfig.getIpAndPort());

        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

        consumer.setMessageModel(MessageModel.BROADCASTING);

        consumer.subscribe(TopicName.TOPIC_TEST.getTopicName(), TagName.TAG_TEST.getTagName());

        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                System.out.printf("Receive New Message Count: %s%n", msgs.size());
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        consumer.start();
        System.out.printf("Broadcast Consumer Started%n");
    }
}
