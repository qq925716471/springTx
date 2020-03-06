package com.zlj.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        conn.setAutoCommit(false);
        try {
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
            if (rs != null) {
                rs.close();
            }
            if (pstmt != null) {
                pstmt.close();
            }
            if (conn != null) {
                conn.close();
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

    public void delete(final int id) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("delete from goods where id = ?", new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setInt(1, id);
            }
        });
    }

}
