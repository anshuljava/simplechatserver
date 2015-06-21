package com.anshul.simplechatserver;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserService {
  
  private final static UserService userService = new UserService();
  
  private Map<String, User> usersMap = new HashMap<String, User>();
  private long nextUserId = 5;
  
  private UserService() {  
    // TODO: loads users from database; or has other methods to look up users from the database
  }
  
  public static UserService get() { return userService; }

  public long authenticate(String userEmail, String passwordHash) {
    if (!usersMap.containsKey(userEmail)) {
      User u = new User(nextUserId++, userEmail, userEmail, passwordHash);
      u.setLastSeen(new Date());
      usersMap.put(userEmail, u);
    }
    
    return usersMap.get(userEmail).getUserId();
  }

  public boolean isAContact(long userId, long userId2) {
    // TODO: should maintain contact lists per user.
    return true;
  }
  
  // TODO: maintain a contact list per user; for now, all users are contacts.
  public List<User> getContactsListForUser(long userId) {
    List<User> usersList = new ArrayList<User>();
    usersList.addAll(usersMap.values());
    return usersList;
  }
  
  /**
   * Return a User given a user id
   * @param userId
   * @return
   */
  public User getUser(long userId) {
      return usersMap.get(userId);
  }
  
}
