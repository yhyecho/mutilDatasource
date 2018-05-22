package com.yhyecho.demo;

import com.yhyecho.demo.service.DynamicServcice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * Created by Echo on 5/23/18.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestDynamicDS {
    private Logger logger = LoggerFactory.getLogger(TestDynamicDS.class);
    //
    @Autowired
    private DynamicServcice dynamicServcice;

    @Test
    public void test() {
        Integer integer = dynamicServcice.ds1();
        logger.info("integer:" + integer);
//        String ds2 = dynamicServcice.ds2();
//        logger.info("ds2:"+ds2);
        //List<Object> ds3 = dynamicServcice.ds3();
        //System.out.println(ds3);
        // logger.info("ds3:" + ds3);
    }
}
