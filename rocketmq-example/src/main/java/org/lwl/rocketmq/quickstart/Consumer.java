package org.lwl.rocketmq.quickstart;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.lwl.rocketmq.common.GroupName;
import org.lwl.rocketmq.common.RocketmqConfig;
import org.lwl.rocketmq.common.TopicName;

import java.util.List;

/**
 * @author thinking_fioa
 * @createTime 2018/4/10
 * @description
 */


public class Consumer {
    public static void main(String [] args) throws InterruptedException, MQClientException {
        // 特定的组，创建一个消费者
        DefaultMQPushConsumer pushConsumer = new DefaultMQPushConsumer(GroupName.QUICK_START_CONSUMER.getGroupName());

        pushConsumer.setNamesrvAddr(RocketmqConfig.getIpAndPort());

        pushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

        //订阅Topic
        pushConsumer.subscribe(TopicName.TOPIC_TEST.getTopicName(), "*");

        // 注册回调函数，MessageListennerConcurrently处理无序消息
        pushConsumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs
                    , ConsumeConcurrentlyContext context) {
                System.out.printf("[Consumer]- %s Receive New Messages: %s %n", Thread.currentThread().getName(), msgs);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        //start
        pushConsumer.start();

        System.out.printf("[Consumer]- started %n");
    }
}
