package org.lwl.rocketmq.benchmark;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.filter.ExpressionType;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.srvutil.ServerUtil;
import org.lwl.rocketmq.common.CustomThreadFactory;
import org.lwl.rocketmq.common.TopicName;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author thinking_fioa
 * @createTime 2018/4/27
 * @description
 */

public class Consumer {
    private static final ScheduledExecutorService scheduleConsumer
            = new ScheduledThreadPoolExecutor(3, new CustomThreadFactory("tBenchmarkConsumer", true));

    public static void main(String [] args) throws MQClientException, IOException {

        final String topic = TopicName.BENCHMARK.getTopicName();
        final String groupProfix = "benchmark_consumer";
        String groupName = groupProfix + "_" + Long.toString(System.currentTimeMillis() % 100);

        final StatsBenchmarkConsumer statsBenchmarkConsumer = new StatsBenchmarkConsumer();

        final LinkedList<Long []> snapshotList = new LinkedList<Long []>();
        // 1秒获取一次数据
        scheduleConsumer.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                snapshotList.addLast(statsBenchmarkConsumer.createSnapshot());

                if(snapshotList.size() > 10) {
                    snapshotList.removeFirst();
                }
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);

        scheduleConsumer.scheduleAtFixedRate(new Runnable() {
            private void printStats() {
                if(snapshotList.size() >=10) {
                    Long [] begin = snapshotList.getFirst();
                    Long [] end = snapshotList.getLast();

                    final long consumeTps =
                            (long) (((end[1] - begin[1]) / (double) (end[0] - begin[0])) * 1000L);
                    final double averageB2CRT = (end[2] - begin[2]) / (double) (end[1] - begin[1]);
                    final double averageS2CRT = (end[3] - begin[3]) / (double) (end[1] - begin[1]);

                    System.out.printf("Consume TPS: %d Average(B2C) RT: %7.3f Average(S2C) RT: %7.3f MAX(B2C) RT: %d MAX(S2C) RT: %d%n",
                            consumeTps, averageB2CRT, averageS2CRT, end[4], end[5]
                    );
                }
            }
            @Override
            public void run() {
                try {
                    this.printStats();
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }, 10000, 10000, TimeUnit.MILLISECONDS);

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(groupName);
        consumer.setInstanceName(Long.toString(System.currentTimeMillis()));

//        if(ExpressionType.TAG.equals(filterType)) {
        consumer.subscribe(topic, "TagA|TagB");
//        } else if(ExpressionType.SQL92.equals(filterType)) {
//            String expr = MixAll.file2String(expression);
//            System.out.printf("Expression: %s%n", expr);
//            consumer.subscribe(topic, MessageSelector.bySql(expr));
//        } else {
//            throw new IllegalArgumentException("Not support filter type! " + filterType);
//        }

        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                MessageExt msg = msgs.get(0);
                long now = System.currentTimeMillis();

                statsBenchmarkConsumer.getReceiveMessageTotalCount().incrementAndGet();

                long born2ConsumerRT = now - msg.getBornTimestamp();
                statsBenchmarkConsumer.getBorn2ConsumerTotalRT().addAndGet(born2ConsumerRT);

                long store2ConsumerRT = now - msg.getStoreTimestamp();
                statsBenchmarkConsumer.getStore2ConsumerTotalRT().addAndGet(store2ConsumerRT);

                compareAndSetMax(statsBenchmarkConsumer.getBorn2ConsumerMaxRT(), born2ConsumerRT);

                compareAndSetMax(statsBenchmarkConsumer.getStore2ConsumerMaxRT(), store2ConsumerRT);

                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        consumer.start();
        System.out.printf("Consumer Started.%n");
    }

    /**
     * 更新最大值
     * @param target
     * @param value
     */
    public static void compareAndSetMax(final AtomicLong target, final long value) {
        long prev = target.get();
        while(value > prev) {
            boolean updated = target.compareAndSet(prev, value);
            if(updated) {
                break;
            }

            prev = target.get();
        }
    }

    private static class StatsBenchmarkConsumer {
        // 接收消息总量
        private final AtomicLong receiveMessageTotalCount = new AtomicLong(0L);
        // 消息从创建到消费总时间
        private final AtomicLong born2ConsumerTotalRT = new AtomicLong(0L);
        // 消息从存储到消费总时间
        private final AtomicLong store2ConsumerTotalRT= new AtomicLong(0L);
        // 最大值
        private final AtomicLong born2ConsumerMaxRT = new AtomicLong(0L);
        // 最大值
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
