package test.pattern;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * created by zhaoyuntao
 * on 23/03/2022
 * description:
 */
public class ValueSafeTransfer {
    public static long value(Long value) {
        return value(value, 0L);
    }

    public static long value(Long value, long defaultValue) {
        return value == null ? defaultValue : value;
    }

    public static int value(Integer value) {
        return value(value, 0);
    }

    public static int value(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    public static double value(Double value) {
        return value(value, 0d);
    }

    public static double value(Double value, double defaultValue) {
        return value == null ? defaultValue : value;
    }

    public static float value(Float value) {
        return value(value, 0f);
    }

    public static float value(Float value, float defaultValue) {
        return value == null ? defaultValue : value;
    }

    public static short value(Short value) {
        short defaultValue = 0;
        return value(value, defaultValue);
    }

    public static short value(Short value, short defaultValue) {
        return value == null ? defaultValue : value;
    }

    public static boolean value(Boolean value) {
        return value(value, false);
    }

    public static boolean value(Boolean value, boolean defaultValue) {
        return value == null ? defaultValue : value;
    }

    public static long longValue(String value) {
        return longValue(value, 0L);
    }

    public static long longValue(String value, long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignore) {
            return defaultValue;
        }
    }

    public static float floatValue(String value) {
        return floatValue(value, 0);
    }

    public static float floatValue(String value, float defaultValue) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException ignore) {
            return defaultValue;
        }
    }

    public static String stringValue(Object o) {
        return stringValue(o, "null");
    }

    public static String stringValue(Object o, String defaultValue) {
        return o == null ? defaultValue : String.valueOf(o);
    }

    public static List<String> stringListValue(List<Long> longs) {
        if (longs == null) {
            return null;
        }
        List<String> list = new ArrayList<>(longs.size());
        for (Long longValue : longs) {
            list.add(stringValue(longValue));
        }
        return list;
    }

    public static List<Long> longListValue(List<String> strings) {
        if (strings == null) {
            return null;
        }
        List<Long> list = new ArrayList<>(strings.size());
        for (String string : strings) {
            list.add(longValue(string));
        }
        return list;
    }

    public static <T1, T2> List<T2> transformList(List<T1> originList, Transfer<T1, T2> transformer) {
        if (originList == null) {
            return null;
        }
        List<T2> list = new ArrayList<>(originList.size());
        int i = 0;
        for (T1 t1 : originList) {
            if (t1 == null) {
                continue;
            }
            T2 t2 = transformer.transform(i++, t1);
            if (t2 != null) {
                list.add(t2);
            }
        }
        return list;
    }

    public static <T extends Cloneable> List<T> clone(Collection<T> list, ElementIterator<T> iterator) {
        if (list == null) {
            return null;
        }
        List<T> cloneList = new ArrayList<>(list.size());
        int i = 0;
        for (T t : list) {
            cloneList.add(iterator.element(i++, t));
        }
        return cloneList;
    }

    public static <T> T iterate(Collection<T> list, ElementIterator<T> iterator) {
        if (list == null) {
            return null;
        }
        int i = 0;
        for (T t : list) {
            T tResult = iterator.element(i++, t);
            if (tResult != null) {
                return tResult;
            }
        }
        return null;
    }

    public static <T> List<T> singletonList(T t) {
        return new ArrayList<>(Collections.singletonList(t));
    }


    @SuppressWarnings("unchecked")
    public static <T> T from(String classPath) {
        try {
            Class<?> aClass = Class.forName(classPath);
            return (T) aClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("classPath not found:" + classPath);
        }
    }

    public static int intValue(String numberString) {
        try {
            return Integer.parseInt(numberString);
        } catch (Exception e) {
            return 0;
        }
    }

    public static boolean equals(List<?> list1, List<?> list2) {
        if (list1 == null && list2 == null) {
            return true;
        } else if (list1 != null && list2 != null) {
            if (list1.size() != list2.size()) {
                return false;
            }
            for (int i = 0; i < list1.size(); i++) {
                Object o1 = list1.get(i);
                Object o2 = list2.get(i);
                if (o1 == o2) {
                    continue;
                }
                if (o1 != null && o2 != null) {
                    if (!o1.equals(o2)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static <T> boolean equals(List<T> list1, List<T> list2, Comparator<T> comparator) {
        if (list1 == null && list2 == null) {
            return true;
        } else if (list1 != null && list2 != null) {
            if (list1.size() != list2.size()) {
                return false;
            }
            for (int i = 0; i < list1.size(); i++) {
                T o1 = list1.get(i);
                T o2 = list2.get(i);
                if (o1 == o2) {
                    continue;
                }
                if (o1 != null && o2 != null) {
                    if (!comparator.compare(o1, o2)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public interface Transfer<T1, T2> {

        T2 transform(int position, T1 t);
    }

    public interface ElementIterator<T> {

        T element(int position, T t);
    }

    public interface Comparator<T> {
        boolean compare(T t1, T t2);
    }
}
