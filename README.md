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
最新版本为 `3.0.0.RELEASE`

3. 添加tx.properties

```

#txmanager地址  http://txmanager ip:txmanager port/txmanager name/tx/manager/getServer 写法固定
url=http://127.0.0.1:8761/tx/manager/getServer

#事务补偿记录配置


#db 数据库类型 目前支持 mysql oracle sqlserver
compensate.db.dbType = mysql



```

4. 添加事务拦截器，确保拦截器的优先级高于spring事务优先级
```java

@Aspect
@Component
public class TxTransactionInterceptor  implements Ordered{

    @Autowired
    private TxManagerInterceptor txManagerInterceptor;

    @Override
    public int getOrder() {
        return 1;
    }

    @Around("execution(* com.demo.service.impl.*Impl.*(..))")
    public Object around(ProceedingJoinPoint point)throws Throwable{
        return txManagerInterceptor.around(point);
    }
}


```
注意：  
@Around 拦截地址不能包含com.lorne.tx.*   
LCN是不控制事务。切面仅用于识别LCN分布式事务的作用。


5. 添加`META-INF\dubbo\com.alibaba.dubbo.rpc.Filter`配置。

```

transactionFilter=com.lorne.tx.dubbo.filter.TransactionFilter

```

在dubbo配置文件下添加
```
 <!-- 请求拦截器-->
 <dubbo:consumer  filter="transactionFilter" />
 
 <!-- 拒绝重复调用-->
 <dubbo:provider delay="-1" timeout="6000"  port="20881"  retries="0"/>
 
```

 

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

7. spring配置说明

若spring下面配置了`<aop:aspectj-autoproxy expose-proxy="true"/>`增加`proxy-target-class="true" `

如下：
```
 <aop:aspectj-autoproxy expose-proxy="true" proxy-target-class="true" />

```

8. 连接池配置，**后面操作的连接池都必须是LCN代理连接池**

