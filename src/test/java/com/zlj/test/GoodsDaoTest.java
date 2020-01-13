package com.zlj.test;

import com.zlj.Application;
import com.zlj.dao.GoodsDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.SQLException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class GoodsDaoTest {
    @Autowired
    private GoodsDao goodsDao;

    @Test
    public void testSave2() throws SQLException {
        goodsDao.save2();
    }
}
