package com.yhyecho.demo;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Echo on 5/26/18.
 */
public class TestPro2 {
    public static void main(String[] args) {
        List<Person> ourList = new ArrayList<>();
        Person person1 = new Person("1", new BigDecimal("100.00")); // 7 - 100
        Person person2 = new Person("2", new BigDecimal("80.00")); // 9 6 8 80
        Person person3 = new Person("3", new BigDecimal("60.00")); // 87

        ourList.add(person1);
        ourList.add(person2);
        ourList.add(person3);

        List<Person> tradeList = new ArrayList<>();
        Person person6 = new Person("4", new BigDecimal("10")); // 2 10 +
        Person person7 = new Person("5", new BigDecimal("100")); // 1 100
        Person person8 = new Person("6", new BigDecimal("30")); // 2 30
        Person person9 = new Person("7", new BigDecimal("40")); // 2 40
        Person person10 = new Person("8", new BigDecimal("10")); // 10 + 120
        Person person11 = new Person("9", new BigDecimal("10")); //
        Person person12 = new Person("10", new BigDecimal("30")); // 90

        tradeList.add(person6);
        tradeList.add(person7);
        tradeList.add(person8);
        tradeList.add(person9);
        tradeList.add(person10);
        tradeList.add(person11);
        tradeList.add(person12);

        if (tradeList.size() == 0) {
            for (Person item : ourList) {
                System.out.println("单边记账id  " + item.getId() + "money " + item.getValue());
            }
            return;
        }

        if (tradeList.size() == 1) {
            System.out.println("抵小id  " + ourList.get(0).getId() + "money 差额 " + ourList.get(0).getValue().subtract(tradeList.get(0).getValue()).toString());
            for (int i = 1; i < ourList.size(); i++) {
                System.out.println("单边记账id  " + ourList.get(i).getId() + "money " + ourList.get(i).getValue());
            }
            return;
        }

        // 一对一相等匹配
        cal1(ourList, tradeList);
        // 一对多相等匹配
        cal2(ourList, tradeList);
        // 一对多抵小匹配
        cal3(ourList, tradeList);
    }

    // 一对一相等匹配
    public static Map<String, List<Person>> cal1(List<Person> ourList, List<Person> tradeList) {
        Iterator<Person> ourIterator = ourList.iterator();
        while (ourIterator.hasNext()) {
            Person ourObj = ourIterator.next();
            Iterator<Person> tradeIterator = tradeList.iterator();
            while (tradeIterator.hasNext()) {
                Person tradeObj = tradeIterator.next();
                if (ourObj.getValue().compareTo(tradeObj.getValue()) == 0) {
                    ourIterator.remove();
                    tradeIterator.remove();
                    System.out.println("一对一 本方id: " + ourObj.getId() + "对方id:" + tradeObj.getId());
                    break;
                }
            }
        }

        Map<String, List<Person>> map = new HashMap<>();
        map.put("ourList", ourList);
        map.put("tradeList", tradeList);

        return map;
    }

    // 一对多组合相等匹配
    public static Map<String, List<Person>> cal2(List<Person> ourList, List<Person> tradeList) {
        List<String> orderList = groupHandler(tradeList);
        Map<BigDecimal, List<String>> treeMap = new TreeMap<>(new Comparator<BigDecimal>() {
            @Override
            public int compare(BigDecimal o1, BigDecimal o2) {
                return o2.compareTo(o1);
            }
        });
        for (String item : orderList) {
            BigDecimal key = new BigDecimal(item.substring(item.lastIndexOf("-") + 1));
            String value = item.substring(0, item.lastIndexOf("-"));
            if (!treeMap.containsKey(key)) {
                treeMap.put(key, new ArrayList<>());
            }
            treeMap.get(key).add(value);
        }

        Iterator<Person> ourIterator = ourList.iterator();
        while (ourIterator.hasNext()) {
            Person ourObj = ourIterator.next();
            Iterator<BigDecimal> mapIterator1 = treeMap.keySet().iterator();
            while (mapIterator1.hasNext()) {
                {
                    BigDecimal temp = mapIterator1.next();

                    if (ourObj.getValue().compareTo(temp) == 0) {
                        System.out.println("本方id: " + ourObj.getId() + "对方:id " + treeMap.get(temp).get(0));
                        // 移除本方相关id
                        ourIterator.remove();

                        String[] tradeIdArr = treeMap.get(temp).get(0).split("-");

                        // 移除对方
                        for (int i = 0; i < tradeIdArr.length; i++) {
                            // 移除treemap中匹配的
                            List<String> valueList = treeMap.get(temp);

                            Iterator<String> valueIterator = valueList.iterator();
                            while (valueIterator.hasNext()) {
                                String tempValue = valueIterator.next();
                                if (tempValue.contains(tradeIdArr[i])) {
                                    valueIterator.remove();
                                }
                            }
                            Map<BigDecimal, List<String>> newMap = new TreeMap<>();
                            newMap.put(ourObj.getValue(), valueList);
                            treeMap = newMap;
                            // 移除对方id
                            Iterator<Person> tradeIterator = tradeList.iterator();
                            while (tradeIterator.hasNext()) {
                                Person tradeObj = tradeIterator.next();
                                if (tradeObj.getId().equals(tradeIdArr[i])) {
                                    tradeIterator.remove();
                                }
                            }
                        }
                    }
                }
            }
        }
        Map<String, List<Person>> map = new HashMap<>();
        map.put("ourList", ourList);
        map.put("tradeList", tradeList);

        return map;
    }

