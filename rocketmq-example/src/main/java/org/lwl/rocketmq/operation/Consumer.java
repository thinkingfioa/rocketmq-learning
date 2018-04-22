package org.lwl.rocketmq.operation;


import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.commons.cli.*;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.lwl.rocketmq.common.RocketmqConfig;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author thinking_fioa
 * @createTime 2018/4/22
 * @description
 */


public class Consumer {
    public static void main(String [] args) throws MQClientException {
        CommandLine commandLine = buildCommandline(args);
        if(null != commandLine) {
            String group = commandLine.getOptionValue('g');
            String topic = commandLine.getOptionValue('t');
            String tags = commandLine.getOptionValue('a');
            final String returnFailedHalf = commandLine.getOptionValue('f');

            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(group);
            consumer.setNamesrvAddr(RocketmqConfig.getIpAndPort());
            consumer.setInstanceName(Long.toString(System.currentTimeMillis()));

            consumer.subscribe(topic, tags);

            consumer.registerMessageListener(new MessageListenerConcurrently() {
                AtomicLong consumeTimes = new AtomicLong(0);

                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                    long currentTimes = this.consumeTimes.incrementAndGet();
                    System.out.printf("%-8d %s%n", currentTimes, msgs);
                    if(Boolean.parseBoolean(returnFailedHalf)) {
                        if((currentTimes % 2) == 0) {
                            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                        }
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });

            consumer.start();
            System.out.printf("Consumer Started.%n");
        }
    }

    public static CommandLine buildCommandline(String [] args) {
        final Options options = new Options();
        // 1. Commons CLI 定义
        // 第一个参数: 参数的简单形式
        // 第二个参数: 参数的复杂形式
        // 第三个参数: 是否需要额外的输入
        // 第四个参数: 对参数的描述信息
        Option opt = new Option("h", "help", false, "Print help");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("g", "consumerGruop", true, "Consumer Gruop Name");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("t", "topic", true, "Topic Name");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("a", "tags", true, "Tags");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("f", "returnFailedHalf", true, "return failed result, for half message");
        opt.setRequired(true);
        options.addOption(opt);

        // 2. Commons CLI 解析
        PosixParser parser = new PosixParser();
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(110);

        CommandLine commandLine = null;
        try {
            // 3. Commons CLI 询问交互
            commandLine = parser.parse(options, args);
            if(commandLine.hasOption("h")) {
                hf.printHelp("consumer", options, true);
            }
        } catch (ParseException e) {
            hf.printHelp("consumer", options, true);
            return null;
        }

        return commandLine;
    }
}
