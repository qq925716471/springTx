package com.zlj.service;

import com.zlj.annotation.Transactional;
import com.zlj.dao.GoodsDao;
import com.zlj.dao.ItemDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
@Service
public class GoodsService {
    private GoodsDao goodsDao = new GoodsDao();
    private ItemDao itemDao = new ItemDao();

    public void delete(int goodsId) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "debian-sys-maint", "MTie2ZhYlrPxrSaw");//连接数据库
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
        Database
            goodsDao.delete(5);
            itemDao.delete(5);
    }
    public static void main(String[] args) throws SQLException {
        GoodsService goodsService = new GoodsService();
        goodsService.delete(1);
    }
}
