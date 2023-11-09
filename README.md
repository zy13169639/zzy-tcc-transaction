如果这个项目对你有所帮助，记得 ![:heart:](https://cn-assets.gitee.com/assets/emoji/heart-aa0a990af1ed6612e33b6344ea04b28b.png) Star 关注 ![:heart:](https://cn-assets.gitee.com/assets/emoji/heart-aa0a990af1ed6612e33b6344ea04b28b.png) 哦，这是对我最大的支持与鼓励。

#### 介绍

架构：省略，网上关于TCC一大堆。。。

本项目提供轻量级TCC分布式事务解决方案，旨在利用最少的资源实现分布式事务，只要项目中用到`Redis`和关系型数据库，那么都可以使用本项目来实现事务的最终一致性。

#### 软件架构

springboot-2.x.x，redis-5.x

#### 安装教程

只需在项目中引入一个依赖

```xml
<dependency>
    <groupId>com.zzy</groupId>
    <artifactId>zzy-tcc-transaction-booter</artifactId>
    <version>${main.version}</version>
</dependency>
```

然后在需要的事务控制的请求上加上`@DistributeTransaction`注解

**请注意**：还要在本地事务的执行方法中加上`@InTransactional(InTransactionalWorker.BUSINESS_SUCCESS)`

#### 使用说明

具体参考`zzy-tcc-transaction-samples`模块使用。

#### 参与贡献

欢迎给出优化建议以及指出bug，请清楚描述遇到的问题，通过提issue或者直接联系作者讨论，感激不尽。

#### 联系作者

![:email:](https://cn-assets.gitee.com/assets/emoji/email-9bd5677c771795ddf4d9b357561b9ff9.png) 邮箱： [13169639@qq.com](mailto:13169639@qq.com)

QQ交流群：773938960