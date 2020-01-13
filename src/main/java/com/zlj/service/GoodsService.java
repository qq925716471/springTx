package com.zlj.service;

import com.zlj.annotation.Transactional;
import com.zlj.dao.GoodsDao;
import com.zlj.dao.ItemDao;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
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

    public void delete(int goodsId) throws SQLException {
        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(false);
        try {
            goodsDao.delete(conn, 5);
            itemDao.delete(conn, 5);
            conn.commit();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            conn.rollback();
        } finally {
            conn.close();
        }
    }

    @Transactional
    public void delete2(int goodsId) throws SQLException {
        goodsDao.delete(5);
        itemDao.delete(5);
    }

    public static void main(String[] args) throws SQLException {
        GoodsService goodsService = new GoodsService();
        goodsService.delete(1);
    }
}
