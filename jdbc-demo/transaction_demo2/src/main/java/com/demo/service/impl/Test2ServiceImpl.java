package com.demo.service.impl;

import com.demo.dao.TestDao;
import com.demo.service.Test2Service;
//import com.lorne.tx.mq.service.impl.MQTransactionServiceImpl;
import com.lorne.tx.annotation.TxTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lorne on 2017/6/7.
 */
@Service
public class Test2ServiceImpl  implements Test2Service {


    @Autowired
    private TestDao testDao;



    @Override
    @TxTransaction
    public String test() {

        String name = "hello_demo2";

        testDao.save(name);


        return name;

    }
}
