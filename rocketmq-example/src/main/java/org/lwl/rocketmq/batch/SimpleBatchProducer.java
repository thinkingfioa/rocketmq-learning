package org.lwl.rocketmq.batch;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.lwl.rocketmq.common.GroupName;
import org.lwl.rocketmq.common.RocketmqConfig;
import org.lwl.rocketmq.common.TagName;
import org.lwl.rocketmq.common.TopicName;

import java.util.ArrayList;
import java.util.List;

/**
 * @author thinking_fioa，一次发送多条消息，将多条消息封装进入List，然后发送
 * @createTime 2018/4/14
 * @description
 */

public class SimpleBatchProducer {

    public static void main(String [] args ) throws Exception{
        DefaultMQProducer producer = new DefaultMQProducer(GroupName.BATCH_PRODUCER_GROUP_NAME.getGroupName());
        producer.setNamesrvAddr(RocketmqConfig.getIpAndPort());

        producer.start();

        // 如果单次发送消息大小 < 1MiB，可以使用批发送。使用List封装消息，一次性发出
        // 批发送消息时，请将一起发送的Message的topic设置为相同
        String topic = TopicName.BATCH_TEST.getTopicName();

        List<Message> messageList = new ArrayList<Message>();
        messageList.add(new Message(topic, TagName.TAG_TEST.getTagName(), "OrderID001", "Hello world 1".getBytes()));
        messageList.add(new Message(topic, TagName.TAG_TEST.getTagName(), "OrderID002", "Hello world 2".getBytes()));
        messageList.add(new Message(topic, TagName.TAG_TEST.getTagName(), "OrderID003", "Hello world 323".getBytes()));

        producer.send(messageList);
        System.out.println("send Success.");
    }
}
