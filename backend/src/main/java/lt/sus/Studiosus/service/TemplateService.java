package lt.sus.Studiosus.service;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lt.sus.Studiosus.dto.FileDTO;
import lt.sus.Studiosus.dto.FolderDTO;
import lt.sus.Studiosus.dto.TemplateDTO;
import lt.sus.Studiosus.dto.TemplateSearchRequest;
import lt.sus.Studiosus.model.File;
import lt.sus.Studiosus.model.Folder;
import lt.sus.Studiosus.model.Project;
import lt.sus.Studiosus.model.Template;
import lt.sus.Studiosus.model.User;
import lt.sus.Studiosus.model.UserProjectRole;
import lt.sus.Studiosus.model.enums.Role;
import lt.sus.Studiosus.model.enums.TemplatePhase;
import lt.sus.Studiosus.repository.FileRepository;
import lt.sus.Studiosus.repository.FolderRepository;
import lt.sus.Studiosus.repository.ProjectRepository;
import lt.sus.Studiosus.repository.TemplateRepository;
import lt.sus.Studiosus.repository.UserProjectRoleRepository;
import lt.sus.Studiosus.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class TemplateService {

  private final TemplateRepository templateRepository;
  private final FolderRepository folderRepository;
  private final FileRepository fileRepository;
  private final ProjectRepository projectRepository;
  private final UserRepository userRepository;
  private final UserProjectRoleRepository userProjectRoleRepository;

  public TemplateService(
      TemplateRepository templateRepository,
      FolderRepository folderRepository,
      FileRepository fileRepository,
      ProjectRepository projectRepository,
      UserRepository userRepository,
      UserProjectRoleRepository userProjectRoleRepository) {
    this.templateRepository = templateRepository;
    this.folderRepository = folderRepository;
    this.fileRepository = fileRepository;
    this.projectRepository = projectRepository;
    this.userRepository = userRepository;
    this.userProjectRoleRepository = userProjectRoleRepository;
  }

  public Project createProjectFromTemplate(String email, String projectName, Long templateId) {
    User user = userRepository.findUserByEmail(email).orElseThrow();

    // Retrieve the template from the database using the templateId
    Template template =
        templateRepository
            .findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template not found"));

    // Duplicate the template's contents into a new project
    Project project = duplicateTemplateToProject(template, user, projectName);

    // Save the new project to the database
    projectRepository.save(project);

    // Assign the user as the owner of the new project
    UserProjectRole userProjectRole = new UserProjectRole(user, project, Role.OWNER.name());
    userProjectRoleRepository.save(userProjectRole);

    return project;
  }

  private Project duplicateTemplateToProject(Template template, User user, String projectName) {
    // Create a new project with the same name and description as the template
    Project project = new Project();
    project.setName(projectName);
    project.setPhase("Finalized");
    project.setViewerLink(UUID.randomUUID().toString());
    project.setEditorLink(UUID.randomUUID().toString());

    // Duplicate the template's parent folder into the new project
    Folder parentFolder = duplicateFolder(template.getParentFolder());
    project.setParentFolder(parentFolder);

    return project;
  }

  public Template duplicateProjectToTemplate(
      Project project, User user, String name, String description) {
    Template template = new Template();
    template.setName(name);
    template.setDescription(description);
    template.setPhase(TemplatePhase.PENDING.name());
    template.setUser(user);

    // Duplicate the project's parent folder into the template
    Folder templateParentFolder = duplicateFolder(project.getParentFolder());
    template.setParentFolder(templateParentFolder);

    templateRepository.save(template);

    return template;
  }

  private Folder duplicateFolder(Folder folder) {
    // Create a new folder with the same name as the original folder
    Folder newFolder = new Folder();
    newFolder.setName(folder.getName());

    // Initialize the files list
    newFolder.setFiles(new ArrayList<>());
    newFolder.setChildFolders(new ArrayList<>());

    // Save the new folder to the database
    folderRepository.save(newFolder);

    // Duplicate the files of the original folder into the new folder
    for (File file : folder.getFiles()) {
      File newFile = duplicateFile(file, newFolder);
      newFolder.getFiles().add(newFile);
      fileRepository.save(newFile);
    }
    // Duplicate the child folders of the original folder into the new folder
    for (Folder childFolder : folder.getChildFolders()) {
      Folder newChildFolder = duplicateFolder(childFolder);
      newChildFolder.setParentFolder(newFolder);
      newFolder.getChildFolders().add(newChildFolder);
      folderRepository.save(newChildFolder);
    }

    return newFolder;
  }

  private File duplicateFile(File file, Folder parentFolder) {
    File newFile = new File();
    newFile.setName(file.getName());
    newFile.setContent(file.getContent());
    newFile.setFolder(parentFolder);
    return newFile;
  }

  public FolderDTO getTemplateFolders(Long projectId) throws RuntimeException {
    Template template = templateRepository.findById(projectId).orElseThrow();
    Folder rootFolder = template.getParentFolder();

    return getFilesAndFolders(rootFolder);
  }

  public FolderDTO getFilesAndFolders(Folder folder) {
    List<File> files = fileRepository.findAllByFolder(folder);
    List<FileDTO> fileDTOs =
        files.stream()
            .map(
                file ->
                    new FileDTO(file.getId(), file.getName(), file.getContent(), folder.getId()))
            .collect(toList());

    List<Folder> childFolders = folderRepository.findAllByParentFolder(folder);
    List<FolderDTO> childFolderDTOs =
        childFolders.stream().map(this::getFilesAndFolders).collect(toList());

    return new FolderDTO(
        folder.getId(),
        folder.getName(),
        folder.getParentFolder() != null ? folder.getParentFolder().getId() : null,
        fileDTOs,
        childFolderDTOs);
  }

  public File getFileForUser(Long projectId, Long fileId) {
    Template template = templateRepository.findById(projectId).orElseThrow();
    File file = fileRepository.findById(fileId).orElseThrow();

    // check if file is part of the project
    // Traverse up the parent folders until the root folder is reached
    Folder rootFolder = file.getFolder();
    while (rootFolder.getParentFolder() != null) {
      rootFolder = rootFolder.getParentFolder();
    }

    // Check if the root folder is the same as the project's parent folder
    if (!rootFolder.getId().equals(template.getParentFolder().getId())) {
      throw new RuntimeException("The file does not belong to the template");
    }

    return file;
  }

  public List<TemplateDTO> searchTemplates(TemplateSearchRequest searchRequest) {
    String title = searchRequest.title();
    String username = searchRequest.username();

    if (title == null && username == null) {
      return new ArrayList<>();
    }

    List<Template> templates;
    if (title != null) {
      templates =
          templateRepository.findByNameStartingWithIgnoreCaseAndPhase(
              title, TemplatePhase.APPROVED.name());
    } else {
      templates = templateRepository.findAllByPhase(TemplatePhase.APPROVED.name());
    }

    List<TemplateDTO> templateDTOs =
        templates.stream()
            .map(
                template ->
                    new TemplateDTO(
                        template.getId(),
                        template.getName(),
                        template.getDescription(),
                        template.getUser().getUsername()))
            .toList();

    if (username == null) {
      return templateDTOs;
    }

    List<TemplateDTO> foundTemplates = new ArrayList<>();
    for (TemplateDTO templateDTO : templateDTOs) {
      if (templateDTO.ownerName().toLowerCase().startsWith(username.toLowerCase())) {
        foundTemplates.add(templateDTO);
      }
    }

    return foundTemplates;
  }
}
