package com.nestnav.mobile.service.network;


import com.nestnav.mobile.concurrency.PrioritizedTask;
import com.nestnav.mobile.concurrency.Priority;
import com.nestnav.mobile.concurrency.ThreadPool;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class NetworkClient {
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private final ThreadPool threadPool;

    public NetworkClient(ThreadPool threadPool) {
        this.threadPool = threadPool;
    }

    public void connect(String serverAddress, int port) throws Exception {
        socket = new Socket(serverAddress, port);
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        inputStream = new ObjectInputStream(socket.getInputStream());
    }

    public void send(Object data) throws Exception {
        threadPool.execute(new PrioritizedTask(() -> {
            try {
                outputStream.writeObject(data);
                outputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();  // Proper error handling should be implemented
            }
        }, Priority.HIGH)); // Priority level can be adjusted based on your requirement
    }

    public Object receive() throws Exception {
        return inputStream.readObject();
    }

    public void close() throws Exception {
        inputStream.close();
        outputStream.close();
        socket.close();
    }
}
