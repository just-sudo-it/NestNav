package Concurrency;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadPool {
    private final List<Thread> threads;
    private final SynchronizedPriorityQueue<PrioritizedTask> taskQueue;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final int corePoolSize;
    private final int maxPoolSize;
    private final Object lock = new Object();

    public ThreadPool(int corePoolSize, int maxPoolSize) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.threads = new ArrayList<>();
        this.taskQueue = new SynchronizedPriorityQueue<>();
        for (int i = 0; i < corePoolSize; i++) {
            createAndStartThread();
        }
    }

    private void createAndStartThread() {
        Thread thread = new Thread(() -> {
            try {
                while (isRunning.get() || !taskQueue.isEmpty()) {
                    PrioritizedTask task = taskQueue.take();
                    if (task != null) {
                        task.run();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Properly handle interruption
            } finally {
                synchronized (lock) {
                    threads.remove(Thread.currentThread());
                }
            }
        });
        synchronized (lock) {
            threads.add(thread);
            thread.start();
        }
    }

    public void execute(PrioritizedTask task) {
        if (!isRunning.get()) {
            throw new IllegalStateException("ThreadPool is shutting down.");
        }
        synchronized (lock) {
            taskQueue.add(task);
            if (taskQueue.size() > threads.size() && threads.size() < maxPoolSize) {
                createAndStartThread(); // Dynamically add threads if the workload is high and maxPoolSize not reached
            }
        }
    }

    public void shutdown() {
        isRunning.set(false);
        synchronized (lock) {
            threads.forEach(Thread::interrupt);
            threads.forEach(thread -> {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }
}
