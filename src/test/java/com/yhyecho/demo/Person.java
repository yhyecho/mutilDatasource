package com.yhyecho.demo;

import java.math.BigDecimal;

/**
 * Created by Echo on 5/23/18.
 */
public class Person {
    private String id;

    private BigDecimal value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public Person(String id, BigDecimal value) {
        this.id = id;
        this.value = value;
    }
}
