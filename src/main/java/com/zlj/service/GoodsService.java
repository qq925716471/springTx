package com.zlj.service;

import com.zlj.dao.GoodsDao;
import com.zlj.dao.ItemDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@Service
public class GoodsService {
    @Autowired
    private GoodsDao goodsDao;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;
    @Autowired
    private ItemService itemService;
    @Autowired
    private TransactionTemplate transactionTemplate;

    public void delete(int goodsId) throws SQLException {
        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(false);
        try {
            goodsDao.delete(conn, goodsId);
            itemDao.delete(conn, goodsId);
            conn.commit();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            conn.rollback();
        } finally {
            conn.close();
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void delete2(int goodsId) throws SQLException {
        goodsDao.delete(goodsId);
        itemDao.delete(goodsId);
    }

    public void delete3(final int goodsId) throws SQLException {
        RuleBasedTransactionAttribute rbta = new RuleBasedTransactionAttribute();
        rbta.getRollbackRules().add(new RollbackRuleAttribute(Exception.class));

        TransactionTemplate transactionTemplate = new TransactionTemplate(dataSourceTransactionManager, rbta);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                goodsDao.delete(goodsId);
                itemDao.delete(goodsId);
            }
        });
    }

    @Transactional(rollbackFor = Throwable.class)
    public void delete4(int goodsId) {
        goodsDao.delete(goodsId);
        itemService.delete(goodsId);
    }

}
