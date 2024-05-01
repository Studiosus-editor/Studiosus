package lt.sus.Studiosus.service;

import java.util.List;
import java.util.stream.Collectors;
import lt.sus.Studiosus.dto.ProjectRole;
import lt.sus.Studiosus.model.Project;
import lt.sus.Studiosus.model.User;
import lt.sus.Studiosus.model.UserProjectRole;
import lt.sus.Studiosus.repository.ProjectRepository;
import lt.sus.Studiosus.repository.UserProjectRoleRepository;
import lt.sus.Studiosus.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {
  private final UserRepository userRepository;
  private final ProjectRepository projectRepository;
  private final UserProjectRoleRepository userProjectRoleRepository;

  private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

  public ProjectService(
      UserRepository userRepository,
      ProjectRepository projectRepository,
      UserProjectRoleRepository userProjectRoleRepository) {
    this.userRepository = userRepository;
    this.projectRepository = projectRepository;
    this.userProjectRoleRepository = userProjectRoleRepository;
  }

  public Project createProjectForUser(String email, String projectName) {
    User user = userRepository.findUserByEmail(email).orElseThrow();
    Project project = new Project(projectName);
    // You can set additional project details here

    projectRepository.save(project);
    logger.info("Created project: {}", project);

    // Assign user a role (e.g., "Owner") in the project
    UserProjectRole userProjectRole = new UserProjectRole(user, project, "Owner");
    userProjectRoleRepository.save(userProjectRole);
    logger.info("Assigned role: {} to user: {} for project: {}", "Owner", user, project);

    return project;
  }

  public void deleteProjectForUser(String email, Long projectId) {
    User user = userRepository.findUserByEmail(email).orElseThrow();
    Project project = projectRepository.findById(projectId).orElseThrow();

    // Check if user has permission to delete the project
    UserProjectRole userProjectRole =
        userProjectRoleRepository.findByUserAndProject(user, project).orElseThrow();
    if (!userProjectRole.getRole().equals("Owner")) {
      throw new RuntimeException("User does not have permission to delete the project");
    }

    logger.info("starting deletion process");

    // Delete the project
    userProjectRoleRepository.deleteById(userProjectRole.getId());
    projectRepository.delete(project);
    logger.info("Deleted project: {}", project);
  }

  public List<ProjectRole> getProjectsForUser(User user) {
    List<UserProjectRole> userProjectRoles = userProjectRoleRepository.findByUser(user);
    logger.info("Fetched projects for user: {}", user.getEmail());
    return userProjectRoles.stream()
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
    if (!userProjectRole.getRole().equals("Owner")) {
      throw new RuntimeException("User does not have permission to delete the project");
    }

    project.setName(newName);
    projectRepository.save(project);
    return project.getName();
  }
}
