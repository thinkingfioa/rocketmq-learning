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

        consumer.subscribe(TopicName.TOPIC_TEST.getTopicName(), "TagA||TagB||TabC");

        consumer.registerMessageListener(new MessageListenerOrderly() {
            AtomicLong consumeTimes = new AtomicLong(0);

            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
                context.setAutoCommit(false);
                System.out.printf("Receive New Messages: %s %d%n", Thread.currentThread().getName(), msgs.size());
                System.out.printf("+++++++++++++++++%n");
                for(int i = 0; i<msgs.size(); i++) {
                    System.out.printf("%s %n", msgs.get(i).getKeys().toString());
                }
                System.out.printf("-----------------%n");
                this.consumeTimes.incrementAndGet();
                if((this.consumeTimes.get() % 2) ==0){
                    return ConsumeOrderlyStatus.SUCCESS;
                } else if((this.consumeTimes.get() % 3) == 0) {
                    return ConsumeOrderlyStatus.ROLLBACK;
                } else if((this.consumeTimes.get() % 4) == 0){
                    return ConsumeOrderlyStatus.COMMIT;
                } else if((this.consumeTimes.get() % 5) == 0) {
                    context.setSuspendCurrentQueueTimeMillis(200);
                    return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                }

                return ConsumeOrderlyStatus.SUCCESS;
            }
        });

        consumer.start();
        System.out.printf("Consumer Started.%n");
    }
}
