package com.zlj.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.util.unit.DataUnit;

import java.sql.*;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

@Slf4j
public class GoodsDao {
    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");//加载数据库驱动
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int save2() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "debian-sys-maint", "MTie2ZhYlrPxrSaw");//连接数据库
        PreparedStatement pstmt = null;
        PreparedStatement pstmt2 = null;
        ResultSet rs = null;
        try {
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement("insert into goods (name) value (?)", RETURN_GENERATED_KEYS);
            pstmt.setString(1, "test");
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            int id = 0;
            if (rs.next())
                id = rs.getInt(1);
            pstmt2 = conn.prepareStatement("insert into item (goods_id,name) value (?,?)");
            pstmt2.setInt(1, id);
            pstmt2.setString(2, "test");
            pstmt2.executeUpdate();
            conn.commit();
            return id;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            conn.rollback();
        } finally {
            if (conn != null) {
                conn.close();
            }
            if (pstmt != null) {
                pstmt.close();
            }
            if (pstmt2 != null) {
                pstmt2.close();
            }
            if (rs != null) {
                rs.close();
            }
        }
        return 0;
    }

    public int delete(Connection conn, int id) throws SQLException {
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("delete from goods where id = ?");
            pstmt.setInt(1, id);
            return pstmt.executeUpdate();
        } finally {
            if (pstmt != null) {
                pstmt.close();
            }
        }
    }

    public int delete( int id) throws SQLException {
        Connection conn = Datasource
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("delete from goods where id = ?");
            pstmt.setInt(1, id);
            return pstmt.executeUpdate();
        } finally {
            if (pstmt != null) {
                pstmt.close();
            }
        }
    }

    public static void main(String[] args) throws SQLException {
        GoodsDao goodsDao = new GoodsDao();
        goodsDao.save2();
    }
}
