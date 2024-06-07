package com.nestnav.mobile.concurrency;

public class PrioritizedTask implements Runnable, Comparable<PrioritizedTask> {
    private final Runnable task;
    private final Priority priority;

    public PrioritizedTask(Runnable task, Priority priority) {
        this.task = task;
        this.priority = priority;
    }

    @Override
    public void run() {
        this.task.run();
    }

    @Override
    public int compareTo(PrioritizedTask other) {
        return Integer.compare(other.priority.getValue(), this.priority.getValue());
    }

    public Priority getPriority() {
        return this.priority;
    }
}
