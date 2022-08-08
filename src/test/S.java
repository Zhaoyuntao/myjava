package test;

import java.util.Arrays;

/**
 * created by zhaoyuntao
 * on 06/09/2020
 * description:
 */
public class S {
    public static void s(Object o) {
        System.out.println(o);
    }

    public static void e(Object o) {
        System.err.println(o);
    }

    public static void e(Throwable o) {
        System.err.println(Arrays.toString(o.getStackTrace()));
    }
}
