package Concurrency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class ThreadSafeList<T> {
    private final List<T> list = new ArrayList<>();
    private final Object lock = new Object();

    public void add(T item) {
        synchronized (lock) {
            list.add(item);
        }
    }

    public void addAll(Collection<? extends T> items) {
        synchronized (lock) {
            list.addAll(items);
        }
    }

    public T get(int index) {
        synchronized (lock) {
            return list.get(index);
        }
    }

    public int size() {
        synchronized (lock) {
            return list.size();
        }
    }

    public List<T> getAll() {
        synchronized (lock) {
            return new ArrayList<>(list); // Return shallow copy to avoid concurrency issues
        }
    }

    public void removeAll(Collection<? extends T> items) {
        synchronized (lock) {
            list.removeAll(items);
        }
    }
    public Stream<T> stream() {
        synchronized (list) {
            return new ArrayList<>(list).stream();  // Make a copy for safe streaming outside the synchronized block
        }
    }
}
