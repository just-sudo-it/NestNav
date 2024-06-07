package com.nestnav.mobile.service.network;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NetworkClient {
    private static final Logger logger = Logger.getLogger(NetworkClient.class.getName());
    private final Socket socket;

    public NetworkClient(Socket socket) {
        this.socket = socket;
    }

    public Object sendRequest(Object requestData) throws Exception {
        logger.log(Level.INFO, "Sending request...");
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            out.writeObject(requestData);
            out.flush();
            Object response = in.readObject();
            logger.log(Level.INFO, "Received response.");
            return response;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during request/response cycle", e);
            throw e;
        }
    }
}
