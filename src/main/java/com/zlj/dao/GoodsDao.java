package com.zlj.dao;

import com.zlj.support.TransactionSynchronizationManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataUnit;

import javax.sql.DataSource;
import java.sql.*;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

@Slf4j
@Component
public class GoodsDao {
    @Autowired
    private DataSource dataSource;

    public int save2() throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = null;
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
            pstmt = conn.prepareStatement("insert into item (goods_id,name) value (?,?)");
            pstmt.setInt(1, id);
            pstmt.setString(2, "test");
            pstmt.executeUpdate();
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

    public int delete(int id) throws SQLException {
        Connection conn = (Connection) TransactionSynchronizationManager.getResource(dataSource);
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
