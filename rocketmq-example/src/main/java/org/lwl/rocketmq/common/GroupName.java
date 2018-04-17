package org.lwl.rocketmq.common;

/**
 * @author thinking_fioa
 * @createTime 2018/4/10
 * @description
 */


public enum GroupName {
    QUICK_START_CONSUMER("quick_start_consumer"),
    QUICK_START_PRODUCER("quick_start_producer"),
    BATCH_PRODUCER_GROUP_NAME("batch_producer_group_name"),
    ORDER_MESSAGE("order_message");

    private String groupName;

    GroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }
}
