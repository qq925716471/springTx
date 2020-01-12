package com.zlj.dao;

import java.sql.*;

public class ItemDao {
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
}
