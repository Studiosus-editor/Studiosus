package lt.sus.Studiosus.controllers;

import static lt.sus.Studiosus.service.UserDetailsServiceImpl.getEmail;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import lt.sus.Studiosus.dto.EmailRole;
import lt.sus.Studiosus.dto.FolderDTO;
import lt.sus.Studiosus.dto.ProjectDetailsResponse;
import lt.sus.Studiosus.dto.TemplateRequest;
import lt.sus.Studiosus.model.*;
import lt.sus.Studiosus.model.enums.LogLevel;
import lt.sus.Studiosus.model.enums.Role;
import lt.sus.Studiosus.repository.*;
import lt.sus.Studiosus.service.LoggingService;
import lt.sus.Studiosus.service.ProjectService;
import lt.sus.Studiosus.service.TemplateService;
import lt.sus.Studiosus.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(
    allowCredentials = "true",
    originPatterns = "*",
    allowedHeaders = "*",
    methods = {
      RequestMethod.DELETE,
      RequestMethod.GET,
      RequestMethod.POST,
      RequestMethod.PUT,
      RequestMethod.OPTIONS
    })
@RestController
@RequestMapping("/api/")
public class ProjectController {

  @Value("${app.frontendUrl}")
  private String frontendUrl;

  private final UserRepository userRepository;
  private final UserProjectRoleRepository userProjectRoleRepository;
  private final ProjectInvitedMembersRepository projectInvitedMembersRepository;
  private final InvitedMembersRepository invitedMembersRepository;
  private final ProjectRepository projectRepository;
  private final ProjectService projectService;
  private final TemplateService templateService;
  private final UserDetailsServiceImpl userDetailsService;
  private final LoggingService loggingService;

  public ProjectController(
      UserRepository userRepository,
      UserProjectRoleRepository userProjectRoleRepository,
      ProjectInvitedMembersRepository projectInvitedMembersRepository,
      InvitedMembersRepository invitedMembersRepository,
      ProjectRepository projectRepository,
      ProjectService projectService,
      TemplateService templateService,
      UserDetailsServiceImpl userDetailsService,
      LoggingService loggingService) {
    this.userRepository = userRepository;
    this.userProjectRoleRepository = userProjectRoleRepository;
    this.projectInvitedMembersRepository = projectInvitedMembersRepository;
    this.invitedMembersRepository = invitedMembersRepository;
    this.projectRepository = projectRepository;
    this.templateService = templateService;
    this.projectService = projectService;
    this.userDetailsService = userDetailsService;
    this.loggingService = loggingService;
  }

  @GetMapping("/projects")
  public ResponseEntity<?> getProjects(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String email = getEmail(authentication);
    User user =
        userRepository
            .findUserByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    return ResponseEntity.status(HttpStatus.OK).body(projectService.getProjectsForUser(user));
  }

