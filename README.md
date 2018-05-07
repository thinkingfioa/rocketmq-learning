# RocketMQ 源码学习
```
@author 鲁伟林
向开源致敬，向优秀前辈代码致敬。
源码地址：https://github.com/apache/rocketmq
RocketMQ学习版本：4.2.0
曾用名: Metaq
```
---

# 1.为什么要读RocketMQ源码?
随着分布式应用的需求，中间件已经成为重点研究领域。中间件可以轻松帮助实现分布式系统。RocketMQ出自于阿里集团，是众多中间件中非常优秀项目。通过源码学习，可以学到优秀的编码风格、编程技术和设计理念。

## 1.1 RocketMQ特点
- 1.支持严格的消息顺序
- 2.支持Topic与Queue两种模式
- 3.亿级消息堆积能力
- 4.比较友好的分布式特性
- 5.同时支持Push与Pull方式消费信息

## 1.2 RocketMQ服务器
RocketMQ不同于ZeroMQ，ZeroMQ是一个端到端的消息中间件。RocketMQ除了消息生产者和消息消费者外，还需要单独起一个RocketMQ服务器，充当Master节点。
![](https://img-blog.csdn.net/20160408142513136)

## 1.3 部署RocketMQ服务器
参考文档: [RocketMQ部署文档](https://rocketmq.apache.org/docs/quick-start/)

### 1.3.1 通过脚本启动
- 1.startRocketMQ.sh

```
#! /bin/bash

./rocketmq/distribution/target/apache-rocketmq/bin/mqnamesrv &
./rocketmq/distribution/target/apache-rocketmq/bin/mqbroker -n "localhost:9876" autoCreateTopicEnable=true &
```

- 2.stopRocketMQ.sh

```
#! /bin/bash

./rocketmq/rocketmq/distribution/target/apache-rocketmq/bin/mqshutdown broker
./rocketmq/rocketmq/distribution/target/apache-rocketmq/bin/mqshutdown namesrv
```

### 1.3.2 部署启动RocketMQ服务器踩过的坑
- 1.端口:9876，千万不能改，千万不能改，重要的是说2遍
- 2.启动mqbroker是，务必加上: autoCreateTopicEnable=ture
- 3.请在maven的pom.xml文件中，加上fastjson的jar包
- 4.如果还是不行，请看日志，日志位于: ~/logs/rocketmqlogs目录下namesrv.log和broker.log

### 1.3.3 RocketRQ命令
- 1.查看所有topic: sh mqadmin topicList -n 127.0.0.1:9876
- 2.删除topic: sh mqadmin deleteTopic -n 127.0.0.1:9876 -c DefaultCluster -t [topicName]
- 3.查看topicName的详细信息: sh mqadmin topicstatus -n 127.0.0.1:9876 -t [topicName]
- 4.新建一个topic: sh mqadmin updateTopic -n 127.0.0.1:9876 -c DefaultCluster -t [topicName]

# 2. RocketMQ案例学习
第2章仔细分析RocketMQ源码中的example提供的多个案例。对每个案例进行分析和学习，同时普及一些RocketMQ中的简单概念和使用方式

- 1.学习过程，尽量避免大篇幅贴代码。所有代码都能在我的GitHub中找到。重点是理解案例，进而掌握
- 2.案例项目地址: [路径](https://github.com/thinkingfioa/rocketmq-learning/tree/master/rocketmq-example/src/main/java/org/lwl/rocketmq)

## 2.1 quickstart(快速启动)
RocketMQ最简单的消息生产者(Producer)和消息消费者(Consumer)。[参考代码](https://github.com/thinkingfioa/rocketmq-learning/tree/master/rocketmq-example/src/main/java/org/lwl/rocketmq/quickstart)

### 2.1.1 quicketstart 案例提醒点
 - 1.创建Consumer和Producer时候，都会指定Group的名字，可以不必相同。只是标记Consumer和Producer属于哪个组，和消息传输没有关系
 - 2.消息的标记是通过: Topic和Tag共同指定。所以Producer和Consumer生成消息和消费消息时，需要指定消息的Topic和Tag

## 2.2 batch(批生产消息)
批生产消息，能够大大加快消息生产速度。RocketMQ支持批生产消息，一次性发送多条消息。batch案例[参考代码](https://github.com/thinkingfioa/rocketmq-learning/tree/master/rocketmq-example/src/main/java/org/lwl/rocketmq/batch)

### 2.2.1 SimpleBatchProducer
- 1.一次性发送多条消息男，单次发送消息大小 < 1M
- 2.使用批发送消息，请务必保证消息的topic相同

### 2.2.2 SplitBatchProducer(实用)
- 1.SimpleBatchProducer类指定单次发送的消息集合大小 < 1M，但这个要求经常无法满足，所以需要拆包发送。
- 2.SplitBatchProducer采用分割方式，将大消息集合拆分成小集合，然后发送。无需担心消息集合大小

##### 代码:
- 1.使用类ListSplitter来拆分大消息集合，其属性sizeLimit定义单次发送最多的字节数
- 2.实现了迭代器: Iterator<List<Message>>。方便遍历

```html
// 将多条消息，拆分进入小集合中
ListSplitter splitter = new ListSplitter(messageList);
while(splitter.hasNext()) {
    List<Message> subList = splitter.next();
    producer.send(subList);
}

static class ListSplitter implements Iterator<List<Message>> {
    // 单次发送最大字节数
    private int sizeLimit = 1000 * 1000;
    private final List<Message> messages;
    private int currIndex;

    public ListSplitter(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public boolean hasNext() {
        return currIndex < messages.size();
    }

    @Override
    public List<Message> next() {
       ....    
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not allowed to remove");
    }
}
```

## 2.3 ordermessage(顺序消费消息）
ordermessage案例是RocketMQ的一个强势特性案例:顺序消费消息。当多个消息消费者时，往往无法保证消息的顺序问题。ordermessage案例中，利用RocketMQ来实现顺序消费消息。[参考代码](https://github.com/thinkingfioa/rocketmq-learning/tree/master/rocketmq-example/src/main/java/org/lwl/rocketmq/ordermessage)


### 2.3.1 Producer
使用类MessageQueueSelector实现相同的orderId号进入同一个队列queue。这样，保证先发送的消息，先被处理

##### 代码:
```java
// 订单的Producer发送消息需要注册回调函数
SendResult sendResult = producer.send(msg, new MessageQueueSelector() {
    int orderId=0;
    @Override
    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) 	{
        // arg就是orderId，其值与其相等
        Integer id = (Integer) arg;
        int index = id % mqs.size();
        return mqs.get(index);
    }
}, orderId);
```

### 2.3.2 Consumer
- 1.消费者使用类MessageListenerOrderly有序拉取队列queue中的数据。代码参见案例
- 2.提醒源代码中: 请将autoCommit设置为true，否则每次都会从头开始重复消费。

## 2.4 operation(接入命令行)
operation案例，讲解的是如何通过命令行输入的参数，比如输入group, topic等信息传给Producer和Consumer。[参考代码](https://github.com/thinkingfioa/rocketmq-learning/tree/master/rocketmq-example/src/main/java/org/lwl/rocketmq/operation)


### 2.4.1 Commons CLI理解
Commons CLI是Apache为用户提供一个解释命令行解释的API。分为3个步骤:定义、解释和询问交互。

##### Option的参数解释
- 1.第一个参数: 参数的简单形式
- 2.第二个参数: 参数的复杂形式
- 3.第三个参数: 是否需要额外的输入
- 4.第四个参数: 对参数的描述信息

##### 代码:
```Java
final Options options = new Options();
// 1. Commons CLI 定义
// 第一个参数: 参数的简单形式
// 第二个参数: 参数的复杂形式
// 第三个参数: 是否需要额外的输入
// 第四个参数: 对参数的描述信息
Option opt = new Option("h", "help", false, "Print help");
// 终端该参数必须输入
opt.setRequired(false);
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
    }
} catch (ParseException e) {
    ...
}
return commandLine;
```

##### Maven代码依赖:
```xml
// Maven依赖
<dependency>
	<groupId>commons-cli</groupId>
	<artifactId>commons-cli</artifactId>
	<version>1.2</version>
</dependency>
```

### 2.4.1 Producer
- 1.setInstanceName(...) - 同一个Jvm，不同的Producer需要往不同的RocketMQ集群发送消息，需要设置不同的instanceName。
- 2.commandLine.getOptionValue('c') - 从Commons CLI中取得用户输入的参数

### 2.4.2 Consumer
Consumer中的样例代码存在少许难以理解的地方。已作批示和修改。可直接运行起来

## 2.5 openmessaging(分布式消息开放标准)
- 1.openmessaging不是第三方中间件，不是第三方中间件，不是第三方中间件。重要的是说3遍。
- 2.openmessaging是一个力图构建分布式消息分发等开放标准。openmessaging案例中，实现了基于RocketMQ来实现这个开放标准
- 3.可以运行的[代码案例](https://github.com/thinkingfioa/rocketmq-learning/tree/master/rocketmq-example/src/main/java/org/lwl/rocketmq/openmessaging)

## 2.6 broadcast(RocketMQ消费模式)
RocketMQ消费模式有两种: 集群消费和广播消费。分别用来定义单条消息可以被多个相同的GroupName的消费，还是只能被其中一个消费。

##### RocketMQ源代码:
```
ublic enum MessageModel {
    /**
     * broadcast
     */
    BROADCASTING("BROADCASTING"),
    /**
     * clustering
     */
    CLUSTERING("CLUSTERING");
    ...
}
```

### 2.6.1  集群消费和广播消费不同点
- 1.集群消费: 单条消息只会被相同的GroupName中一个Consumer消费(不考虑特别情况下重复消费)
- 2.广播消费：单条消息会被相同的GroupName中每一个声明广播消费的Consumer都消费一次。
- **3.提醒:** 如果两个消费者的GroupName不同，对于单条消息，都会消费一次，即使是集群消费
- 4.可以使用案例中PushConsumer和PushConsumer2来测试比较，帮助记忆和理解
- 5.参考的[代码案例](https://github.com/thinkingfioa/rocketmq-learning/tree/master/rocketmq-example/src/main/java/org/lwl/rocketmq/broadcast)


### 2.6.2 集群消费
- 1.多个Consumer在创建时，都被赋予了一个GroupName。那么单条消息，只会被其中一个Consumer中消费
- 2.一个Producer发送一个消息进入RocketMQ，如果多个消费者GroupName不同，都会接收到这条消息。如果多个Consumer的GroupName相同，则只会由其中一个Consumer消费该条消息
- 3.默认情况下是：集群消费

### 2.6.3 广播消费
消息会发送给Consumer Group中的每一个消费者。声明为Broadcast的每个消费者都会处理该条消息。比如，案例中的PushConsumer和PushConsumer2都会接收到Producer发来的每一条消息消息.

## 2.7 benchmark(基准测试)
banchmark是一个批测试案例。定义多个指标，来判断测试结果。 [参考的代码](https://github.com/thinkingfioa/rocketmq-learning/tree/master/rocketmq-example/src/main/java/org/lwl/rocketmq/benchmark)

### 2.7.1 Consumer
- 1.利用Scheduled定时机制，定时获取统计数据。这是一种非常普通的压力测试数据
- 2.Consumer统计的指标有：消息总量，创建到消费时延，存储到消费时延，最大值等。
- 3.问: MessageSelector.byTag和bySql区别?(解释)

##### 代码:
```java
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
            ...
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

```

### 2.7.2 Producer
- 1.Producer统计指标方法与Consumer完全类似
- 2.Producer中利用producer.send(msg)方法抛出的异常来统计错误类型，可以参考错误类型

##### 代码:
```java
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

    // getXXX()方法
}
```

### 2.7.3 

# 3. RocketMQ源代码分析

# 参考文档
- 1.[《RocketMQ 消息队列单机部署及使用》](https://blog.csdn.net/loongshawn/article/details/51086876)
- 2.[RocketMQ部署文档](https://rocketmq.apache.org/docs/quick-start/)
- 3.[RocketMQ解决消息顺序和重复](https://blog.csdn.net/lovesomnus/article/details/51776942)
- 4.[Commons CLI](https://www.cnblogs.com/xing901022/archive/2016/06/22/5608823.html)
