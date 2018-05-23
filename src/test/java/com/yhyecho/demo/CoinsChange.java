package com.yhyecho.demo;

/**
 * Created by Echo on 5/23/18.
 */
public class CoinsChange {

    public static void main(String[] args) {
        int[] num = {5, 4, 3, 2, 1};
        int target = 9;
        cacle(num, target);
    }

    private static void cacle(int[] num, int target) {
        // 5 4 3 2 1
        int sum = num[0];
        for (int i = 1; i < num.length; i++) {
            sum = sum + num[i];
            if (sum == target) {
                System.out.println("匹配成功");
                System.out.println("位置" + i);
                break;
            } else if (sum > target) {
                int result = sum - target;
                System.out.println("位置" + i + "数值" + num[i]);
                System.out.println("大于了");
                break;
            } else {
                if (i == num.length - 1) {
                    System.out.println("一直小于" + sum);
                }
            }
        }
    }
}
