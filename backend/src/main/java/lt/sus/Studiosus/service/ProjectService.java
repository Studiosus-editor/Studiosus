package lt.sus.Studiosus.service;

import static lt.sus.Studiosus.service.MailSenderService.inviteLinkHtml;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lt.sus.Studiosus.controllers.exception.NoPermissionException;
import lt.sus.Studiosus.dto.EmailRole;
import lt.sus.Studiosus.dto.FileDTO;
import lt.sus.Studiosus.dto.FolderDTO;
import lt.sus.Studiosus.dto.ProjectDetailsResponse;
import lt.sus.Studiosus.dto.ProjectRole;
import lt.sus.Studiosus.model.File;
import lt.sus.Studiosus.model.Folder;
import lt.sus.Studiosus.model.InvitedMembers;
import lt.sus.Studiosus.model.Log;
import lt.sus.Studiosus.model.Project;
import lt.sus.Studiosus.model.ProjectInvitedMembers;
import lt.sus.Studiosus.model.User;
import lt.sus.Studiosus.model.UserProjectRole;
import lt.sus.Studiosus.model.enums.LogLevel;
import lt.sus.Studiosus.model.enums.ResourceType;
import lt.sus.Studiosus.model.enums.Role;
import lt.sus.Studiosus.repository.FileRepository;
import lt.sus.Studiosus.repository.FolderRepository;
import lt.sus.Studiosus.repository.InvitedMembersRepository;
import lt.sus.Studiosus.repository.ProjectInvitedMembersRepository;
import lt.sus.Studiosus.repository.ProjectRepository;
import lt.sus.Studiosus.repository.UserProjectRoleRepository;
import lt.sus.Studiosus.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {
  private final UserRepository userRepository;
  private final ProjectRepository projectRepository;
  private final UserProjectRoleRepository userProjectRoleRepository;
  private final InvitedMembersRepository invitedMembersRepository;
  private final ProjectInvitedMembersRepository projectInvitedMembersRepository;
  private final FolderRepository folderRepository;
  private final FileRepository fileRepository;
  private final MailSenderService mailService;
  private final LoggingService loggingService;

  private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

  @Value("${app.backendUrl}")
  private String backendUrl;

  public ProjectService(
      UserRepository userRepository,
      ProjectRepository projectRepository,
      UserProjectRoleRepository userProjectRoleRepository,
      InvitedMembersRepository invitedMembersRepository,
      ProjectInvitedMembersRepository projectInvitedMembersRepository,
      FolderRepository folderRepository,
      FileRepository fileRepository,
      MailSenderService mailService,
      LoggingService loggingService) {
    this.userRepository = userRepository;
    this.projectRepository = projectRepository;
    this.userProjectRoleRepository = userProjectRoleRepository;
    this.invitedMembersRepository = invitedMembersRepository;
    this.projectInvitedMembersRepository = projectInvitedMembersRepository;
    this.folderRepository = folderRepository;
    this.fileRepository = fileRepository;
    this.mailService = mailService;
    this.loggingService = loggingService;
  }

  public Project initProjectForUser(String email, String projectName) {
    User user = userRepository.findUserByEmail(email).orElseThrow();

    // generate random viewer and editor links
    String viewerLink = UUID.randomUUID().toString();
    String editorLink = UUID.randomUUID().toString();

    Folder parentFolder = new Folder(projectName, null);
    folderRepository.save(parentFolder);

    // delete any previous Initialized project if any
    userProjectRoleRepository.deleteAllByUserAndRoleAndProjectPhase(
        user, Role.OWNER.name(), "Initialized");

    Project project = new Project(projectName, "Initialized", viewerLink, editorLink, parentFolder);

    projectRepository.save(project);
    logger.info("Created project: {}", project);

    UserProjectRole userProjectRole = new UserProjectRole(user, project, Role.OWNER.name());
    userProjectRoleRepository.save(userProjectRole);
    logger.info("Assigned role: {} to user: {} for project: {}", "Owner", user, project);

    return project;
  }

  public Project finalizeProjectForUser(String email, Long projectId, List<EmailRole> emailRoles) {
    User user = userRepository.findUserByEmail(email).orElseThrow();
    Project project = projectRepository.findById(projectId).orElseThrow();

    // Check if user has permission to finalize the project
    UserProjectRole userProjectRole =
        userProjectRoleRepository.findByUserAndProject(user, project).orElseThrow();
    if (!userProjectRole.getRole().equals(Role.OWNER.name())) {
      throw new RuntimeException(
          "User does not have permission to change project_phase to finalize the project");
    }

    logger.info("starting project finalization process");

    project.setPhase("Finalized");
    projectRepository.save(project);

    for (EmailRole emailRole : emailRoles) {
      Optional<InvitedMembers> invitedMember =
          invitedMembersRepository.findInvitedMembersByEmail(emailRole.email());
      String inviteLink = UUID.randomUUID().toString();
      if (invitedMember.isPresent()) {
        ProjectInvitedMembers projectInvitedMembers =
            new ProjectInvitedMembers(invitedMember.get(), project, inviteLink, emailRole.role());
        projectInvitedMembersRepository.save(projectInvitedMembers);

        // Send email to the new invited member
        String inviteLinkUrl =
            backendUrl + "/api/project/" + project.getId() + "/invite/" + inviteLink;
        mailService.sendNewMail(
            emailRole.email(),
            "Someone invited you to a project!",
            inviteLinkHtml(project.getName(), inviteLinkUrl));
        loggingService.saveLogToDatabase(
            new Log(
                "Send invite to project to email: %s".formatted(emailRole.email()),
                LogLevel.INFO,
                LocalDateTime.now()));
      } else {
        InvitedMembers newInvitedMember = new InvitedMembers(emailRole.email());
        invitedMembersRepository.save(newInvitedMember);
        ProjectInvitedMembers projectInvitedMembers =
            new ProjectInvitedMembers(newInvitedMember, project, inviteLink, emailRole.role());
        projectInvitedMembersRepository.save(projectInvitedMembers);

        // Send email to the new invited member
        String inviteLinkUrl =
            backendUrl + "/api/project/" + project.getId() + "/invite/" + inviteLink;
        mailService.sendNewMail(
            emailRole.email(),
            "Someone invited you to a project!",
            inviteLinkHtml(project.getName(), inviteLinkUrl));
        loggingService.saveLogToDatabase(
            new Log(
                "Send invite to project to email: %s".formatted(emailRole.email()),
                LogLevel.INFO,
                LocalDateTime.now()));
      }
    }

    logger.info("finalized project: {}", project);
    return project;
  }

  public UserProjectRole addViewerToProjectByLink(String email, String viewerLink) {
    User user = userRepository.findUserByEmail(email).orElseThrow();
    Project project = projectRepository.findProjectByViewerLink(viewerLink).orElseThrow();

    UserProjectRole userProjectRole = new UserProjectRole(user, project, Role.VIEWER.name());
    userProjectRoleRepository.save(userProjectRole);
    logger.info("Assigned role: {} to user: {} for project: {}", "Viewer", user, project);

    return userProjectRole;
  }

  public UserProjectRole addEditorToProjectByLink(String email, String editorLink) {
    User user = userRepository.findUserByEmail(email).orElseThrow();
    Project project = projectRepository.findProjectByEditorLink(editorLink).orElseThrow();

    UserProjectRole userProjectRole = new UserProjectRole(user, project, Role.EDITOR.name());
    userProjectRoleRepository.save(userProjectRole);
    logger.info("Assigned role: {} to user: {} for project: {}", "Editor", user, project);

    return userProjectRole;
  }

  public ProjectDetailsResponse getProjectDetailsForUser(String email, Long projectId) {
    User user = userRepository.findUserByEmail(email).orElseThrow();
    Project project = projectRepository.findById(projectId).orElseThrow();

    // Check if user has permission to view the project
    UserProjectRole userProjectRole =
        userProjectRoleRepository.findByUserAndProject(user, project).orElseThrow();
    if (!userProjectRole.getRole().equals(Role.OWNER.name())) {
      throw new RuntimeException("User does not have permission to view the project");
    }

    userProjectRole.setLastEdited(new Date());
    userProjectRoleRepository.save(userProjectRole);

    List<ProjectInvitedMembers> projectInvitedMembers =
        projectInvitedMembersRepository.findAllProjectInvitedMembersByProject(project);

    List<UserProjectRole> userProjectRoles = userProjectRoleRepository.findAllByProject(project);

    ProjectDetailsResponse projectDetailsResponse =
        new ProjectDetailsResponse(project, projectInvitedMembers, userProjectRoles);

    logger.info("Fetched project: {} for user: {}", project, user);

    return projectDetailsResponse;
  }

  public FolderDTO getProjectFolders(String email, Long projectId, boolean fetchContent)
      throws RuntimeException {

    User user = userRepository.findUserByEmail(email).orElseThrow();
    Project project = projectRepository.findById(projectId).orElseThrow();
    Folder rootFolder = project.getParentFolder();

    // Check if user has at least viewer permissions to view the project
    UserProjectRole userProjectRole =
        userProjectRoleRepository.findByUserAndProject(user, project).orElseThrow();
    Role userRole = Role.valueOf(userProjectRole.getRole().toUpperCase());

    if (userRole.getValue() < Role.VIEWER.getValue()) {
      throw new RuntimeException("User does not have permission to view the project");
    }

    userProjectRole.setLastEdited(new Date());
    userProjectRoleRepository.save(userProjectRole);

    logger.info("retrieving all files and folders for project: {}", project.getName());

    return getFilesAndFolders(rootFolder, fetchContent);
  }

  public Folder createFolderForProject(
      String email, Long projectId, Long parentFolderId, String folderName) {
    User user = userRepository.findUserByEmail(email).orElseThrow();
    Project project = projectRepository.findById(projectId).orElseThrow();
    Folder parentFolder = folderRepository.findById(parentFolderId).orElseThrow();

    // Traverse up the parent folders until the root folder is reached
    Folder rootFolder = parentFolder;
    while (rootFolder.getParentFolder() != null) {
      rootFolder = rootFolder.getParentFolder();
    }

    // Check if the root folder is the same as the project's parent folder
    if (!rootFolder.getId().equals(project.getParentFolder().getId())) {
      throw new RuntimeException("The parent folder does not belong to the project");
    }

    // Check if user has permission to create a folder in the project
    UserProjectRole userProjectRole =
        userProjectRoleRepository.findByUserAndProject(user, project).orElseThrow();
    Role userRole = Role.valueOf(userProjectRole.getRole().toUpperCase());

    if (!(userRole == Role.OWNER || userRole == Role.EDITOR)) {
      throw new RuntimeException("User does not have permission to create a folder in the project");
    }

    Folder newFolder = new Folder(folderName, parentFolder);
    folderRepository.save(newFolder);
    return newFolder;
  }

  public File createFileForProject(
      String email, Long projectId, Long parentFolderId, String fileName, String fileContent) {
    User user = userRepository.findUserByEmail(email).orElseThrow();
    Project project = projectRepository.findById(projectId).orElseThrow();
    Folder parentFolder = folderRepository.findById(parentFolderId).orElseThrow();

    // Check if user has permission to create a file in the project
    UserProjectRole userProjectRole =
        userProjectRoleRepository.findByUserAndProject(user, project).orElseThrow();
    Role userRole = Role.valueOf(userProjectRole.getRole().toUpperCase());

    if (!(userRole == Role.OWNER || userRole == Role.EDITOR)) {
      throw new RuntimeException("User does not have permission to create a file in the project");
    }

    File newFile = new File(fileName, Optional.ofNullable(fileContent).orElse(""), parentFolder);
    fileRepository.save(newFile);
    return newFile;
  }

  @Transactional
  public void deleteProjectForUser(String email, Long projectId) {
    User user = userRepository.findUserByEmail(email).orElseThrow();
    Project project = projectRepository.findById(projectId).orElseThrow();

    // Check if user has permission to delete the project
    UserProjectRole userProjectRole =
        userProjectRoleRepository.findByUserAndProject(user, project).orElseThrow();
    if (!userProjectRole.getRole().equals(Role.OWNER.name())) {
      throw new RuntimeException("User does not have permission to delete the project");
    }

    logger.info("starting deletion process");

    // Delete the project
    userProjectRoleRepository.deleteAllByProjectId(project.getId());

    // Delete all ProjectInvitedMembers associated with the project
    projectInvitedMembersRepository.deleteAllByProjectId(project.getId());

    projectRepository.delete(project);
    logger.info("Deleted project: {}", project);
  }

  public List<ProjectRole> getProjectsForUser(User user) {
    List<UserProjectRole> userProjectRoles = userProjectRoleRepository.findByUser(user);
    logger.info("Fetched projects for user: {}", user.getEmail());
    return userProjectRoles.stream()
        .sorted(Comparator.comparing(UserProjectRole::getLastEdited).reversed())
        .filter(upr -> upr.getProject().getPhase().equals("Finalized"))
        .map(
            upr ->
                new ProjectRole(
                    upr.getProject().getId(),
                    upr.getProject().getName(),
                    upr.getRole(),
                    upr.getLastEdited()))
        .collect(Collectors.toList());
  }

  public String renameProjectForUser(String email, Long projectId, String newName) {
    User user = userRepository.findUserByEmail(email).orElseThrow();
    Project project = projectRepository.findById(projectId).orElseThrow();

    // Check if user has permission to rename the project
    UserProjectRole userProjectRole =
        userProjectRoleRepository.findByUserAndProject(user, project).orElseThrow();
    if (!userProjectRole.getRole().equals(Role.OWNER.name())) {
      throw new RuntimeException("User does not have permission to rename the project");
    }

    userProjectRole.setLastEdited(new Date());
    userProjectRoleRepository.save(userProjectRole);

    project.setName(newName);
    projectRepository.save(project);

    Folder rootFolder = project.getParentFolder();
    rootFolder.setName(newName);
    folderRepository.save(rootFolder);

    return project.getName();
  }

  public void removeProjectFromUser(String email, Long projectId) {
    User user = userRepository.findUserByEmail(email).orElseThrow();
    Project project = projectRepository.findById(projectId).orElseThrow();

    // Check if user has permission to leave the project
    UserProjectRole userProjectRole =
        userProjectRoleRepository.findByUserAndProject(user, project).orElseThrow();
    if (userProjectRole.getRole().equals(Role.OWNER.name())) {
      throw new RuntimeException("Owner cannot leave the project, it must delete it");
    }

    userProjectRoleRepository.delete(userProjectRole);
    logger.info("User: {}, left project: {}", user.getEmail(), project.getName());
  }

  public void updateProjectMembers(String email, Long projectId, List<EmailRole> emailRoles) {
    User user = userRepository.findUserByEmail(email).orElseThrow();
    Project project = projectRepository.findById(projectId).orElseThrow();

    // Check if user has permission to update project member list
    UserProjectRole ownerProjectRole =
        userProjectRoleRepository.findByUserAndProject(user, project).orElseThrow();
    if (!ownerProjectRole.getRole().equals(Role.OWNER.name())) {
      throw new RuntimeException(
          "User does not have permission to update member list for the project");
    }

    ownerProjectRole.setLastEdited(new Date());
    userProjectRoleRepository.save(ownerProjectRole);

    // Update ProjectInvitedMembers
    List<EmailRole> pendingMembers = emailRoles.stream().filter(EmailRole::pending).toList();

    updateProjectInvitedMembers(pendingMembers, project);

    // Update UserProjectRole
    List<EmailRole> joinedMembers =
        emailRoles.stream().filter(emailRole -> !emailRole.pending()).toList();

    updateUserProjectRole(joinedMembers, project);
  }

  public void updateUserProjectRole(List<EmailRole> emailRoles, Project project) {
    List<UserProjectRole> allUserProjectRoles = userProjectRoleRepository.findAllByProject(project);
    for (UserProjectRole userProjectRole : allUserProjectRoles) {
      EmailRole matchingEmailRole =
          emailRoles.stream()
              .filter(emailRole -> userProjectRole.getUser().getEmail().equals(emailRole.email()))
              .findFirst()
              .orElse(null);
      if (matchingEmailRole != null) {
        if (!userProjectRole
            .getRole()
            .equals(Role.getRoleByValue(matchingEmailRole.role()).name())) {
          userProjectRole.setRole(Role.getRoleByValue(matchingEmailRole.role()).name());
          userProjectRoleRepository.save(userProjectRole);
          emailRoles.remove(matchingEmailRole);
        }
      } else {
        if (!userProjectRole.getRole().equals(Role.OWNER.name())) {
          userProjectRoleRepository.delete(userProjectRole);
        }
      }
    }
  }

  private void updateProjectInvitedMembers(List<EmailRole> emailRolesList, Project project) {
    List<EmailRole> emailRoles = new ArrayList<>(emailRolesList);
    List<ProjectInvitedMembers> projectInvitedMembers =
        projectInvitedMembersRepository.findAllProjectInvitedMembersByProject(project);
    for (ProjectInvitedMembers projectInvitedMember : projectInvitedMembers) {
      EmailRole matchingEmailRole =
          emailRoles.stream()
              .filter(
                  emailRole ->
                      projectInvitedMember.getInvitedMembers().getEmail().equals(emailRole.email()))
              .findFirst()
              .orElse(null);
      if (matchingEmailRole != null) {
        if (!projectInvitedMember.getType().equals(matchingEmailRole.role())) {
          projectInvitedMember.setType(matchingEmailRole.role());
          projectInvitedMembersRepository.save(projectInvitedMember);
          emailRoles.remove(matchingEmailRole);
        }
      } else {
        projectInvitedMembersRepository.delete(projectInvitedMember);
      }
    }

    // add remaining email roles to project invited members
    for (EmailRole emailRole : emailRoles) {
      Optional<InvitedMembers> invitedMemberOptional =
          invitedMembersRepository.findInvitedMembersByEmail(emailRole.email());
      InvitedMembers invitedMember;
      if (invitedMemberOptional.isPresent()) {
        invitedMember = invitedMemberOptional.get();
      } else {
        invitedMember = new InvitedMembers(emailRole.email());
        invitedMembersRepository.save(invitedMember);
      }

      // Check if the InvitedMember is already associated with the project
      Optional<ProjectInvitedMembers> existingProjectInvitedMemberOptional =
          projectInvitedMembersRepository.findByInvitedMembersAndProject(invitedMember, project);
      if (existingProjectInvitedMemberOptional.isEmpty()) {
        String inviteLink = UUID.randomUUID().toString();
        ProjectInvitedMembers newProjectInvitedMember =
            new ProjectInvitedMembers(invitedMember, project, inviteLink, emailRole.role());
        projectInvitedMembersRepository.save(newProjectInvitedMember);

        // Send email to the new invited member
        String inviteLinkUrl =
            backendUrl + "/api/project/" + project.getId() + "/invite/" + inviteLink;
        mailService.sendNewMail(
            emailRole.email(),
            "Someone invited you to a project!",
            inviteLinkHtml(project.getName(), inviteLinkUrl));
        loggingService.saveLogToDatabase(
            new Log(
                "Send invite to project to email: %s".formatted(emailRole.email()),
                LogLevel.INFO,
                LocalDateTime.now()));
      }
    }
  }

  public FolderDTO getFilesAndFolders(Folder folder, boolean fetchContent) {
    List<File> files = fileRepository.findAllByFolder(folder);
    List<FileDTO> fileDTOs =
        files.stream()
            .map(
                file ->
                    new FileDTO(
                        file.getId(),
                        file.getName(),
                        fetchContent ? file.getContent() : "",
                        folder.getId()))
            .collect(Collectors.toList());

    List<Folder> childFolders = folderRepository.findAllByParentFolder(folder);
    List<FolderDTO> childFolderDTOs =
        childFolders.stream()
            .map(childFolder -> this.getFilesAndFolders(childFolder, fetchContent))
            .collect(Collectors.toList());

    return new FolderDTO(
        folder.getId(),
        folder.getName(),
        folder.getParentFolder() != null ? folder.getParentFolder().getId() : null,
        fileDTOs,
        childFolderDTOs);
  }

  public File getFileForUser(String email, Long projectId, Long fileId) {
    User user = userRepository.findUserByEmail(email).orElseThrow();
    Project project = projectRepository.findById(projectId).orElseThrow();
    File file = fileRepository.findById(fileId).orElseThrow();

    // check if file is part of the project
    // Traverse up the parent folders until the root folder is reached
    Folder rootFolder = file.getFolder();
    while (rootFolder.getParentFolder() != null) {
      rootFolder = rootFolder.getParentFolder();
    }

    // Check if the root folder is the same as the project's parent folder
    if (!rootFolder.getId().equals(project.getParentFolder().getId())) {
      loggingService.saveLogToDatabase(
          new Log(
              "User: %s tried to access file: %s that does not belong to the project: %s"
                  .formatted(email, file.getName(), project.getName()),
              LogLevel.INFO,
              LocalDateTime.now()));
      throw new RuntimeException("The file does not belong to the project");
    }

    // Check if user has at least viewer permissions to view the project
    UserProjectRole userProjectRole =
        userProjectRoleRepository.findByUserAndProject(user, project).orElseThrow();
    Role userRole = Role.valueOf(userProjectRole.getRole().toUpperCase());

    if (userRole.getValue() < Role.VIEWER.getValue()) {
      throw new RuntimeException("User does not have permission to view the project");
    }

    return file;
  }

  public File updateFileForProject(String email, Long projectId, Long fileId, String content) {
    User user = userRepository.findUserByEmail(email).orElseThrow();
    Project project = projectRepository.findById(projectId).orElseThrow();

    UserProjectRole userProjectRole =
        userProjectRoleRepository.findByUserAndProject(user, project).orElseThrow();
    Role userRole = Role.valueOf(userProjectRole.getRole().toUpperCase());

    if (userRole.getValue() < Role.EDITOR.getValue()) {
      throw new RuntimeException("User does not have permission to modify the file");
    }

    File file = fileRepository.findById(fileId).orElseThrow();
    file.setContent(content);
    fileRepository.save(file);
    return file;
  }

  public File updateFileNameForProject(
      String email, Long projectId, Long fileId, String newFileName) {
    User user = userRepository.findUserByEmail(email).orElseThrow();
    Project project = projectRepository.findById(projectId).orElseThrow();

    UserProjectRole userProjectRole =
        userProjectRoleRepository.findByUserAndProject(user, project).orElseThrow();
    Role userRole = Role.valueOf(userProjectRole.getRole().toUpperCase());

    if (userRole.getValue() < Role.EDITOR.getValue()) {
      throw new RuntimeException("User does not have permission to modify the file");
    }

    File file = fileRepository.findById(fileId).orElseThrow();
    file.setName(newFileName);
    fileRepository.save(file);
    return file;
  }

  public Folder updateFolderNameForProject(
      String email, Long projectId, Long folderId, String newFolderName) {
    User user = userRepository.findUserByEmail(email).orElseThrow();
    Project project = projectRepository.findById(projectId).orElseThrow();

    UserProjectRole userProjectRole =
        userProjectRoleRepository.findByUserAndProject(user, project).orElseThrow();
    Role userRole = Role.valueOf(userProjectRole.getRole().toUpperCase());

    if (userRole.getValue() < Role.EDITOR.getValue()) {
      throw new RuntimeException("User does not have permission to modify the file");
    }

    Folder folder = folderRepository.findById(folderId).orElseThrow();
    folder.setName(newFolderName);
    folderRepository.save(folder);
    return folder;
  }

  public Project deleteFileForProject(String email, Long projectId, Long fileId) {
    // Check if the user has the necessary permissions to delete the file
    if (!canUserEditResource(email, projectId, fileId, ResourceType.FILE)) {
      throw new NoPermissionException("User does not have permission to delete the file");
    }

    // Delete the file
    fileRepository.deleteById(fileId);
    return projectRepository.findProjectById(projectId).get();
  }

  public Project deleteFolderForProject(String email, Long projectId, Long folderId) {
    // Check if the user has the necessary permissions to delete the folder
    if (!canUserEditResource(
        email,
        projectId,
        folderId,
        ResourceType.FOLDER)) { // Assuming the same permissions apply for folders
      throw new NoPermissionException("User does not have permission to delete the folder");
    }

    // Delete the folder and its contents recursively
    Folder folder = folderRepository.findById(folderId).orElseThrow();
    deleteFolderAndContents(folder);
    return projectRepository.findProjectById(projectId).get();
  }

  public FolderDTO moveFolderForProject(
      String email, Long projectId, Long folderId, Long newParentFolderId) {
    // Check if the user has the necessary permissions to move the file
    if (!canUserEditResource(email, projectId, folderId, ResourceType.FOLDER)) {
      throw new NoPermissionException("User does not have permission to move the folder");
    }

    if (folderId.equals(newParentFolderId)) {
      throw new RuntimeException("The folder cannot be moved to itself");
    }

    Project project = projectRepository.findById(projectId).orElseThrow();
    Folder folder = folderRepository.findById(folderId).orElseThrow();
    Folder newParentFolder = folderRepository.findById(newParentFolderId).orElseThrow();

    // check if folder is part of the project
    Folder rootFolder = folder;
    while (rootFolder.getParentFolder() != null) {
      rootFolder = rootFolder.getParentFolder();
    }

    // Check if the root folder is the same as the project's parent folder
    if (!rootFolder.getId().equals(project.getParentFolder().getId())) {
      throw new RuntimeException("The folder does not belong to the project");
    }

    // check if newParentFolder is part of the project and it's also not child of
    // folder
    rootFolder = newParentFolder;
    while (rootFolder.getParentFolder() != null) {
      if (rootFolder.getParentFolder().getId().equals(folder.getId())) {
        throw new RuntimeException("The new parent folder cannot be a child of the folder");
      }
      rootFolder = rootFolder.getParentFolder();
    }

    if (!rootFolder.getId().equals(project.getParentFolder().getId())) {
      throw new RuntimeException("The new parent folder does not belong to the project");
    }

    folder.setParentFolder(newParentFolder);
    folderRepository.save(folder);
    return getFilesAndFolders(project.getParentFolder(), false);
  }

  public FolderDTO moveFileForProject(
      String email, Long projectId, Long fileId, Long newParentFolderId) {
    // Check if the user has the necessary permissions to move the file
    if (!canUserEditResource(email, projectId, fileId, ResourceType.FILE)) {
      throw new NoPermissionException("User does not have permission to move the file");
    }

    Project project = projectRepository.findById(projectId).orElseThrow();
    File file = fileRepository.findById(fileId).orElseThrow();
    Folder newParentFolder = folderRepository.findById(newParentFolderId).orElseThrow();

    // check if folder is part of the project
    Folder rootFolder = newParentFolder;
    while (rootFolder.getParentFolder() != null) {
      rootFolder = rootFolder.getParentFolder();
    }

    // Check if the root folder is the same as the project's parent folder
    if (!rootFolder.getId().equals(project.getParentFolder().getId())) {
      throw new RuntimeException("The folder does not belong to the project");
    }

    file.setFolder(newParentFolder);
    fileRepository.save(file);

    return getFilesAndFolders(project.getParentFolder(), false);
  }

  private void deleteFolderAndContents(Folder folder) {
    // Delete child folders recursively
    List<Folder> childFolders = folderRepository.findAllByParentFolder(folder);
    for (Folder childFolder : childFolders) {
      deleteFolderAndContents(childFolder);
    }

    // Delete files in the folder
    List<File> files = fileRepository.findAllByFolder(folder);
    fileRepository.deleteAll(files);

    // Delete the folder itself
    folderRepository.delete(folder);
  }

  public boolean hasUserPermissionsToEditProject(Long id, String email) {
    User user = userRepository.findUserByEmail(email).orElseThrow();
    Project project = projectRepository.findById(id).orElseThrow();
    UserProjectRole userProjectRole =
        userProjectRoleRepository.findByUserAndProject(user, project).orElseThrow();
    Role userRole = Role.valueOf(userProjectRole.getRole().toUpperCase());

    return userRole.getValue() >= Role.EDITOR.getValue();
  }

  public boolean canUserViewProject(
      String email, Long projectId, Long resourceId, ResourceType resourceType) {
    return hasUserPermission(email, projectId, resourceId, Role.VIEWER, resourceType);
  }

  public boolean canUserViewResource(
      String email, Long projectId, Long resourceId, ResourceType resourceType) {
    return hasUserPermission(email, projectId, resourceId, Role.VIEWER, resourceType);
  }

  public boolean canUserEditResource(
      String email, Long projectId, Long resourceId, ResourceType resourceType) {
    return hasUserPermission(email, projectId, resourceId, Role.EDITOR, resourceType);
  }

  private boolean hasUserPermission(
      String email, Long projectId, Long resourceId, Role requiredRole, ResourceType resourceType) {
    Optional<User> userOptional = userRepository.findUserByEmail(email);
    Optional<Project> projectOptional = projectRepository.findById(projectId);

    if (userOptional.isEmpty() && projectOptional.isEmpty()) {
      return false;
    }

    User user = userOptional.get();
    Project project = projectOptional.get();

    // Check if resource is part of the project
    Folder rootFolder;
    if (resourceType.name().equals("FILE")) {
      Optional<File> fileOptional = fileRepository.findById(resourceId);
      if (fileOptional.isEmpty()) {
        return false;
      }
      File file = fileOptional.get();
      rootFolder = file.getFolder();
    } else if (resourceType.name().equals("FOLDER")) {
      Optional<Folder> folderOptional = folderRepository.findById(resourceId);
      if (folderOptional.isEmpty()) {
        return false;
      }
      Folder folder = folderOptional.get();
      rootFolder = folder.getParentFolder();
    } else if (resourceType.name().equals("PROJECT")) {
      rootFolder = project.getParentFolder();
    } else {
      throw new IllegalArgumentException("Invalid resource type: " + resourceType);
    }

    while (rootFolder.getParentFolder() != null) {
      rootFolder = rootFolder.getParentFolder();
    }
    if (!rootFolder.getId().equals(project.getParentFolder().getId())) {
      return false;
    }

    // Check if the user has the necessary permissions
    Optional<UserProjectRole> userProjectRoleOptional =
        userProjectRoleRepository.findByUserAndProject(user, project);
    if (userProjectRoleOptional.isEmpty()) {
      return false;
    }

    Role userRole = Role.valueOf(userProjectRoleOptional.get().getRole().toUpperCase());

    return userRole.getValue() >= requiredRole.getValue();
  }
}
