package test;

public class Pair<K, V> {
    public K k;
    public V v;

    public Pair(K k, V v) {
        this.k = k;
        this.v = v;
    }

    public V getValue() {
        return v;
    }

    public K getKey() {
        return k;
    }
}
