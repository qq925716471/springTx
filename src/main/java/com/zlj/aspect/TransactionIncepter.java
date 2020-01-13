package com.zlj.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Aspect
@Component
public class TransactionIncepter {
    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");//加载数据库驱动
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Around("@annotation(com.zlj.annotation.Transactional)")
    public Object invokeWithTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "debian-sys-maint", "MTie2ZhYlrPxrSaw");//连接数据库

        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            throw e;
        } finally {

        }

    }
}
