package org.lwl.rocketmq.ordermessage;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.lwl.rocketmq.common.GroupName;
import org.lwl.rocketmq.common.RocketmqConfig;
import org.lwl.rocketmq.common.TopicName;

import java.util.List;

/**
 * @author thinking_fioa
 * @createTime 2018/4/17
 * @description
 */


public class Producer {

    public static void main(String [] args) throws Exception{
        DefaultMQProducer producer = new DefaultMQProducer(GroupName.ORDER_MESSAGE.getGroupName());
        producer.setNamesrvAddr(RocketmqConfig.getIpAndPort());
        producer.start();

        String[] tags = new String [] {"TagA", "TagB", "TagC", "TagD", "TagE", "TagF"};

        for(int i=0; i<5;i++) {
            int orderId = i % 10;
            Message msg = new Message(TopicName.TOPIC_TEST.getTopicName(), tags[i%tags.length], "KEY"+i,
                    ("Hello ThinkingFioa"+i).getBytes(RemotingHelper.DEFAULT_CHARSET));

            // 订单的Producer发送消息需要注册回调函数
            SendResult sendResult = producer.send(msg, new MessageQueueSelector() {
                @Override
                public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                    Integer id = (Integer) arg;
                    int index = id % mqs.size();
                    return mqs.get(index);
                }
            }, 0);

            System.out.printf("%s%n", sendResult);
        }

        producer.shutdown();
    }
}
