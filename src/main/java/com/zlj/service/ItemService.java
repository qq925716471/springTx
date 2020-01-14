package com.zlj.service;

import com.zlj.dao.ItemDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@Service
public class ItemService {
    @Autowired
    private ItemDao itemDao;

    @Transactional(rollbackFor = Throwable.class)
    public void delete(int goodsId) {
        itemDao.delete(goodsId);
        int a = 1/0;
    }

}
