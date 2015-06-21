package com.anshul.simplechatserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Handles all client requests primarily
 * 
 */
public class ClientHandler implements Runnable {

    private SimpleChatServer chatServer;
    private Socket clientSocket;
    private ObjectOutputStream oos;

    private long authToken;
    private long userId;

    public ClientHandler(SimpleChatServer simpleChatServer, Socket clientSocket) {
	this.chatServer = simpleChatServer;
	this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
	try {
	    ObjectInputStream ois = new ObjectInputStream(
		    clientSocket.getInputStream());
	    while (true) {
		Message msg = (Message) ois.readObject();
		switch (msg.getType()) {
		case AuthenticationRequest:
		    authenticate((Message.AuthenticationRequest) msg);
		    break;
		case ChatMessageRequest:
		    sendChat((Message.ChatMessageRequest) msg);
		    break;
		case ContactListRequest:
		    sendContactList((Message.ContactListRequest) msg);
		    break;
		case PresenceRequest:
		    processPresence((Message.PresenceRequest) msg);
		    break;
		case UserInfoRequest:
		    sendUserInfo((Message.UserInfoRequest) msg);
		default:
		    break;
		}
	    }
	} catch (Exception e) {
	    chatServer.deregisterClient(this.userId, this);
	    e.printStackTrace();
	}
    }

    private void authenticate(Message.AuthenticationRequest msg)
	    throws IOException {
	long userId = UserService.get().authenticate(msg.userEmail,
		msg.passwordHash);
	Message.AuthenticationResponse response = new Message.AuthenticationResponse();
	if (userId == -1) {
	    // didn't authenticate
	    response.authenticationSucceeded = false;
	} else {
	    response.authenticationSucceeded = true;
	    response.userId = userId;
	    response.authToken = chatServer.registerClient(userId, this);
	    this.authToken = response.authToken;
	    this.userId = response.userId;
	}
	send(response);
    }

    private void sendChat(Message.ChatMessageRequest msg) throws IOException {
	if (msg.authToken != this.authToken) {
	    // TODO: handle invalid credentials
	    return;
	}
	if (!UserService.get().isAContact(this.userId, msg.userId)) {
	    // Not allowed to send to user not in contact list
	    return;
	}
	Message.ChatMessageResponse response = new Message.ChatMessageResponse();
	response.message = msg.message;
	response.userId = this.userId;
	// send the chat message to all clients for both this userId and the
	// recipient userId (skipping this client)
	chatServer.sendMessage(this, this.userId, msg.userId, response);
	updatePresence(msg.userId);
    }

    private void processPresence(Message.PresenceRequest msg) throws IOException {
	if (msg.authToken != this.authToken) {
	    // TODO: handle invalid credentials
	    return;
	}
	User u = UserService.get().getUser(msg.userId);
	u.setLastSeen(new Date());
	//	update the presence for all contacts for this user, for all clients
	List<User> contacts = UserService.get().getContactsListForUser(msg.userId);
	for (User contactId : contacts) {
	    Message.PresenceResponse response = new Message.PresenceResponse();
	    response.userId = msg.userId;
	    response.lastSeen = u.getLastSeen();
	    chatServer.sendMessage(this, this.userId, contactId.getUserId(), response);
	}
    }

    private void sendUserInfo(Message.UserInfoRequest msg) throws IOException {
	if (msg.authToken != this.authToken) {
	    // TODO: handle invalid credentials
	    return;
	}
	Message.UserInfoResponse response = new Message.UserInfoResponse();
	User u = UserService.get().getUser(msg.userId);
	response.userId = msg.userId;
	response.name = u.getUserName();
	response.lastSeen = u.getLastSeen();
	send(response);
    }

    private void sendContactList(Message.ContactListRequest msg)
	    throws IOException {
	if (msg.authToken != this.authToken) {
	    // TODO: handle invalid credentials
	    return;
	}
	Message.ContactListResponse response = new Message.ContactListResponse();
	List<Long> contactUserIds = new ArrayList<>();
	for (User u : UserService.get().getContactsListForUser(msg.userId)) {
	    contactUserIds.add(u.getUserId());
	}
	response.userIds = contactUserIds;
	send(response);
	updatePresence(msg.userId);
    }

    private void updatePresence(long userId) {
	User u = UserService.get().getUser(userId);
	u.setLastSeen(new Date());
    }

    public synchronized void send(Message msg) throws IOException {
	if (this.oos == null) {
	    this.oos = new ObjectOutputStream(clientSocket.getOutputStream());
	}
	this.oos.writeObject(msg);
	this.oos.flush();
    }

}
