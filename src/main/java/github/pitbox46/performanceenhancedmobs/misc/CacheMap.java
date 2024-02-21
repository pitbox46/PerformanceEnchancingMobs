package github.pitbox46.performanceenhancedmobs.misc;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class CacheMap<K,V> extends LinkedHashMap<K, V> {
    /**
     * If there are more entries than this number, the oldest entries will be deleted until capacity is reached
     */
    protected int maxCapacity = Integer.MAX_VALUE;

    public CacheMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public CacheMap(int initialCapacity) {
        super(initialCapacity);
    }

    public CacheMap() {
        super();
        maxCapacity = 512;
    }

    @Override
    public V get(Object key) {
        V value = super.remove(key);

        if (value != null) {
            put((K) key, value);
        }
        return value;
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxCapacity;
    }
}
