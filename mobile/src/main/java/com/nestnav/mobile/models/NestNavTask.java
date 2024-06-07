package com.nestnav.mobile.models;

import java.io.Serializable;

public class NestNavTask implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Integer taskId;
    private final Serializable data;  // data field

    public NestNavTask(Integer taskId, Serializable data) {
        this.taskId = taskId;
        this.data = data;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public Serializable getData() {
        return data;
    }
}
