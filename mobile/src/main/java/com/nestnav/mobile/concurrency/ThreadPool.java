package com.nestnav.mobile.concurrency;

import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadPool {
    private final ThreadSafeList<Thread> threads;
    private final SynchronizedPriorityQueue<PrioritizedTask> taskQueue;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final int corePoolSize;
    private final int maxPoolSize;

    public ThreadPool(int corePoolSize, int maxPoolSize) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.threads = new ThreadSafeList<>();
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
                threads.remove(Thread.currentThread());
            }
        });
        threads.add(thread);
        thread.start();
    }

    public void execute(PrioritizedTask task) {
        if (!isRunning.get()) {
            throw new IllegalStateException("ThreadPool is shutting down.");
        }
        synchronized (threads) {
            taskQueue.add(task);
            if (taskQueue.size() > threads.size() && threads.size() < maxPoolSize) {
                createAndStartThread(); // Dynamically add threads if the workload is high and maxPoolSize not reached
            }
        }
    }

    public void shutdown() {
        isRunning.set(false);
        threads.stream().forEach(Thread::interrupt);
        threads.stream().forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
}
