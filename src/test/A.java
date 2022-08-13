package test;

import java.util.HashMap;
import java.util.Map;

public class A {
    public static void main(String[] args) {
        Map<String,String> map1=new HashMap<>();
        Map<String,String> map2=new HashMap<>();

        map1.put("map1","map1");
        map2.put("map2","map2");
        map2.putAll(map1);
        S.s(map1);
        S.s(map2);
    }
}
