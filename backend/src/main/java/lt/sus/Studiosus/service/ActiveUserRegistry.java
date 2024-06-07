package lt.sus.Studiosus.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@Getter
public class ActiveUserRegistry {

  private final Map<String, Map<String, String>> projectUsers = new ConcurrentHashMap<>();

  public void addUser(String projectId, String username) {
    Map<String, String> users = projectUsers.getOrDefault(projectId, new ConcurrentHashMap<>());
    users.put(username, getRandomColor());
    projectUsers.put(projectId, users);
  }

  public void removeUser(String projectId, String username) {
    Map<String, String> users = projectUsers.get(projectId);
    if (users != null) {
      users.remove(username);
      if (users.isEmpty()) {
        projectUsers.remove(projectId);
      } else {
        projectUsers.put(projectId, users);
      }
    }
  }

  public Map<String, String> getUsers(String projectId) {
    return projectUsers.getOrDefault(projectId, new ConcurrentHashMap<>());
  }

  public static String getRandomColor() {
    Random random = new Random();
    String[] letters = "0123456789ABCDEF".split("");
    StringBuilder color = new StringBuilder("#");
    for (int i = 0; i < 6; i++) {
      color.append(letters[random.nextInt(16)]);
    }
    return color.toString();
  }
}
