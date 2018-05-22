package com.yhyecho.demo.service;

import com.yhyecho.demo.DS;
import com.yhyecho.demo.dao.DynamicDSMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Echo on 5/23/18.
 */
@Service
public class DynamicServciceImpl implements DynamicServcice {
    @Autowired
    private DynamicDSMapper dynamicDSMapper;

    @DS()
    public Integer ds1() {
        return dynamicDSMapper.queryJournal();
    }

    @DS(name = "logic")
    public String ds2() {
        return dynamicDSMapper.queryUser();
    }

    @DS(name = "dao")
    public List<Object> ds3() {
        return dynamicDSMapper.queryType();
    }

}
