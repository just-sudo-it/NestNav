package com.nestnav.mobile.service.network;

import com.nestnav.mobile.concurrency.ThreadPool;

import com.nestnav.mobile.concurrency.PrioritizedTask;
import com.nestnav.mobile.concurrency.Priority;

import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NetworkService {
    private static final Logger logger = Logger.getLogger(NetworkService.class.getName());
    private final ThreadPool threadPool;
    private final ConnectionPool connectionPool;

    public NetworkService(ThreadPool threadPool, String serverAddress, int serverPort, int maxConnections) {
        this.threadPool = threadPool;
        this.connectionPool = new ConnectionPool(serverAddress, serverPort, maxConnections);
    }

    public void sendRequest(Object request, ResponseHandler handler) {
        threadPool.execute(new PrioritizedTask(() -> {
            Socket socket = null;
            try {
                socket = connectionPool.acquire();
                logger.log(Level.INFO, "Socket acquired: " + socket);

                NetworkClient client = new NetworkClient(socket);
                Object response = client.sendRequest(request);
                logger.log(Level.INFO, "Response received: " + response);

                handler.handleResponse(response);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Network request failed", e);
                handler.handleError(e);
            } finally {
                if (socket != null) {
                    try {
                        connectionPool.release(socket);
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Failed to release socket", e);
                    }
                }
            }
        }, Priority.MEDIUM));
    }

    public void shutdown() {
        logger.log(Level.INFO, "Shutting down NetworkService...");
        threadPool.shutdown();
        connectionPool.shutdown();
    }

    public interface ResponseHandler {
        void handleResponse(Object response);
        void handleError(Exception exception);
    }
}
