package com.yhyecho.demo.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Created by Echo on 5/23/18.
 */

@Mapper
public interface DynamicDSMapper {
    Integer queryJournal();

    String queryUser();

    List<Object> queryType();
}
