package github.pitbox46.performanceenhancedmobs.misc;

import java.util.LinkedHashMap;
import java.util.Map;

public class CacheMap<K,V> extends LinkedHashMap<K, V> {
    /**
     * If there are more entries than this number, the oldest accessed entries will be deleted until capacity is reached
     */
    protected final int maxCapacity;

    public CacheMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor, true);
        this.maxCapacity = initialCapacity;
    }

    public CacheMap(int initialCapacity) {
        this(initialCapacity, 0.75F);
    }

    public CacheMap() {
        this(512);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxCapacity;
    }
}
