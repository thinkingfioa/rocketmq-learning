package org.lwl.rocketmq.quickstart;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.lwl.rocketmq.common.GroupName;
import org.lwl.rocketmq.common.RocketmqConfig;
import org.lwl.rocketmq.common.TagName;
import org.lwl.rocketmq.common.TopicName;

/**
 * @author thinking_fioa
 * @createTime 2018/4/10
 * @description
 */


public class Producer {
    public static void main(String [] args) throws MQClientException, InterruptedException {
        // 特定的组，创建一个消费者
        DefaultMQProducer producer = new DefaultMQProducer(GroupName.QUICK_START.getGroupName());
        producer.setNamesrvAddr(RocketmqConfig.getIpAndPort());

        producer.start();


        // produce message
        for(int i = 0; i<1000; i++) {
            try {
                String content = "thinking_fioa"+i;
                Message msg = new Message(TopicName.TOPIC_TEST.getTopicName(),
                        TagName.TAG_TEST.getTagName(), content.getBytes(RemotingHelper.DEFAULT_CHARSET));
                SendResult sendResult = producer.send(msg);

                System.out.printf("[Producer]- %s%n",sendResult);
            } catch (Exception e){
                e.printStackTrace();
                Thread.sleep(1000);
            }
        }

        //shutdown
        producer.shutdown();

    }
}
