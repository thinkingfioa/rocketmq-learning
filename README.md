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

./rocketmq/distribution/target/apache-rocketmq/bin/mqnamesrv -n "localhost:6666" &
./rocketmq/distribution/target/apache-rocketmq/bin/mqbroker -n "localhost:6666" &
```

- 2.stopRocketMQ.sh

```
#! /bin/bash

./rocketmq/rocketmq/distribution/target/apache-rocketmq/bin/mqshutdown broker
./rocketmq/rocketmq/distribution/target/apache-rocketmq/bin/mqshutdown namesrv
```

## 2. RocketMQ案例学习

# 参考文档
- 1.[《RocketMQ 消息队列单机部署及使用》](https://blog.csdn.net/loongshawn/article/details/51086876)
- 2.[RocketMQ部署文档](https://rocketmq.apache.org/docs/quick-start/)
