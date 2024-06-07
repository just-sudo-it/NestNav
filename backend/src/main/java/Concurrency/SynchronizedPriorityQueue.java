
package Concurrency;

import java.util.PriorityQueue;
public class SynchronizedPriorityQueue<T extends Comparable<T>> {
    private final PriorityQueue<T> queue = new PriorityQueue<>();
    private final Object lock = new Object();

    public void add(T item) {
        synchronized (lock) {
            queue.add(item);
            lock.notify(); // Notify a single waiting thread
        }
    }

    public T take() throws InterruptedException {
        synchronized (lock) {
            while (queue.isEmpty()) {
                lock.wait(); //Wait for notify
            }
            return queue.poll(); //take
        }
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }
    public synchronized int size() {
        return queue.size();
    }
}