  @GetMapping("/project/{id}")
  public ResponseEntity<ProjectDetailsResponse> getProject(
      @PathVariable Long id, Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String email = getEmail(authentication);

    try {
      ProjectDetailsResponse projectDetailsResponse =
          projectService.getProjectDetailsForUser(email, id);
      return ResponseEntity.status(HttpStatus.OK).body(projectDetailsResponse);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @GetMapping("/project/{id}/folders")
  public ResponseEntity<FolderDTO> getProjectFolders(
      @PathVariable Long id, Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    try {
      String email = getEmail(authentication);
      return ResponseEntity.status(HttpStatus.OK).body(projectService.getProjectFolders(email, id));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @GetMapping("/project/{projectId}/invite/{inviteLink}")
  public void processEmailInviteLink(
      @PathVariable Long projectId,
      @PathVariable String inviteLink,
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication)
      throws IOException {
    if (inviteLink == null) {
      response.sendRedirect(frontendUrl + "/projects?status=NoLinkProvided");
      return;
    }

    if (authentication == null || !authentication.isAuthenticated()) {
      Map<String, String> linkAndRole = new HashMap<>();
      linkAndRole.put("link", inviteLink);
      linkAndRole.put("role", "EMAIL");

      // Convert the map to a JSON string
      String linkAndRoleJson = new ObjectMapper().writeValueAsString(linkAndRole);

      // Encode the JSON string using Base64
      String encodedLinkAndRole = Base64.getEncoder().encodeToString(linkAndRoleJson.getBytes());

      // Create a new cookie
      Cookie cookie = new Cookie("linkAndRole", encodedLinkAndRole);
      cookie.setPath("/");
      // Add the cookie to the response
      response.addCookie(cookie);

      response.sendRedirect(frontendUrl + "/login?status=NotAuthenticated");
      return;
    }

    String email;
    User user;
    Project project;
    ProjectInvitedMembers projectInvitedMembers;

    try {
      email = getEmail(authentication);
      user = userDetailsService.getUser(email);
      projectInvitedMembers =
          projectInvitedMembersRepository
              .findByInviteLink(inviteLink)
              .orElseThrow(() -> new IllegalArgumentException("Invalid link"));
      project = projectInvitedMembers.getProject();

    } catch (IllegalArgumentException e) {
      loggingService.saveLogToDatabase(
          new Log(
              "Unsuccessfully joined project with invite link: %s".formatted(e.getMessage()),
              LogLevel.INFO,
              LocalDateTime.now()));

      response.sendRedirect(frontendUrl + "/projects?status=invalidLink");
      return;
    }

    Optional<UserProjectRole> userProjectRole =
        userProjectRoleRepository.findUserProjectRoleByUserAndProject(user, project);

    try {
      addUserToProjectFromEmailInvite(userProjectRole, user, projectInvitedMembers);
      response.sendRedirect(frontendUrl + "/projects?status=addedToProjectSuccessfully");
    } catch (IllegalArgumentException e) {

      loggingService.saveLogToDatabase(
          new Log(
              "Unsuccessfully joined project with invite link: %s".formatted(e.getMessage()),
              LogLevel.INFO,
              LocalDateTime.now()));

      response.sendRedirect(frontendUrl + "/projects?status=hasHigherAuthority");
    }
  }

  @GetMapping("/project/{projectId}/file/{fileId}")
  public ResponseEntity<String> getFileContent(
      @PathVariable Long projectId, @PathVariable Long fileId, Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String email = getEmail(authentication);
    try {
      File file = projectService.getFileForUser(email, projectId, fileId);
      return ResponseEntity.status(HttpStatus.OK).body(file.getContent());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }
  }

  @GetMapping("/project/invite")
  public void assignRoleToTheProject(
      @RequestParam(required = false) String viewerLink,
      @RequestParam(required = false) String editorLink,
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication)
      throws IOException {

    if (viewerLink != null && editorLink != null) {
      loggingService.saveLogToDatabase(
          new Log(
              "Unsuccessfully joined project with invite link: %s"
                  .formatted("Multiple links provided"),
              LogLevel.INFO,
              LocalDateTime.now()));

      response.sendRedirect(frontendUrl + "/projects?status=MultipleLinksProvided");
      return;
    }

    if (authentication == null || !authentication.isAuthenticated()) {
      Map<String, String> linkAndRole = new HashMap<>();
      if (viewerLink != null) {
        linkAndRole.put("link", viewerLink);
        linkAndRole.put("role", Role.VIEWER.name());
      } else {
        linkAndRole.put("link", editorLink);
        linkAndRole.put("role", Role.EDITOR.name());
      }

      // Convert the map to a JSON string
      String linkAndRoleJson = new ObjectMapper().writeValueAsString(linkAndRole);

      // Encode the JSON string using Base64
      String encodedLinkAndRole = Base64.getEncoder().encodeToString(linkAndRoleJson.getBytes());

      // Create a new cookie
      Cookie cookie = new Cookie("linkAndRole", encodedLinkAndRole);
      cookie.setPath("/");
      // Add the cookie to the response
      response.addCookie(cookie);

      loggingService.saveLogToDatabase(
          new Log(
              "Unsuccessfully joined project with invite link: %s".formatted("Not authenticated"),
              LogLevel.INFO,
              LocalDateTime.now()));

      response.sendRedirect(frontendUrl + "/login?status=NotAuthenticated");
      return;
    }

    if (viewerLink == null && editorLink == null) {
      loggingService.saveLogToDatabase(
          new Log(
              "Unsuccessfully joined project with invite link: %s".formatted("No link provided"),
              LogLevel.INFO,
              LocalDateTime.now()));

      response.sendRedirect(frontendUrl + "/projects?status=NoLinkProvided");
      return;
    }

    String email;
    User user;
    Project project;
    try {
      email = getEmail(authentication);
      user = userDetailsService.getUser(email);
      if (viewerLink != null) {
        project = getProject(viewerLink, Role.VIEWER);
      } else {
        project = getProject(editorLink, Role.EDITOR);
      }
    } catch (IllegalArgumentException e) {
      loggingService.saveLogToDatabase(
          new Log(
              "Unsuccessfully joined project with invite link: %s".formatted(e.getMessage()),
              LogLevel.INFO,
              LocalDateTime.now()));
      response.sendRedirect(frontendUrl + "/projects?status=invalidLink");
      return;
    }

    Optional<UserProjectRole> userProjectRole =
        userProjectRoleRepository.findUserProjectRoleByUserAndProject(user, project);

    try {
      if (viewerLink != null) {
        checkRoleAndAddUserToProject(userProjectRole, email, viewerLink, Role.VIEWER);
      } else {
        checkRoleAndAddUserToProject(userProjectRole, email, editorLink, Role.EDITOR);
      }
      response.sendRedirect(frontendUrl + "/projects?status=addedToProjectSuccessfully");
    } catch (IllegalArgumentException e) {
      loggingService.saveLogToDatabase(
          new Log(
              "Unsuccessfully joined project with invite link: %s".formatted(e.getMessage()),
              LogLevel.INFO,
              LocalDateTime.now()));
      response.sendRedirect(frontendUrl + "/projects?status=hasHigherAuthority");
    }
  }

  @PostMapping("/project/{name}")
  public ResponseEntity<Project> initProject(
      @PathVariable String name, Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String email = getEmail(authentication);
    Project savedProject = projectService.initProjectForUser(email, name);

    return ResponseEntity.status(HttpStatus.CREATED).body(savedProject);
  }

  @PostMapping("/project/{projectId}/folder/{parentFolderId}")
  public ResponseEntity<Folder> createFolder(
      @PathVariable Long projectId,
      @PathVariable Long parentFolderId,
      @RequestBody String folderName,
      Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String email = getEmail(authentication);
    try {
      Folder newFolder =
          projectService.createFolderForProject(email, projectId, parentFolderId, folderName);
      return ResponseEntity.status(HttpStatus.CREATED).body(newFolder);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @PostMapping("/project/{projectId}/folder/{parentFolderId}/file")
  public ResponseEntity<File> createFile(
      @PathVariable Long projectId,
      @PathVariable Long parentFolderId,
      @RequestBody String fileName,
      Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String email = getEmail(authentication);
    try {
      File newFile =
          projectService.createFileForProject(email, projectId, parentFolderId, fileName);
      return ResponseEntity.status(HttpStatus.CREATED).body(newFile);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @PostMapping("/project/{projectId}/template")
  public ResponseEntity<Template> duplicateProjectToTemplate(
      @PathVariable Long projectId,
      @RequestBody TemplateRequest templateRequest,
      Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String email = getEmail(authentication);
    User user = userDetailsService.getUser(email);
    try {
      Project project =
          projectRepository
              .findProjectById(projectId)
              .orElseThrow(() -> new IllegalArgumentException("Project not found"));
      UserProjectRole userProjectRole =
          userProjectRoleRepository
              .findUserProjectRoleByUserAndProject(user, project)
              .orElseThrow(() -> new IllegalArgumentException("User does not have permission"));

      if (!userProjectRole.getRole().equals(Role.OWNER.name())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }

      Template template =
          templateService.duplicateProjectToTemplate(
              project, user, templateRequest.name(), templateRequest.description());
      return ResponseEntity.status(HttpStatus.CREATED).body(template);
    } catch (Exception e) {
      loggingService.saveLogToDatabase(
          new Log(
              "Error creating template from project: %s".formatted(e.getMessage()),
              LogLevel.WARN,
              LocalDateTime.now()));
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @PutMapping("/project/{id}:finalize")
  public ResponseEntity<Project> finalizeProject(
      @PathVariable Long id,
      @RequestBody List<EmailRole> emailRoles,
      Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String email = getEmail(authentication);
    Project savedProject = projectService.finalizeProjectForUser(email, id, emailRoles);

    return ResponseEntity.status(HttpStatus.OK).body(savedProject);
  }

  @PutMapping("/project/{id}:leave")
  public ResponseEntity<?> removeUserFromProject(
      @PathVariable Long id, Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    try {
      String email = getEmail(authentication);
      projectService.removeProjectFromUser(email, id);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body("User does not have permission to leave the project");
    }

    return ResponseEntity.status(HttpStatus.OK)
        .body("{\"message\": \"Removed project from user successfully\"}");
  }

  @PutMapping("/project/{id}:rename")
  public ResponseEntity<?> renameProject(
      @PathVariable Long id, @RequestBody String newName, Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String email = getEmail(authentication);

    String newProjectName;
    try {
      newProjectName = projectService.renameProjectForUser(email, id, newName);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body("User does not have permission to rename the project");
    }

    return ResponseEntity.status(HttpStatus.OK)
        .body("{\"message\": \"Renamed project to " + newProjectName + " successfully\"}");
  }

  @PutMapping("/project/{id}:update")
  public ResponseEntity<?> updateProjectMemberList(
      @PathVariable Long id,
      @RequestBody List<EmailRole> emailRoles,
      Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    try {
      String email = getEmail(authentication);
      projectService.updateProjectMembers(email, id, emailRoles);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }

    return ResponseEntity.status(HttpStatus.OK)
        .body("{\"message\": \"Updated project members successfully\"}");
  }

  @PutMapping("/project/{projectId}/file/{fileId}:update")
  public ResponseEntity<File> updateFile(
      @PathVariable Long projectId,
      @PathVariable Long fileId,
      @RequestBody String content,
      Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String email = getEmail(authentication);
    try {
      File updatedFile = projectService.updateFileForProject(email, projectId, fileId, content);
      return ResponseEntity.status(HttpStatus.OK).body(updatedFile);
    } catch (Exception e) {
      loggingService.saveLogToDatabase(
          new Log(
              "Error updating project file content for user: %s, error: %s"
                  .formatted(email, e.getMessage()),
              LogLevel.WARN,
              LocalDateTime.now()));
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @PutMapping("/project/{projectId}/file/{fileId}:rename")
  public ResponseEntity<File> renameFile(
      @PathVariable Long projectId,
      @PathVariable Long fileId,
      @RequestBody String newFileName,
      Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    String email = getEmail(authentication);
    try {
      File updatedFile =
          projectService.updateFileNameForProject(email, projectId, fileId, newFileName);
      return ResponseEntity.status(HttpStatus.OK).body(updatedFile);
    } catch (Exception e) {
      loggingService.saveLogToDatabase(
          new Log(
              "Error renaming project file for user: %s, error: %s"
                  .formatted(email, e.getMessage()),
              LogLevel.WARN,
              LocalDateTime.now()));
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @PutMapping("/project/{projectId}/folder/{folderId}:rename")
  public ResponseEntity<Folder> renameFolder(
      @PathVariable Long projectId,
      @PathVariable Long folderId,
      @RequestBody String newFolderName,
      Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    String email = getEmail(authentication);
    try {
      Folder updatedFolder =
          projectService.updateFolderNameForProject(email, projectId, folderId, newFolderName);
      return ResponseEntity.status(HttpStatus.OK).body(updatedFolder);
    } catch (Exception e) {
      loggingService.saveLogToDatabase(
          new Log(
              "Error renaming project folder for user: %s, error: %s"
                  .formatted(email, e.getMessage()),
              LogLevel.WARN,
              LocalDateTime.now()));
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @PutMapping("/project/{projectId}/folder/{folderId}/{newParentFolderId}")
  public ResponseEntity<FolderDTO> moveFolder(
      @PathVariable Long projectId,
      @PathVariable Long folderId,
      @PathVariable Long newParentFolderId,
      Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    String email = getEmail(authentication);
    try {
      FolderDTO updatedProjectStructure =
          projectService.moveFolderForProject(email, projectId, folderId, newParentFolderId);
      return ResponseEntity.status(HttpStatus.OK).body(updatedProjectStructure);
    } catch (Exception e) {
      loggingService.saveLogToDatabase(
          new Log(
              "Error moving project folder for user: %s, error: %s"
                  .formatted(email, e.getMessage()),
              LogLevel.WARN,
              LocalDateTime.now()));
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @PutMapping("/project/{projectId}/file/{fileId}/{newParentFolderId}")
  public ResponseEntity<FolderDTO> moveFile(
      @PathVariable Long projectId,
      @PathVariable Long fileId,
      @PathVariable Long newParentFolderId,
      Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    String email = getEmail(authentication);
    try {
      FolderDTO updatedProjectStructure =
          projectService.moveFileForProject(email, projectId, fileId, newParentFolderId);
      return ResponseEntity.status(HttpStatus.OK).body(updatedProjectStructure);
    } catch (Exception e) {
      loggingService.saveLogToDatabase(
          new Log(
              "Error moving project file for user: %s, error: %s".formatted(email, e.getMessage()),
              LogLevel.WARN,
              LocalDateTime.now()));
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @DeleteMapping("/project/{id}")
  public ResponseEntity<String> deleteProject(
      @PathVariable Long id, Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String email = getEmail(authentication);

    try {
      projectService.deleteProjectForUser(email, id);
    } catch (DataIntegrityViolationException e) {
      loggingService.saveLogToDatabase(
          new Log(
              "Error deleting project %s, for user: %s: error: %s"
                  .formatted(id, email, e.getMessage()),
              LogLevel.FATAL,
              LocalDateTime.now()));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("User does not have permission to delete the project");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body("User does not have permission to delete the project");
    }

    return ResponseEntity.status(HttpStatus.OK)
        .body("{\"message\": \"Deleted project successfully\"}");
  }

  @DeleteMapping("/project/{projectId}/file/{fileId}")
  public ResponseEntity<Project> deleteFile(
      @PathVariable Long projectId, @PathVariable Long fileId, Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String email = getEmail(authentication);
    try {
      Project updatedProjectStructure =
          projectService.deleteFileForProject(email, projectId, fileId);
      return ResponseEntity.status(HttpStatus.OK).body(updatedProjectStructure);
    } catch (Exception e) {
      loggingService.saveLogToDatabase(
          new Log(
              "Error deleting project file for user: %s, error: %s"
                  .formatted(email, e.getMessage()),
              LogLevel.WARN,
              LocalDateTime.now()));
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @DeleteMapping("/project/{projectId}/folder/{folderId}")
  public ResponseEntity<Project> deleteFolder(
      @PathVariable Long projectId, @PathVariable Long folderId, Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String email = getEmail(authentication);
    try {
      Project updatedProjectStructure =
          projectService.deleteFolderForProject(email, projectId, folderId);
      return ResponseEntity.status(HttpStatus.OK).body(updatedProjectStructure);
    } catch (Exception e) {
      loggingService.saveLogToDatabase(
          new Log(
              "Error deleting project folder for user: %s, error: %s"
                  .formatted(email, e.getMessage()),
              LogLevel.WARN,
              LocalDateTime.now()));
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  private void addUserToProjectFromEmailInvite(
      Optional<UserProjectRole> userProjectRole,
      User user,
      ProjectInvitedMembers projectInvitedMembers) {

    if (userProjectRole.isPresent()) {
      Role userRole = Role.valueOf(userProjectRole.get().getRole().toUpperCase());

      if (userRole.getValue() >= projectInvitedMembers.getType()) {
        throw new IllegalArgumentException("User already has higher or same authority");
      }

      UserProjectRole newUserProjectRole = userProjectRole.get();
      newUserProjectRole.setRole(Role.getRoleByValue(projectInvitedMembers.getType()).name());
      userProjectRoleRepository.save(newUserProjectRole);
      projectInvitedMembersRepository.delete(projectInvitedMembers);

      // Check if the invited member is not invited to any other project
      // if so delete it
      InvitedMembers invitedMembers = projectInvitedMembers.getInvitedMembers();
      List<ProjectInvitedMembers> projectInvitedMembersList =
          projectInvitedMembersRepository.findAllByInvitedMembers(invitedMembers);
      if (projectInvitedMembersList.isEmpty()) {
        invitedMembersRepository.delete(invitedMembers);
      }
      return;
    }

    UserProjectRole newUserProjectRole =
        new UserProjectRole(
            user,
            projectInvitedMembers.getProject(),
            Role.getRoleByValue(projectInvitedMembers.getType()).name());
    userProjectRoleRepository.save(newUserProjectRole);
    projectInvitedMembersRepository.delete(projectInvitedMembers);

    // Check if the invited member is not invited to any other project
    // if so delete it
    InvitedMembers invitedMembers = projectInvitedMembers.getInvitedMembers();
    List<ProjectInvitedMembers> projectInvitedMembersList =
        projectInvitedMembersRepository.findAllByInvitedMembers(invitedMembers);
    if (projectInvitedMembersList.isEmpty()) {
      invitedMembersRepository.delete(invitedMembers);
    }
  }

  private void checkRoleAndAddUserToProject(
      Optional<UserProjectRole> userProjectRoleOptional, String email, String link, Role role)
      throws IllegalArgumentException {
    if (userProjectRoleOptional.isPresent()) {
      Role userRole = Role.valueOf(userProjectRoleOptional.get().getRole().toUpperCase());

      if (userRole.getValue() >= role.getValue()) {
        throw new IllegalArgumentException("User already has higher or same authority");
      }

      UserProjectRole userProjectRole = userProjectRoleOptional.get();
      userProjectRole.setRole(role.name());
      userProjectRoleRepository.save(userProjectRole);
    } else if (role == Role.VIEWER) {
      projectService.addViewerToProjectByLink(email, link);
    } else {
      projectService.addEditorToProjectByLink(email, link);
    }
  }

  private Project getProject(String link, Role role) throws IllegalArgumentException {
    return (role == Role.EDITOR
            ? projectRepository.findProjectByEditorLink(link)
            : projectRepository.findProjectByViewerLink(link))
        .orElseThrow(() -> new IllegalArgumentException("Invalid link"));
  }
}
