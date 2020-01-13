package com.zlj.dao;

import com.zlj.support.TransactionSynchronizationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;

@Component
public class ItemDao {
    @Autowired
    private DataSource dataSource;

    public int delete(Connection conn, int goodsId) throws SQLException {
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("delete from item where goods_id = ?");
            pstmt.setInt(1, goodsId);
            return pstmt.executeUpdate();
        } finally {
            if (pstmt != null) {
                pstmt.close();
            }
        }
    }

    public int delete(int goodsId) throws SQLException {
        Connection conn = (Connection) TransactionSynchronizationManager.getResource(dataSource);
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("delete from item where goods_id = ?");
            pstmt.setInt(1, goodsId);
            return pstmt.executeUpdate();
        } finally {
            if (pstmt != null) {
                pstmt.close();
            }
        }
    }
}
