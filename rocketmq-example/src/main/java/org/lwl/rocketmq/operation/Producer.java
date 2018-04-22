package org.lwl.rocketmq.operation;

import org.apache.commons.cli.*;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.lwl.rocketmq.common.RocketmqConfig;

/**
 * @author thinking_fioa
 * @createTime 2018/4/22
 * @description Producer利用org.apache.commons包下的CommandLine来实现命令行提示
 */


public class Producer {

    public static void main(String[] args) throws MQClientException, InterruptedException {

        CommandLine commandLine = buildCommandline(args);

        if (null != commandLine) {
            String gruop = commandLine.getOptionValue('g');
            String topic = commandLine.getOptionValue('t');
            String tags = commandLine.getOptionValue('a');
            String keys = commandLine.getOptionValue('k');
            String msgCount = commandLine.getOptionValue('c');

            DefaultMQProducer producer = new DefaultMQProducer(gruop);
            producer.setNamesrvAddr(RocketmqConfig.getIpAndPort());

            producer.setInstanceName(Long.toString(System.currentTimeMillis()));

            producer.start();

            for (int i = 0; i < Integer.parseInt(msgCount); i++) {
                try {
                    Message msg = new Message(
                            topic,
                            tags,
                            keys,
                            ("Hello RocketMQ" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
                    SendResult sendResult = producer.send(msg);
                    System.out.printf("%-8d %s%n", i, sendResult);
                } catch (Exception e) {
                    e.printStackTrace();
                    Thread.sleep(1000);
                }
            }

        }
    }

    /**
     * 使用Commons CLI命令行。解析命令行主要有三个状态: 定义，解释，询问交互。
     * @param args
     * @return
     */
    public static CommandLine buildCommandline(String[] args) {
        final Options options = new Options();
        // 1. Commons CLI 定义
        // 第一个参数: 参数的简单形式
        // 第二个参数: 参数的复杂形式
        // 第三个参数: 是否需要额外的输入
        // 第四个参数: 对参数的描述信息
        Option opt = new Option("h", "help", false, "Print help");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("g", "producerGruop", true, "Producer Gruop Name");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("t", "topic", true, "Topic Name");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("a", "tags", true, "Tags Name");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("k", "keys", true, "Keys Name");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("c", "msgCount", true, "Message Count");
        opt.setRequired(true);
        options.addOption(opt);

        // 2. Commons CLI 解析
        PosixParser parser = new PosixParser();
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(110);
        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
            // 3. Commons CLI 询问交互
            if (commandLine.hasOption('h')) {
                hf.printHelp("producer", options, true);
                return null;
            }
        } catch (ParseException e) {
            hf.printHelp("producer", options, true);
            return null;
        }
        return commandLine;
    }
}