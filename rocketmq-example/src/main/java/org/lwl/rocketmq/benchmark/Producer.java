package org.lwl.rocketmq.benchmark;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.lwl.rocketmq.common.CustomThreadFactory;
import org.lwl.rocketmq.common.TopicName;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author thinking_fioa
 * @createTime 2018/4/27
 * @description
 */


public class Producer {
    private static final ScheduledExecutorService scheduleProducer
            = new ScheduledThreadPoolExecutor(3, new CustomThreadFactory("tBmkScheduledProducer", true));

    private static final ExecutorService sendThreadPool = Executors.newFixedThreadPool(10, new CustomThreadFactory("tBmkSendProducer", true) );
    public static void main(String [] args) throws MQClientException {
        final String topic = TopicName.BENCHMARK.getTopicName();
        final StatsBenchmarkProducer statsBenchmark = new StatsBenchmarkProducer();

        final LinkedList<Long[]> snapshotList = new LinkedList<Long[]>();
        // 1秒获取一次数据
        scheduleProducer.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                snapshotList.addLast(statsBenchmark.createSnapshot());
                if(snapshotList.size() > 10) {
                    snapshotList.removeFirst();
                }
            }
        },1000, 1000, TimeUnit.MILLISECONDS);

        scheduleProducer.scheduleAtFixedRate(new Runnable() {
            private void printStats() {
                if(snapshotList.size() >=10) {
                    Long [] begin = snapshotList.getFirst();
                    Long [] end = snapshotList.getLast();

                    final long sendTps = (long) (((end[3] - begin[3]) / (double) (end[0] - begin[0])) * 1000L);
                    final double averageRT = (end[5] - begin[5]) / (double) (end[3] - begin[3]);

                    System.out.printf("Send TPS: %d Max RT: %d Average RT: %7.3f Send Failed: %d Response Failed: %d%n",
                            sendTps, statsBenchmark.getSendMessageMaxRT().get(), averageRT, end[2], end[4]);
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

        final DefaultMQProducer producer = new DefaultMQProducer();
        producer.setInstanceName(Long.toString(System.currentTimeMillis()));

        producer.setCompressMsgBodyOverHowmuch(Integer.MAX_VALUE);
        producer.start();

        for(int i =0;i <10;i++) {
            sendThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        Message msg =null;

                        try {
                            msg = buildMessage(10, topic);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        final long beginTimestamp = System.currentTimeMillis();
                        try {
                            producer.send(msg);
                            statsBenchmark.getSendRequestSuccessCount().incrementAndGet();
                            statsBenchmark.getReceiveResponseSuccessCount().incrementAndGet();
                            final long currentRT = System.currentTimeMillis() - beginTimestamp;
                            statsBenchmark.getSendMessageSuccessTimeTotal().addAndGet(currentRT);
                            // 更新最大值
                            compareAndSetMax(statsBenchmark.getSendMessageMaxRT(), currentRT);

                        } catch (MQClientException e) {
                            statsBenchmark.getSendRequestFailedCount().incrementAndGet();

                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException ignored) {
                            }
                        } catch (RemotingException e) {
                            statsBenchmark.getSendRequestFailedCount().incrementAndGet();
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e1) {
                            }
                        } catch (MQBrokerException e) {
                            statsBenchmark.getSendRequestFailedCount().incrementAndGet();
                        } catch (InterruptedException e) {
                            statsBenchmark.getReceiveResponseFailedCount().incrementAndGet();
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException ignored) {
                            }
                        }

                    }
                }
            });
        }
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


    private static Message buildMessage(final int messageSize, final String topic) throws UnsupportedEncodingException {
        Message msg = new Message();
        msg.setTopic(topic);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < messageSize; i += 10) {
            sb.append("hello baby");
        }

        msg.setBody(sb.toString().getBytes(RemotingHelper.DEFAULT_CHARSET));

        return msg;
    }

    static class StatsBenchmarkProducer {
        private final AtomicLong sendRequestSuccessCount = new AtomicLong(0L);

        private final AtomicLong sendRequestFailedCount = new AtomicLong(0L);

        private final AtomicLong receiveResponseSuccessCount = new AtomicLong(0L);

        private final AtomicLong receiveResponseFailedCount = new AtomicLong(0L);

        private final AtomicLong sendMessageSuccessTimeTotal = new AtomicLong(0L);

        private final AtomicLong sendMessageMaxRT = new AtomicLong(0L);

        public Long[] createSnapshot() {
            Long [] snap = new Long[] {
                    System.currentTimeMillis(),
                    this.sendRequestSuccessCount.get(),
                    this.sendRequestFailedCount.get(),
                    this.receiveResponseSuccessCount.get(),
                    this.receiveResponseFailedCount.get(),
                    this.sendMessageSuccessTimeTotal.get(),
            };

            return snap;
        }

        public AtomicLong getSendRequestSuccessCount() {
            return sendRequestSuccessCount;
        }

        public AtomicLong getSendRequestFailedCount() {
            return sendRequestFailedCount;
        }

        public AtomicLong getReceiveResponseSuccessCount() {
            return receiveResponseSuccessCount;
        }

        public AtomicLong getReceiveResponseFailedCount() {
            return receiveResponseFailedCount;
        }

        public AtomicLong getSendMessageSuccessTimeTotal() {
            return sendMessageSuccessTimeTotal;
        }

        public AtomicLong getSendMessageMaxRT() {
            return sendMessageMaxRT;
        }
    }
}
