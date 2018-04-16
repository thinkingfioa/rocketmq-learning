# RocketMQ 源码学习
```
@author 鲁伟林
向开源致敬，向优秀前辈代码致敬。
源码地址：https://github.com/apache/rocketmq
RocketMQ学习版本：4.2.0
曾用名: Metaq
```
---

## 1.为什么要读RocketMQ源码?
随着分布式应用的需求，中间件已经成为重点研究领域。中间件可以轻松帮助实现分布式系统。RocketMQ出自于阿里集团，是众多中间件中非常优秀项目。通过源码学习，可以学到优秀的编码风格、编程技术和设计理念。

### 1.1 RocketMQ特点
- 1.支持严格的消息顺序
- 2.支持Topic与Queue两种模式
- 3.亿级消息堆积能力
- 4.比较友好的分布式特性
- 5.同时支持Push与Pull方式消费信息

### 1.2 RocketMQ服务器
RocketMQ不同于ZeroMQ，ZeroMQ是一个端到端的消息中间件。RocketMQ除了消息生产者和消息消费者外，还需要单独起一个RocketMQ服务器，充当Master节点。
![](https://img-blog.csdn.net/20160408142513136)

### 1.3 部署RocketMQ服务器
参考文档: [RocketMQ部署文档](https://rocketmq.apache.org/docs/quick-start/)

##### 1.3.1 通过脚本启动
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

##### 1.3.2 部署启动RocketMQ服务器踩过的坑
- 1.端口:9876，千万不能改，千万不能改，重要的是说2遍
- 2.启动mqbroker是，务必加上: autoCreateTopicEnable=ture
- 3.请在maven的pom.xml文件中，加上fastjson的jar包
- 4.如果还是不行，请看日志，日志位于: ~/logs/rocketmqlogs目录下namesrv.log和broker.log

## 2. RocketMQ案例学习
案例项目地址: [路径](https://github.com/thinkingfioa/rocketmq-learning/tree/master/rocketmq-example/src/main/java/org/lwl/rocketmq)

### 2.1 quickstart
RocketMQ最简单的消息生产者(Producer)和消息消费者(Consumer)。[参考代码](https://github.com/thinkingfioa/rocketmq-learning/tree/master/rocketmq-example/src/main/java/org/lwl/rocketmq/quickstart)

#### 2.1.1 quicketstart 案例提醒点
 - 1.创建Consumer和Producer时候，都会指定Group的名字，可以不必相同。只是标记Consumer和Producer属于哪个组，和消息传输没有关系
 - 2.消息的标记是通过: Topic和Tag共同指定。所以Producer和Consumer生成消息和消费消息时，需要指定消息的Topic和Tag

### 2.2 batch
RocketMQ支持批生产消息，一次性发送多条消息。[参考代码](https://github.com/thinkingfioa/rocketmq-learning/tree/master/rocketmq-example/src/main/java/org/lwl/rocketmq/batch)

#### 2.2.1 SimpleBatchProducer
- 1.单词发送消息< 1M，一次性发送多条消息
- 2.使用批发送消息，请务必保证消息的topic相同

#### 2.2.2 SplitBatchProducer(实用)
- 1.SimpleBatchProducer类指定单次发送的消息集合大小必须 < 1M，但这个要求经常无法满足。
- 2.SplitBatchProducer无需担心消息集合大小，采用分割方式，将大消息集合拆分成小集合，然后发送


## 3. RocketMQ源代码分析

# 参考文档
- 1.[《RocketMQ 消息队列单机部署及使用》](https://blog.csdn.net/loongshawn/article/details/51086876)
- 2.[RocketMQ部署文档](https://rocketmq.apache.org/docs/quick-start/)
