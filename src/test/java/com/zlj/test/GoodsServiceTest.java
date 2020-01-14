package com.zlj.test;

import com.zlj.Application;
import com.zlj.service.GoodsService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.SQLException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class GoodsServiceTest {
    @Autowired
    private GoodsService goodsService;

    @Test
    public void testDelete() throws SQLException {
        goodsService.delete(5);
    }

    @Test
    public void testDelete2() throws SQLException {
        goodsService.delete2(5);
    }

    @Test
    public void testDelete3() throws SQLException {
        goodsService.delete3(5);
    }

    @Test
    public void testDelete4() throws SQLException {
        goodsService.delete4(5);
    }
}
