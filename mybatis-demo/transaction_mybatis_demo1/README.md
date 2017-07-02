# transaction_mybatis_demo1
transaction_mybatis_demo1 事务发起方


### 使用步骤

项目依赖服务：  
redis服务   
mysql服务   
dubbo服务  
zookeeper服务   


1. 启动[txManager](https://github.com/1991wangliang/txManager)服务.
2. 创建数据库test，执行test.sql脚本
3. 配置transaction_mybatis_demo1和transaction_demo2的db.properties和applicationContext_dubbo_consumer.xml配置
3. 启动transaction_mybatis_demo2的[TransactionTest.test()](https://github.com/1991wangliang/transaction_mybatis_demo2)
4. 运行该项目的TransactionTest.test()方法。

#### 注意：
 由于三个dubbo服务在一个dubbo服务下，需要修改port不能冲突`<dubbo:protocol accesslog="true" name="dubbo" port="20881" />`


 在所有的消费方添加filter`<dubbo:consumer  filter="transactionFilter" />` 还有配置文件 `META-INF/dubbo/com.alibaba.dubbo.rpc.Filter` 参考本demo
 
#### 效果：
会出异常，然后数据库数据不被修改。当注释调transaction_mybatis_demo1 TestServiceImpl下的 int v = 100/0;代码后，数据库下t_test表会增加两条数据。
