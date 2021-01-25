# Curator
Curator 是一套由netflix 公司开源的，Java 语言编程的 ZooKeeper 客户端框架，Curator项目是现在ZooKeeper 客户端中使用最多，对ZooKeeper 版本支持最好的第三方客户端。
Curator 把我们平时常用的很多 ZooKeeper 服务开发功能做了封装，例如 Leader 选举、 分布式计数器、分布式锁。这就减少了技术人员在使用 ZooKeeper 时的大部分底层细节开发工作。
在会话重新连接、Watch 反复注册、多种异常处理等使用场景中，用原生的 ZooKeeper 处理比较复杂。而在使用 Curator 时，由于其对这些功能都做了高度的封装，使用起来更加简单，不但减少了开发时间，而且增强了程序的可靠性。
