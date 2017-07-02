package com.demo.interceptor;

import com.lorne.tx.dubbo.interceptor.TxManagerInterceptor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Created by lorne on 2017/6/28.
 */
@Aspect
@Component
public class TxTransactionInterceptor {

    @Autowired
    private TxManagerInterceptor txManagerInterceptor;

    @Order(10)
    @Around("execution(* com.demo.service.impl.*Impl.*(..))")
    public Object around(ProceedingJoinPoint point)throws Throwable{
        return txManagerInterceptor.around(point);
    }
}
