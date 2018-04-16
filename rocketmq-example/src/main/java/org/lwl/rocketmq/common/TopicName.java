package org.lwl.rocketmq.common;

/**
 * @author thinking_fioa
 * @createTime 2018/4/10
 * @description
 */


public enum TopicName {
    TOPIC_TEST("topic_test"),
    BATCH_TEST("batch_test");

    private String topicName;

    TopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getTopicName() {
        return topicName;
    }
}
