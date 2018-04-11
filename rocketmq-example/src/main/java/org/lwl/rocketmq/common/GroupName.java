package org.lwl.rocketmq.common;

/**
 * @author thinking_fioa
 * @createTime 2018/4/10
 * @description
 */


public enum GroupName {
    QUICK_START_CONSUMER("quick_start_consumer"),
    QUICK_START_PRODUCER("quick_start_producer");

    private String groupName;

    GroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }
}
