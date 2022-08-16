package m4gshm.benchmark.rest.java.concurrency;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSet;

@RequiredArgsConstructor
public class RwLockMap<K, V> implements Map<K, V> {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<K, V> map;

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        try {
            lock.readLock().lock();
            return map.containsKey(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean containsValue(Object value) {
        try {
            lock.readLock().lock();
            return map.containsValue(value);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public V get(Object key) {
        try {
            lock.readLock().lock();
            return map.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public V put(K key, V value) {
        try {
            lock.writeLock().lock();
            return map.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public V remove(Object key) {
        try {
            lock.writeLock().lock();
            return map.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        try {
            lock.writeLock().lock();
            map.putAll(m);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void clear() {
        try {
            lock.writeLock().lock();
            map.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Set<K> keySet() {
        try {
            lock.readLock().lock();
            return unmodifiableSet(map.keySet());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Collection<V> values() {
        try {
            lock.readLock().lock();
            return unmodifiableCollection(map.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        try {
            lock.readLock().lock();
            return unmodifiableSet(map.entrySet());
        } finally {
            lock.readLock().unlock();
        }
    }
}
