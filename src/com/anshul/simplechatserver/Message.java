package com.anshul.simplechatserver;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Represents various messages exchanged in the system - client to server, or
 * server to client
 */
public interface Message {
    public enum Type {
	AuthenticationRequest, AuthenticationResponse, UserInfoRequest, UserInfoResponse, ContactListRequest, ContactListResponse, PresenceRequest, PresenceResponse, ChatMessageRequest, ChatMessageResponse
    };

    public Type getType();

    /**
     * Sent by the client after they first connect with the server to
     * authenticate.
     * 
     * Assumption: the user registers outside this protocol and the server has
     * access to a list of all users and their passwordHash.
     */
    public static class AuthenticationRequest implements Message, Serializable {
	String userEmail;
	String passwordHash;

	@Override
	public Type getType() {
	    return Type.AuthenticationRequest;
	}
    }

    /**
     * Sent by the server in response to an authentication request.
     * authSucceeded will be false, if the password doesn't match. An authToken
     * will be returned to validate future requests from this client.
     */
    public static class AuthenticationResponse implements Message, Serializable {
	boolean authenticationSucceeded;
	long authToken;
	long userId;

	@Override
	public Type getType() {
	    return Type.AuthenticationResponse;
	}
    }

    /**
     * Sent by the client to retrieve their contact list
     */
    public static class ContactListRequest implements Message, Serializable {
	long authToken;
	long userId;

	@Override
	public Type getType() {
	    return Type.ContactListRequest;
	}
    }

    /**
     * Response to a contact list request
     */
    public static class ContactListResponse implements Message, Serializable {
	List<Long> userIds;

	@Override
	public Type getType() {
	    return Type.ContactListResponse;
	}
    }

    /**
     * Sent by an active client periodically to report it's active status
     */
    public static class PresenceRequest implements Message, Serializable {
	long authToken;
	long userId;

	@Override
	public Type getType() {
	    return Type.PresenceRequest;
	}
    }
    
    /**
     * Response to a presence response send to all contacts for the user
     */
    public static class PresenceResponse implements Message, Serializable {
	long userId;
	Date lastSeen;

	@Override
	public Type getType() {
	    return Type.PresenceResponse;
	}
    }

    /**
     * Sent by the client to send a message to the user identified by userId.
     */
    public static class ChatMessageRequest implements Message, Serializable {
	long authToken;
	long userId; // user to send the message to
	String message;

	@Override
	public Type getType() {
	    return Type.ChatMessageRequest;
	}
    }

    /**
     * Pushed by server to all clients that need to be notified of a message.
     * This message will also go out to all other clients of the user that sent
     * the message as well.
     */
    public static class ChatMessageResponse implements Message, Serializable {
	long userId; // user who sent the message
	String message;

	@Override
	public Type getType() {
	    return Type.ChatMessageResponse;
	}
    }

    /**
     * Sent by the client to gather information related to user, usually the
     * other contacts
     */
    public static class UserInfoRequest implements Message, Serializable {
	long authToken;
	long userId;

	@Override
	public Type getType() {
	    return Type.UserInfoRequest;
	}
    }

    /**
     * Response for user info request, may contain all information that may be
     * needed on the client for the user - like name, last seen presence (more
     * can be added)
     */
    public static class UserInfoResponse implements Message, Serializable {
	long userId;
	String name;
	Date lastSeen;

	@Override
	public Type getType() {
	    return Type.UserInfoResponse;
	}
    }

}
