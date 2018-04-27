package org.lwl.rocketmq.benchmark;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.rocketmq.srvutil.ServerUtil;
import org.lwl.rocketmq.common.CustomThreadFactory;
import org.lwl.rocketmq.common.TopicName;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author thinking_fioa
 * @createTime 2018/4/27
 * @description
 */


public class Consumer {
    private static final ScheduledExecutorService scheduleServer
            = new ScheduledThreadPoolExecutor(3, new CustomThreadFactory("BenchmarkThread"));

    public static void main(String [] args) {
        Options options = ServerUtil.buildCommandlineOptions(new Options());
        CommandLine commandLine = ServerUtil.parseCmdLine("benchmarkConsumer", args, buildCommandlineOptions(options), new PosixParser());
        if(null == commandLine) {
            System.exit(-1);
            return;
        }

        final String topic = commandLine.hasOption('t')?commandLine.getOptionValue('t').trim(): TopicName.BENCHMARK.getTopicName();
        final String groupProfix = commandLine.hasOption('g') ? commandLine.getOptionValue('g').trim() : "benchmark_consumer";
        final String filterType = commandLine.hasOption('f') ? commandLine.getOptionValue('f').trim() : null;
        final String expression = commandLine.hasOption('e') ? commandLine.getOptionValue('e').trim() : null;
        String groupName = groupProfix + "_" + Long.toString(System.currentTimeMillis() % 100);

        System.out.printf("topic: %s, groupName: %s, filterType: %s, expression: %s%n", topic, groupName, filterType, expression);

        final StatsBenchmarkConsumer statsBenchmarkConsumer = new StatsBenchmarkConsumer();

    }

    public static Options buildCommandlineOptions(final Options options) {
        Option opt = new Option("t", "topic", true, "Topic name, Default: benchmark");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("g", "group", true, "Consumer group name, Default: benchmark_consumer");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("f", "filterType", true, "TAG, SQL92");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("e", "expression", true, "filter expression content file");
        opt.setRequired(false);
        options.addOption(opt);

        return options;
    }

    private static class StatsBenchmarkConsumer {
        // 接收消息总量
        private final AtomicLong receiveMessageTotalCount = new AtomicLong(0L);

        private final AtomicLong born2ConsumerTotalRT = new AtomicLong(0L);

        private final AtomicLong store2ConsumerTotalRT= new AtomicLong(0L);

        private final AtomicLong born2ConsumerMaxRT = new AtomicLong(0L);

        private final AtomicLong store2ConsumerMaxRT = new AtomicLong(0L);

        public Long[] createSnapshot() {
            Long[] snap = new Long[] {
                    System.currentTimeMillis(),
                    this.receiveMessageTotalCount.get(),
                    this.born2ConsumerTotalRT.get(),
                    this.store2ConsumerTotalRT.get(),
                    this.born2ConsumerMaxRT.get(),
                    this.store2ConsumerMaxRT.get(),
            };

            return snap;
        }

        public AtomicLong getReceiveMessageTotalCount() {
            return receiveMessageTotalCount;
        }

        public AtomicLong getBorn2ConsumerTotalRT() {
            return born2ConsumerTotalRT;
        }

        public AtomicLong getStore2ConsumerTotalRT() {
            return store2ConsumerTotalRT;
        }

        public AtomicLong getBorn2ConsumerMaxRT() {
            return born2ConsumerMaxRT;
        }

        public AtomicLong getStore2ConsumerMaxRT() {
            return store2ConsumerMaxRT;
        }
    }
}