```
    <!--mysql druid连接池配置-->
    <bean name="dataSource" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close">
        <property name="url" value="jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&amp;characterEncoding=utf8"/>
        <property name="username" value="root"/>
        <property name="password" value="root"/>
        <!-- 初始化连接大小 -->
        <property name="initialSize" value="5"/>
        <!-- 连接池最大并发使用连接数量 -->
        <property name="maxActive" value="50"/>
        <!-- 连接池最小空闲 -->
        <property name="minIdle" value="1"/>
        <!-- 获取连接最大等待时间 -->
        <property name="maxWait" value="60000"/>
        <!-- 打开pscache功能  在mysql5.5以上版本支持 -->
        <property name="poolPreparedStatements" value="true"/>
        <!-- 指定每个连接上的pscache的大小 -->
        <property name="maxPoolPreparedStatementPerConnectionSize" value="33"/>
        <property name="validationQuery" value="select 1"/>
        <property name="testOnBorrow" value="false"/>
        <!-- 归还连接时执行validationQuery  ，检测是否有效，设置为true这样会降低性能 -->
        <property name="testOnReturn" value="false"/>
        <!-- 申请链接的时间是否检测 -->
        <property name="testWhileIdle" value="true"/>
        <!-- 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒 -->
        <property name="timeBetweenEvictionRunsMillis" value="60000"/>
        <!-- 配置一个连接在池中最小生存的时间，单位是毫秒 -->
        <property name="minEvictableIdleTimeMillis" value="25200000"/>
        <!-- 打开超过时间限制是否回收功能 -->
        <property name="removeAbandoned" value="true"/>
        <!-- 超过多长时间 1800秒，也就是30分钟 -->
        <property name="removeAbandonedTimeout" value="1800"/>
        <!-- 关闭abanded连接时输出错误日志 -->
        <property name="logAbandoned" value="true"/>
        <!-- 监控数据库 -->
        <property name="filters" value="stat"/>
        <!--<property name="filters" value="mergeStat"/>-->
        <!-- 慢sql监控 10毫秒 -->
        <!--<property name="connectionProperties" value="druid.stat.slowSqlMillis=10" />-->
    </bean>
    
    <!--mysql 补偿连接池 compensateDataSource 固定写法-->
    <bean name="compensateDataSource" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close">
        <property name="url" value="${db.url}"/>
        <property name="username" value="${db.username}"/>
        <property name="password" value="${db.password}"/>
        <!-- 初始化连接大小 -->
        <property name="initialSize" value="5"/>
        <!-- 连接池最大并发使用连接数量 -->
        <property name="maxActive" value="50"/>
        <!-- 连接池最小空闲 -->
        <property name="minIdle" value="1"/>
        <!-- 获取连接最大等待时间 -->
        <property name="maxWait" value="60000"/>
        <!-- 打开pscache功能  在mysql5.5以上版本支持 -->
        <property name="poolPreparedStatements" value="true"/>
        <!-- 指定每个连接上的pscache的大小 -->
        <property name="maxPoolPreparedStatementPerConnectionSize" value="33"/>
        <property name="validationQuery" value="select 1"/>
        <property name="testOnBorrow" value="false"/>
        <!-- 归还连接时执行validationQuery  ，检测是否有效，设置为true这样会降低性能 -->
        <property name="testOnReturn" value="false"/>
        <!-- 申请链接的时间是否检测 -->
        <property name="testWhileIdle" value="true"/>
        <!-- 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒 -->
        <property name="timeBetweenEvictionRunsMillis" value="60000"/>
        <!-- 配置一个连接在池中最小生存的时间，单位是毫秒 -->
        <property name="minEvictableIdleTimeMillis" value="25200000"/>
        <!-- 打开超过时间限制是否回收功能 -->
        <property name="removeAbandoned" value="true"/>
        <!-- 超过多长时间 1800秒，也就是30分钟 -->
        <property name="removeAbandonedTimeout" value="1800"/>
        <!-- 关闭abanded连接时输出错误日志 -->
        <property name="logAbandoned" value="true"/>
        <!-- 监控数据库 -->
        <property name="filters" value="stat"/>
        <!--<property name="filters" value="mergeStat"/>-->
        <!-- 慢sql监控 10毫秒 -->
        <!--<property name="connectionProperties" value="druid.stat.slowSqlMillis=10" />-->
    </bean>

        
    <!--lcn代理连接池配置-->
    <bean name="lcnDataSourceProxy" class="com.lorne.tx.db.LCNDataSourceProxy">
        <property name="dataSource" ref="dataSource"/>
          <!-- 分布式事务参与的最大连接数，确保不要超过普通连接池的最大值即可 -->
        <property name="maxCount" value="20"/>
    </bean>
    
    
    <!--jdbcTemplate -->
    <bean id="jdbcTemplate"
          class="org.springframework.jdbc.core.JdbcTemplate">
        <property name="dataSource">
            <ref bean="lcnDataSourceProxy"/>
        </property>
    </bean>
        
    <!--jdbc事务配置 -->
    <bean id="transactionManager"
          class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="lcnDataSourceProxy" />
    </bean>
    


    
```

## 注意事项

1. 禁止重名的bean对象。

  事务的补偿机制是基于java反射的方式重新执行一次需要补偿的业务。因此执行的时候需要获取到业务的service对象，LCN是基于spring的ApplicationContent的getBean方法获取bean的对象的。因此不允许出现重名对象。
    
在配置服务的时候注意，如下禁止：

```
    <dubbo:service interface="com.demo.service.Test2Service" ref="test2Service"  />

    <bean id="test2Service" class="com.demo.service.impl.Test2ServiceImpl"   />
    
```

应该使用如下方式： 

```
    <dubbo:service interface="com.demo.service.Test2Service" ref="test2ServiceImpl"  />
    
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
