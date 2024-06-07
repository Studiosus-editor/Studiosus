package lt.sus.Studiosus.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lt.sus.Studiosus.model.Project;
import lt.sus.Studiosus.model.ProjectInvitedMembers;
import lt.sus.Studiosus.model.UserProjectRole;
import lt.sus.Studiosus.model.enums.Role;

@Getter
public class ProjectDetailsResponse {

  private Project project;
  private List<EmailRole> emailRole;

  public ProjectDetailsResponse(
      Project project,
      List<ProjectInvitedMembers> projectInvitedMembers,
      List<UserProjectRole> userProjectRoles) {
    this.project = project;
    this.emailRole = new ArrayList<>();
    for (ProjectInvitedMembers projectInvitedMember : projectInvitedMembers) {
      emailRole.add(
          new EmailRole(
              projectInvitedMember.getInvitedMembers().getEmail(),
              projectInvitedMember.getType(),
              true));
    }

    for (UserProjectRole userProjectRole : userProjectRoles) {
      emailRole.add(
          new EmailRole(
              userProjectRole.getUser().getEmail(),
              Role.getRoleByName(userProjectRole.getRole().toUpperCase()).getValue(),
              false));
    }
  }
}
