package com.demo.service.impl;

import com.demo.dao.TestDao;
import com.demo.service.Test4Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lorne on 2017/6/7.
 */
@Service
public class Test4ServiceImpl implements Test4Service {


    @Autowired
    private TestDao testDao;


    @Override
    public String test() {

        String name = "hello_demo4";

        testDao.save(name);

        //  int v = 100/0;

        return name;

    }
}
