package com.kpmg;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Echo
 * @create 2018-05-24
 * @desc
 */
public class TestPro {
    //int a = bigdemical.compareTo(bigdemical2)
    //a = -1,表示bigdemical小于bigdemical2；
    //a = 0,表示bigdemical等于bigdemical2；
    //a = 1,表示bigdemical大于bigdemical2；

//    public static void main(String[] args) {
//        List<Person> ourList = new ArrayList<>();
//        Person person1 = new Person("1", new BigDecimal("120"));
//        Person person2 = new Person("2", new BigDecimal("110"));
//        Person person3 = new Person("3", new BigDecimal("60"));
//        Person person4 = new Person("4", new BigDecimal("50"));
//        Person person5 = new Person("5", new BigDecimal("10"));
//
//        ourList.add(person1);
//        ourList.add(person2);
//        ourList.add(person3);
//        ourList.add(person4);
//        ourList.add(person5);
//
//        List<Person> tradeList = new ArrayList<>();
//        Person person6 = new Person("6", new BigDecimal("160"));
//        // Person person7 = new Person("7", new BigDecimal("90"));
//
//        tradeList.add(person6);
//        // tradeList.add(person7);
//
//        if (tradeList.size() == 0) {
//            for (Person item : ourList) {
//                System.out.println("单边记账: id " + item.getId() + " value =>" + item.getValue());
//            }
//        }
//
//        if (ourList.size() == 1) {
//            // 交易双方各一条元素
//            if (ourList.size() == tradeList.size()) {
//                BigDecimal our = ourList.get(0).getValue();
//                BigDecimal trade = tradeList.get(0).getValue();
//                int result = our.compareTo(trade);
//                if (result == 0) {
//                    System.out.println("相等抵消" + ourList.get(0).getId() + ourList.get(0).getValue());
//                } else if (result == -1) {
//                    System.out.println("资产抵小" + trade.subtract(our));
//                } else {
//                    System.out.println("负债抵小" + our.subtract(trade));
//                }
//            }
//
//
//        }
//
//        if (ourList.size() > 1) {
//            if (ourList.size() > tradeList.size()) {
//                // 多对1
//                if (tradeList.size() == 1) {
//
//                    Iterator<Person> iterator = ourList.iterator();
//                    while (iterator.hasNext()) {
//                        Person next = iterator.next();
//                        if (next.getValue().compareTo(tradeList.get(0).getValue()) == 0) {
//                            iterator.remove();
//                            System.out.println("相等抵消" + next.getId() + " => " + next.getValue() + "交易对手: " + tradeList.get(0).getId() + " => " + tradeList.get(0).getValue());
//                            tradeList.clear();
//                            return;
//                        }
//                    }
//
//                    Iterator<Person> iterator2 = ourList.iterator();
//                    while (iterator2.hasNext()) {
//                        Person next = iterator2.next();
//                        // 负债方有一条记录且与资产方的一条相匹配
//                        if (next.getValue().compareTo(tradeList.get(0).getValue()) == 0) {
//                            System.out.println("相等抵消" + next.getId() + " => " + next.getValue() + "交易对手: " + tradeList.get(0).getId() + " => " + tradeList.get(0).getValue());
//                            iterator2.remove();
//                        } else if (next.getValue().compareTo(tradeList.get(0).getValue()) == 1) {
//                            // 资产方大
//                            System.out.println("负债抵小" + "差额:" + next.getValue().subtract(tradeList.get(0).getValue()));
//                            iterator2.remove();
//                            break;
//                        } else {
//                            System.out.println("===多对一匹配==");
//                            ourList = cacl(ourList, tradeList.get(0).getValue());
//                            break;
//                        }
//                    }
//                    // 未匹配的做单边记账
//                    for (Person item : ourList) {
//                        System.out.println("单边记账:" + item.getId() + " => " + item.getValue());
//                    }
//                }
//                // 多对多
//                else {
//                    System.out.println("多对多, 资产方多于负债");
//                }
//            } else if (ourList.size() == tradeList.size()) {
//                System.out.println("多对多, 个数一样");
//            } else {
//                System.out.println("负债方个数多");
//            }
//        }
//
//
//    }

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

    public static void main(String[] args) {

        List<String> strings = TestPro.groupHandler(new String[]{"1-4", "2-3", "3-2", "4-1.1", "5-2.9", "6-1", "7-3"});
        String num = "5.00";
        String result = "";
        for (String item : strings) {
            if (num.equals(item.substring(item.lastIndexOf("-") + 1))) {
                result = item.substring(0, item.lastIndexOf("-"));
                break;
            }
        }
        if (!result.equals("")) {
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
        }

    }

    /**
     * 排列组合算法
     *
     * @param params
     * @return
     */
    public static List<String> groupHandler(String[] params) {
        // String[] mn=new String[] {"1-4","2-3","3-2", "4-1.1","5-2.9","6-1","7-3"};

        Map<String, List<String>> map = new HashMap<String, List<String>>();
        List<String> newlist = new ArrayList<String>();
        List<String> latelist = new ArrayList<String>();
        latelist.add(params[0]);
        //1,2,3,4,5,7,11,13,20
        List<String> tmplist = new ArrayList<String>();

        List<String> metalist = new ArrayList<String>();
        for (int i = 0; i <= params.length - 1; i++) {
            BigDecimal tmp = BigDecimal.ZERO;
            String[] mn2 = params[i].split("-");
            List<String> tmplist1 = new ArrayList<String>();//用来保存中间计算的结果
            metalist.add(params[i]);//用来保存原始数据参与过计算的
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
                    if (!ss.substring(0, ss.lastIndexOf("-")).equals(params[i].substring(0, params[i].lastIndexOf("-")))) {
                        tmp = new BigDecimal(mn2[mn2.length - 1]).add(new BigDecimal(ssary[ssary.length - 1])).setScale(2);
                        //+ Double.parseDouble(ssary[ssary.length - 1]);

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
    
     /**
     *
     * @param treeMap
     * @param nearNum 需要接近的值
     */
    public static void caclNearNum(Map<BigDecimal, List<String>> treeMap, BigDecimal nearNum) {

        BigDecimal firstNum = (BigDecimal) (treeMap.keySet()).toArray()[0];

        // 差值实始化
        BigDecimal diffNum = firstNum.subtract(nearNum).abs();
        // 最终结果
        BigDecimal result = null;
        List<String> matchId = new ArrayList<>();
        for (Map.Entry<BigDecimal, List<String>> entry : treeMap.entrySet()) {
            BigDecimal diffNumTemp = entry.getKey().subtract(nearNum).abs();
            if (diffNumTemp.compareTo(diffNum) == -1) {
                diffNum = diffNumTemp;
                result = entry.getKey();
                matchId = entry.getValue();
            }
        }

        if (result == null) {
            result = firstNum;
            matchId = treeMap.get(result);
        }

        System.out.println(result.toString() + " id " + matchId.get(0));

    }
    
}
