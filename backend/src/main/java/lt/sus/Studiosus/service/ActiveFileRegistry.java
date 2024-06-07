package lt.sus.Studiosus.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lt.sus.Studiosus.model.File;
import lt.sus.Studiosus.repository.FileRepository;
import org.springframework.stereotype.Service;

@Service
public class ActiveFileRegistry {

  private final Map<String, Map<String, String>> projectFiles = new ConcurrentHashMap<>();
  private final Map<String, Set<String>> fileUsers = new ConcurrentHashMap<>();
  private final Map<String, Boolean> hasChanges = new ConcurrentHashMap<>();

  private final FileRepository fileRepository;

  public ActiveFileRegistry(FileRepository fileRepository) {
    this.fileRepository = fileRepository;

    // Schedule a task to save the file content to the database every 5 seconds
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    scheduler.scheduleAtFixedRate(this::saveFileContentToDatabase, 5, 5, TimeUnit.SECONDS);
  }

  public Set<String> getProjectFiles(String projectId) {
    Map<String, String> files = projectFiles.get(projectId);
    return files != null ? files.keySet() : new HashSet<>();
  }

  public void addUserToFile(String projectId, String fileId, String userId) {
    Set<String> users = fileUsers.computeIfAbsent(fileId, k -> new HashSet<>());
    users.add(userId);
  }

  //  remove user from allFiles and delete file if no users left
  public void removeUserFromProject(String projectId, String userId) {
    for (Map.Entry<String, Set<String>> entry : fileUsers.entrySet()) {
      String fileId = entry.getKey();
      Set<String> users = entry.getValue();
      users.remove(userId);
      if (users.isEmpty()) {
        removeFile(projectId, fileId);
      }
    }
  }

  public void removeUserFromOtherFiles(String projectId, String currentFileId, String userId) {
    for (Map.Entry<String, Set<String>> entry : fileUsers.entrySet()) {
      String fileId = entry.getKey();
      Set<String> users = entry.getValue();
      if (!fileId.equals(currentFileId) && users.contains(userId)) {
        users.remove(userId);
        if (users.isEmpty()) {
          removeFile(projectId, fileId);
        }
      }
    }
  }

  public void removeFile(String projectId, String fileId) {
    Map<String, String> files = projectFiles.get(projectId);
    if (files != null) {
      saveFileContentToDatabase();
      files.remove(fileId);
      projectFiles.put(projectId, files);
    }
  }

  public String getFileContent(String projectId, String fileId) {
    Map<String, String> files =
        projectFiles.computeIfAbsent(projectId, k -> new ConcurrentHashMap<>());

    if (files.get(fileId) != null) {
      return files.get(fileId);
    }

    fileRepository
        .findById(Long.parseLong(fileId))
        .ifPresent(fileInDatabase -> files.put(fileId, fileInDatabase.getContent()));

    return files.get(fileId);
  }

  public void setFileContent(String projectId, String fileId, String fileContent) {
    Map<String, String> files = projectFiles.getOrDefault(projectId, new ConcurrentHashMap<>());
    files.put(fileId, fileContent);
    projectFiles.put(projectId, files);
    hasChanges.put(projectId, true);
  }

  private void saveFileContentToDatabase() {
    // Check if there are new changes to save for each project
    for (Map.Entry<String, Boolean> entry : hasChanges.entrySet()) {
      String projectId = entry.getKey();
      Boolean hasChange = entry.getValue();
      if (hasChange) {
        // Save the file content to the database
        Map<String, String> files = projectFiles.get(projectId);
        if (files != null) {
          for (Map.Entry<String, String> fileEntry : files.entrySet()) {
            String fileId = fileEntry.getKey();
            String content = fileEntry.getValue();
            File file = fileRepository.findById(Long.parseLong(fileId)).orElse(null);
            if (file != null) {
              file.setContent(content);
              fileRepository.save(file);
            }
          }
        }

        hasChanges.put(projectId, false);
      }
    }
  }
}
