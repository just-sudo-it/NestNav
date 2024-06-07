package com.nestnav.mobile.service.network;

import com.nestnav.mobile.concurrency.ThreadSafeList;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionPool {
    private static final Logger logger = Logger.getLogger(ConnectionPool.class.getName());
    private final ThreadSafeList<Socket> pool;
    private final String serverAddress;
    private final int serverPort;
    private final int maxConnections;

    public ConnectionPool(String serverAddress, int serverPort, int maxConnections) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.maxConnections = maxConnections;
        this.pool = new ThreadSafeList<>();
    }

    public Socket acquire() throws IOException {
        logger.log(Level.INFO, "Acquiring a socket...");
        synchronized (pool) {
            if (!pool.isEmpty()) {
                Socket socket = pool.poll();
                if (socket != null && socket.isConnected() && !socket.isClosed()) {
                    logger.log(Level.INFO, "Reusing an existing socket.");
                    return socket;
                }
            }
            logger.log(Level.INFO, "Creating a new socket.");
            return createSocket();
        }
    }

    public void release(Socket socket) {
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            synchronized (pool) {
                if (pool.size() < maxConnections) {
                    pool.add(socket);
                    logger.log(Level.INFO, "Socket released back to pool.");
                } else {
                    closeSocket(socket);
                    logger.log(Level.INFO, "Socket closed as the pool is full.");
                }
            }
        } else {
            closeSocket(socket);
        }
    }

    private Socket createSocket() throws IOException {
        return new Socket(serverAddress, serverPort);
    }

    private void closeSocket(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to close socket", e);
        }
    }

    public void shutdown() {
        synchronized (pool) {
            for (Socket socket : pool.getAll()) {
                closeSocket(socket);
            }
            pool.removeAll(pool.getAll());
            logger.log(Level.INFO, "Connection pool shut down and all sockets closed.");
        }
    }
}
