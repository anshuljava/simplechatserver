package com.anshul.simplechatserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SimpleChatServer implements Runnable {

    private static final int LISTEN_PORT = 3000;

    // TODO - fix the auth token could be encrypted as well
    private long nextAuthToken = 5;

    private ServerSocket serverSocket;
    private HashMap<Long, List<ClientHandler>> registeredClients;

    public SimpleChatServer(InetAddress address, int port) throws IOException {
	this.serverSocket = new ServerSocket();
	this.serverSocket.bind(new InetSocketAddress(address, port));
	this.registeredClients = new HashMap<Long, List<ClientHandler>>();
    }

    @Override
    public void run() {
	while (true) {
	    try {
		Socket clientSocket = this.serverSocket.accept();
		new Thread(new ClientHandler(this, clientSocket)).start();
	    } catch (IOException ex) {
		// TODO: handle exceptions
		ex.printStackTrace();
	    }
	}
    }

    public synchronized long registerClient(long userId, ClientHandler handler) {
	long authToken = nextAuthToken++;
	List<ClientHandler> handlers = registeredClients.get(userId);
	if (handlers == null) {
	    registeredClients.put(userId,
		    handlers = new ArrayList<ClientHandler>());
	}
	handlers.add(handler);
	return authToken;
    }

    public void deregisterClient(long userId, ClientHandler handler) {
	List<ClientHandler> handlers = registeredClients.get(userId);
	handlers.remove(handler);
    }

    public void sendMessage(ClientHandler clientHandler, long userIdFrom,
	    long userIdTo, Message message) throws IOException {
	// TODO: handle offline case
	// send to this user's other chat clients
	for (ClientHandler handler : registeredClients.get(userIdFrom)) {
	    if (handler == clientHandler)
		continue;
	    handler.send(message);
	}

	// send to the recipients chat clients
	for (ClientHandler handler : registeredClients.get(userIdTo)) {
	    handler.send(message);
	}
    }

    public static void main(String[] args) throws IOException {
	new Thread(new SimpleChatServer(null, LISTEN_PORT)).start();
    }

}
