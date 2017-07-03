# dubbo LCN分布式事务示例demo


## 使用说明

框架分为jdbc／hibernate／mybatis三个版本。各个版本之间除了DB框架差异以外，其他配置都相同。

demo分为两类，demo1/demo2 只是消费者与提供者两个的简单demo。以及demo1/2/3/4/5复杂类型调用关系。

demo1/demo2类型：

demo1作为消费者（分布式事务的发起者）调用demo2.

demo1/2/3/4/5类型：

demo1作为分布式事务的发起者，调用了demo2 demo3，demo3有调用了demo4 demo5.

## 使用步骤

1. 启动[TxManager](https://github.com/1991wangliang/tx-lcn/tree/master/tx-manager) 

2. 添加配置maven库与tx-lcn库

maven私有仓库地址：
```
	<repositories>
		<repository>
			<id>lorne</id>
			<url>https://1991wangliang.github.io/repository</url>
		</repository>
	</repositories>

```
maven jar地址 

```
		<dependency>
			<groupId>com.lorne.tx</groupId>
			<artifactId>dubbo-transaction</artifactId>
			<version>x.x.x.RELEASE</version>
		</dependency>

```
最新版本为 `2.0.0.RELEASE`

3. 添加tx.properties

```
#txmanager地址 /tx/manager/getServer写法固定
url=http://192.168.3.102:8888/tx/manager/getServer

```

4. 添加事务拦截器
```java

@Aspect
@Component
public class TxTransactionInterceptor {

    @Autowired
    private TxManagerInterceptor txManagerInterceptor;

    @Around("execution(* com.example.demo.service.impl.*Impl.*(..))")
    public Object around(ProceedingJoinPoint point)throws Throwable{
        return txManagerInterceptor.around(point);
    }
}

```

注意：@Around 拦截地址不能包含com.lorne.tx.*

5. 添加`META-INF\dubbo\com.alibaba.dubbo.rpc.Filter`配置。

```

transactionFilter=com.lorne.tx.dubbo.filter.TransactionFilter

```

在dubbo配置文件下添加` <dubbo:consumer  filter="transactionFilter" />`


6. 创建数据库，项目都是依赖相同的数据库，创建一次其他的demo下将不再需要重复创建。mysql数据库，库名称test

```sql

USE test;

DROP TABLE IF EXISTS `t_test`;

CREATE TABLE `t_test` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


```

## 测试说明


demo1/demo2类型:

运行demo2下的TransactionTest，再运行demo1下的TransactionTest。

效果：/by zero 异常所有事务都回滚。

说明： demo1都是消费者，默认在业务里添加了`int v = 100/0;`异常代码。因此在不注释的情况下事务回归。

demo1/2/3/4/5类型:
 
运行demo5下的TransactionTest，再运行demo4下的TransactionTest，再运行demo3下的TransactionTest，再运行demo2下的TransactionTest，再运行demo1下的TransactionTest。

效果：/by zero 异常所有事务都回滚。
 
说明：demo1和demo3是消费者，默认在业务里添加了`int v = 100/0;`，demo3这行已注释，默认回滚，全部注释掉会提交事务。


技术交流群：554855843