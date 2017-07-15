package com.demo.service.impl;

import com.demo.dao.TestDao;
import com.demo.service.Test5Service;
import com.lorne.tx.annotation.TxTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lorne on 2017/6/7.
 */
@Service
public class Test5ServiceImpl implements Test5Service {


    @Autowired
    private TestDao testDao;



    @Override
    @TxTransaction
    public String test() {

        String name = "hello_demo5";

        testDao.save(name);

      //  int v = 100/0;

        return name;

    }
}