    // 一对多抵消匹配
    public static void cal3(List<Person> ourList, List<Person> tradeList) {
        List<String> orderList = groupHandler(tradeList);
        Map<BigDecimal, List<String>> treeMap = new TreeMap<>(new Comparator<BigDecimal>() {
            @Override
            public int compare(BigDecimal o1, BigDecimal o2) {
                return o2.compareTo(o1);
            }
        });
        for (String item : orderList) {
            BigDecimal key = new BigDecimal(item.substring(item.lastIndexOf("-") + 1));
            String value = item.substring(0, item.lastIndexOf("-"));
            if (!treeMap.containsKey(key)) {
                treeMap.put(key, new ArrayList<>());
            }
            treeMap.get(key).add(value);
        }

        Iterator<Person> ourIterator = ourList.iterator();

        while (ourIterator.hasNext()) {

            Person ourPerson = ourIterator.next();
            Map<String, Object> resultMap = calNearNum(treeMap, ourPerson.getValue());

            BigDecimal money = (BigDecimal) resultMap.get("money");
            List<String> matchList = (List<String>) resultMap.get("matchId");
            if (matchList.size() == 0) {
                continue;
            }
            String matchId = matchList.get(0);
            String[] matchIdArr = matchId.split("-");
            System.out.println("借方: " + ourPerson.getId() + " 金额 " + ourPerson.getValue() + " 贷方 " + matchId + "  金额" + money + " 差额 " + ourPerson.getValue().subtract(money).toString());
            ourIterator.remove();
            if (tradeList.size() == 0) {
                break;
            }
            Iterator<BigDecimal> mapIterator = treeMap.keySet().iterator();

            while (mapIterator.hasNext()) {
                BigDecimal temp = mapIterator.next();
                List<String> valueList = treeMap.get(temp);
                if (valueList == null) {
                    break;
                }
                for (int i = 0; i < matchIdArr.length; i++) {
                    Iterator<String> valueIterator = valueList.iterator();
                    while (valueIterator.hasNext()) {
                        String tempValue = valueIterator.next();
                        if (tempValue.contains(matchIdArr[i])) {
                            valueIterator.remove();
                        }
                    }
                    Map<BigDecimal, List<String>> newMap = new TreeMap<>();
                    newMap.put(ourPerson.getValue(), valueList);
                    treeMap = newMap;

                    Iterator<Person> iterator = tradeList.iterator();
                    while (iterator.hasNext()) {
                        if (iterator.next().getId().equals(matchIdArr[i])) {
                            iterator.remove();
                        }
                    }
                }

            }

        }

        if (tradeList.size() != 0 && ourList.size() > 0) {
            cal3(ourList, tradeList);
        } else {
            for (Person item : ourList) {
                System.out.println("借方单边记账id  " + item.getId() + "money " + item.getValue());
            }

            for (Person item : tradeList) {
                System.out.println("贷方单边记账id  " + item.getId() + "money " + item.getValue());
            }
        }

    }

    /**
     * @param treeMap
     * @param nearNum 求最接近的值
     */
    public static Map<String, Object> calNearNum(Map<BigDecimal, List<String>> treeMap, BigDecimal nearNum) {

        // BigDecimal firstNum = (BigDecimal) (treeMap.keySet()).toArray()[0];
        BigDecimal firstNum = treeMap.keySet().iterator().next();

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

        Map<String, Object> map = new HashMap<>();
        map.put("money", result);
        map.put("matchId", matchId);
        return map;
    }

    /**
     * 排列组合算法
     *
     * @return
     */
    public static List<String> groupHandler(List<Person> tradeList) {
        // id-金额
        // String[] mn=new String[] {"1-4","2-3","3-2", "4-1.1","5-2.9","6-1","7-3"};
        List<String> params = new ArrayList<>();
        for (Person item : tradeList) {
            params.add(item.getId() + "-" + item.getValue());
        }

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
}
