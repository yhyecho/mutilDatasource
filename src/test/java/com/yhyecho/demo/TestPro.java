package com.yhyecho.demo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Echo on 5/23/18.
 */
public class TestPro {

    //int a = bigdemical.compareTo(bigdemical2)
    //a = -1,表示bigdemical小于bigdemical2；
    //a = 0,表示bigdemical等于bigdemical2；
    //a = 1,表示bigdemical大于bigdemical2；

    public static void main(String[] args) {
        List<Person> ourList = new ArrayList<>();
        Person person1 = new Person("1", new BigDecimal("120"));
        Person person2 = new Person("2", new BigDecimal("110"));
        Person person3 = new Person("3", new BigDecimal("60"));
        Person person4 = new Person("4", new BigDecimal("50"));
        Person person5 = new Person("5", new BigDecimal("10"));

        ourList.add(person1);
        ourList.add(person2);
        ourList.add(person3);
        ourList.add(person4);
        ourList.add(person5);

        List<Person> tradeList = new ArrayList<>();
        Person person6 = new Person("6", new BigDecimal("50"));
        // Person person7 = new Person("7", new BigDecimal("90"));

        tradeList.add(person6);
        //tradeList.add(person7);

        if (tradeList.size() == 0) {
            for (Person item : ourList) {
                System.out.println("单边记账: id " + item.getId() + " value =>" + item.getValue());
            }
        }

        if (ourList.size() == 1) {

            // 交易双方各一条元素
            if (ourList.size() == tradeList.size()) {
                BigDecimal our = ourList.get(0).getValue();
                BigDecimal trade = tradeList.get(0).getValue();
                int result = our.compareTo(trade);
                if (result == 0) {
                    System.out.println("相等抵消" + ourList.get(0).getId() + ourList.get(0).getValue());
                } else if (result == -1) {
                    System.out.println("资产抵小" + trade.subtract(our));
                } else {
                    System.out.println("负债抵小" + our.subtract(trade));
                }
            }


        }

        if (ourList.size() > 1) {
            if (ourList.size() > tradeList.size()) {
                // 多对1
                if (tradeList.size() == 1) {
                    Iterator<Person> iterator = ourList.iterator();
                    while (iterator.hasNext()) {
                        Person next = iterator.next();
                        // 负债方有一条记录且与资产方的一条相匹配
                        if (next.getValue().compareTo(tradeList.get(0).getValue()) == 0) {
                            System.out.println("相等抵消" + next.getId() + " => " + next.getValue() + "交易对手: " + tradeList.get(0).getId() + " => " + tradeList.get(0).getValue());
                            iterator.remove();
                        } else if (next.getValue().compareTo(tradeList.get(0).getValue()) == 1) {
                            // 资产方大
                            System.out.println("负债抵小" + "差额:" + next.getValue().subtract(tradeList.get(0).getValue()));
                            iterator.remove();
                            break;
                        } else {
                            System.out.println("===多对一匹配==");
                            ourList = cacl(ourList, tradeList.get(0).getValue());
                            break;
                        }
                    }
                    // 未匹配的做单边记账
                    for (Person item : ourList) {
                        System.out.println("单边记账:" + item.getId() + " => " + item.getValue());
                    }
                }
                // 多对多
                else {
                    System.out.println("多对多, 资产方多于负债");
                }
            } else if (ourList.size() == tradeList.size()) {
                System.out.println("多对多, 个数一样");
            } else {
                System.out.println("负债方个数多");
            }
        }


    }

    private static List<Person> cacl(List<Person> personList, BigDecimal target) {
        // 5 4 3 2 1
        BigDecimal sum = personList.get(0).getValue();
        for (int i = 1; i < personList.size(); i++) {
            sum = sum.add(personList.get(i).getValue());
            if (sum.compareTo(target) == 0) {
                System.out.println("匹配成功");
                System.out.println("位置" + i);
                for (int j = 0; j <= i; j++) {
                    personList.remove(0);
                }
                return personList;
            } else if (sum.compareTo(target) == 1) {
                BigDecimal result = sum.subtract(target);
                System.out.println("位置" + i + "数值" + result);
                System.out.println("大于了相等");
                for (int j = 0; j < i; j++) {
                    personList.remove(0);
                }
                personList.get(0).setValue(result);
                return personList;
            } else {
                if (i == personList.size() - 1) {
                    System.out.println("全匹配玩,也没匹配上" + sum + "差额:" + target.subtract(sum));
                    // 清空list
                    personList.clear();
                    return personList;
                }
            }
        }
        return personList;
    }


    // 1对1 匹配
    //  A - B 资产
    //  1       70

    // B - A 负债
    //  1        70

    // 1 对1 不匹配

    //  A - B 资产
    //  1       70

    // B - A 负债
    //  1        60

    // 多对一
    //  A - B 资产
    //  1       120
    //  1       70
    //  1       60
    //  1       50

    // B - A 负债
    // 1        70

    // 多对一
    //  A - B 资产
    //  1       120
    //  1       70
    //  1       60
    //  1       50

    // B - A 负债
    // 1        500

    // 多对1 非全匹配
    //  A - B
    //  1       120
    //  1       70
    //  1       60
    //  1       50

    // B - A
    // 1        90

    // 多对1, 存在匹配项目
    //  A - B
    //  1       120
    //  1       70
    //  1       60
    //  1       50

    // B - A
    // 1        50
    // 1 75

}
