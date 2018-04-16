package org.lwl.rocketmq.batch;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.common.message.Message;
import org.lwl.rocketmq.common.GroupName;
import org.lwl.rocketmq.common.RocketmqConfig;
import org.lwl.rocketmq.common.TagName;
import org.lwl.rocketmq.common.TopicName;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author thinking_fioa
 * @createTime 2018/4/14
 * @description
 */


public class SplitBatchProducer {
    public static void main(String [] args) throws Exception{
        DefaultMQProducer producer = new DefaultMQProducer(GroupName.BATCH_PRODUCER_GROUP_NAME.getGroupName());
        producer.setNamesrvAddr(RocketmqConfig.getIpAndPort());

        producer.start();

        // 发送多条消息
        List<Message> messageList = new ArrayList<Message>();

        for(int i =0; i< 100 * 1000; i++) {
            messageList.add(new Message(TopicName.BATCH_TEST.getTopicName(), TagName.TAG_TEST.getTagName(),
            "OrderId"+i, ("Hello world "+i).getBytes()));
        }

        // 将多条消息，拆分进入小集合中
        ListSplitter splitter = new ListSplitter(messageList);
        while(splitter.hasNext()) {
            List<Message> subList = splitter.next();
            producer.send(subList);
        }

    }

    static class ListSplitter implements Iterator<List<Message>> {
        // 单次发送最大字节数
        private int sizeLimit = 1000 * 1000;
        private final List<Message> messages;
        private int currIndex;

        public ListSplitter(List<Message> messages) {
            this.messages = messages;
            currIndex = 0;
        }

        @Override
        public boolean hasNext() {
            return currIndex < messages.size();
        }

        @Override
        public List<Message> next() {
            int nextIndex = currIndex;
            int currSizeLimit = 0;
            for(; nextIndex < messages.size(); nextIndex++) {
                Message oneMessage = messages.get(nextIndex);
                int tempSize = oneMessage.getTopic().length() + oneMessage.getBody().length;
                Map<String, String> properties = oneMessage.getProperties();
                for(Map.Entry<String, String> entry: properties.entrySet()) {
                    tempSize += entry.getKey().length() + entry.getValue().length();
                }
                if(tempSize > sizeLimit) {
                    if(nextIndex - currSizeLimit ==0) {
                        nextIndex ++;
                    }
                    break;
                }

                if(tempSize + currSizeLimit > sizeLimit) {
                    break;
                } else {
                    currSizeLimit += tempSize;
                }
            }

            List<Message> subList = messages.subList(currIndex, nextIndex);
            currIndex = nextIndex;
            return subList;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not allowed to remove");
        }
    }
}
