package com.yhyecho.demo;

import java.math.BigDecimal;
import java.util.*;

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
        Person person1 = new Person("1", new BigDecimal("120.00"));
        Person person2 = new Person("2", new BigDecimal("110.00"));
        Person person3 = new Person("3", new BigDecimal("60.00"));
        Person person4 = new Person("4", new BigDecimal("50.00"));
        Person person5 = new Person("5", new BigDecimal("10.00"));

        ourList.add(person1);
        ourList.add(person2);
        ourList.add(person3);
        ourList.add(person4);
        ourList.add(person5);

        List<Person> tradeList = new ArrayList<>();
        Person person6 = new Person("6", new BigDecimal("50"));
        Person person7 = new Person("7", new BigDecimal("60"));
        Person person8 = new Person("8", new BigDecimal("10"));
        Person person9 = new Person("9", new BigDecimal("40"));

        tradeList.add(person6);
        tradeList.add(person7);
        tradeList.add(person8);
        tradeList.add(person9);

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
                    List<String> inputList = new ArrayList<>();
                    for (Person item : tradeList) {
                        inputList.add(item.getId() + "-" + item.getValue());
                    }

                    boolean sortFlag = false;
                    int listSize = 0;
                    for (int i = 0; i < ourList.size(); i++) {
                        if (i == 0) {
                            // 首次需要排列组合
                            sortFlag = true;
                        }
                        List<String> resultList = TestPro.handler(inputList, ourList.get(i).getValue().toString(), sortFlag);
                        listSize = resultList.size();
                        if (listSize == 0) {
                            break;
                        } else {
                            continue;
                        }
                        //if (listSize != 0 && list2.size() == list3.size()) {
                        //    System.out.println("next");
                        //} else {
                        //    if (listSize == 0) {
                        //        System.out.println("退出啦");
                        //        break;
                        //    }
                        //    System.out.println("继续匹配下一条记录");
                        //    continue;
                        //}
                    }
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
                    System.out.println("全匹配完,也没匹配上" + sum + "差额:" + target.subtract(sum));
                    // 清空list
                    personList.clear();
                    return personList;
                }
            }
        }
        return personList;
    }

    private static void testData(List<String> input) {

        //List<String> resultList = TestPro.handler(input, "5.00", true);
        //
        //List<String> list2 = TestPro.handler(resultList, "4.00", false);
        //
        //List<String> list3 = TestPro.handler(resultList, "9.00", false);
        //if (list3.size() != 0 && list2.size() == list3.size()) {
        //    System.out.println("next");
        //} else {
        //    if (list3.size() == 0) {
        //        System.out.println("退出啦");
        //        return;
        //    }
        //    System.out.println("继续匹配下一条记录");
        //}
    }

    private static List<String> handler(List<String> inputList, String number, boolean sortFlag) {
        List<String> strings = inputList;
        if (sortFlag) {
            strings = TestPro.groupHandler(inputList);
        }
        String result = null;
        for (String item : strings) {
            if (!number.equals(item.substring(item.lastIndexOf("-") + 1))) {
                return strings;
            } else {
                result = item.substring(0, item.lastIndexOf("-"));
                break;
            }
        }
        if (null != result) {
            System.out.println("找到了" + result);
            Iterator<String> iterator = strings.iterator();
            while (iterator.hasNext()) {
                String temp = iterator.next();
                String idStr = temp.substring(0, temp.lastIndexOf("-"));
                String[] strArr = result.split("-");
                for (int i = 0; i < strArr.length; i++) {
                    if (idStr.contains(strArr[i])) {
                        //System.out.println(idStr);
                        iterator.remove();
                        break;
                    }
                }
            }
        } else {
            System.out.println("未找到任何匹配项目");
        }
        return strings;
    }

    /**
     * 排列组合算法
     *
     * @param params
     * @return
     */
    public static List<String> groupHandler(List<String> params) {
        // String[] mn=new String[] {"1-4","2-3","3-2", "4-1.1","5-2.9","6-1","7-3"};

        List<String> newlist = new ArrayList<String>();
        List<String> latelist = new ArrayList<String>();
        latelist.add(params.get(0));
        //1,2,3,4,5,7,11,13,20
        List<String> tmplist = new ArrayList<String>();

        List<String> metalist = new ArrayList<String>();
        for (int i = 0; i <= params.size() - 1; i++) {
            BigDecimal tmp = BigDecimal.ZERO;
            String[] mn2 = params.get(i).split("-");
            List<String> tmplist1 = new ArrayList<String>();//用来保存中间计算的结果
            metalist.add(params.get(i));//用来保存原始数据参与过计算的
            tmplist.addAll(tmplist1);
            if (i == 0) {
                continue;
            } else {
                List<String> ttlist = new ArrayList<String>();
                for (String strs : tmplist) {//先与加和之后的数据进行计算
                    String[] arystrs = strs.split("-");
                    tmp = new BigDecimal(mn2[mn2.length - 1]).add(new BigDecimal(arystrs[arystrs.length - 1])).setScale(2);
                    //+ Double.parseDouble();
                    StringBuilder sbd1 = new StringBuilder();
                    for (int k = 0; k < mn2.length - 1; k++) {
                        sbd1.append(mn2[k]);
                        if (k != mn2.length - 2) {
                            sbd1.append("-");
                        }
                    }
                    StringBuilder sbd2 = new StringBuilder();
                    for (int k = 0; k < arystrs.length - 1; k++) {
                        sbd2.append(arystrs[k]);
                        if (k != arystrs.length - 2) {
                            sbd2.append("-");
                        }
                    }
                    sbd1.append("-").append(sbd2.toString()).append("-").append(String.valueOf(tmp));
                    tmplist1.add(sbd1.toString());
                    newlist.add(sbd1.toString());
                    ttlist.add(sbd1.toString());
                }
                for (String ss : metalist) {//与元数据进行加和
                    String[] ssary = ss.split("-");
                    if (!ss.substring(0, ss.lastIndexOf("-")).equals(params.get(i).substring(0, params.get(i).lastIndexOf("-")))) {
                        tmp = new BigDecimal(mn2[mn2.length - 1]).add(new BigDecimal(ssary[ssary.length - 1])).setScale(2);
                        StringBuilder sbd3 = new StringBuilder();
                        for (int k = 0; k < ssary.length - 1; k++) {
                            sbd3.append(ssary[k]);
                            if (k != ssary.length - 2) {
                                sbd3.append("-");
                            }
                        }
                        StringBuilder sbd4 = new StringBuilder();
                        for (int k = 0; k < mn2.length - 1; k++) {
                            sbd4.append(mn2[k]);
                            if (k != ssary.length - 2) {
                                sbd4.append("-");
                            }
                        }
                        sbd3.append("-").append(sbd4.toString()).append("-").append(String.valueOf(tmp));
                        tmplist1.add(sbd3.toString());
                        newlist.add(sbd3.toString());
                        ttlist.add(sbd3.toString());
                    }
                }
                tmplist.addAll(ttlist);
            }
        }

        return newlist;
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

