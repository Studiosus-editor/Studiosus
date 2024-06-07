package lt.sus.Studiosus.repository;

import java.util.List;
import java.util.Optional;
import lt.sus.Studiosus.model.InvitedMembers;
import lt.sus.Studiosus.model.Project;
import lt.sus.Studiosus.model.ProjectInvitedMembers;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectInvitedMembersRepository
    extends JpaRepository<ProjectInvitedMembers, Long> {
  List<ProjectInvitedMembers> findAllProjectInvitedMembersByProject(Project project);

  Optional<ProjectInvitedMembers> findProjectInvitedMembersByInvitedMembersAndProject(
      InvitedMembers invitedMembers, Project project);

  Optional<ProjectInvitedMembers> findByInvitedMembersAndProject(
      InvitedMembers invitedMembers, Project project);

  Optional<ProjectInvitedMembers> findByInviteLink(String inviteLink);

  List<ProjectInvitedMembers> findAllByInvitedMembers(InvitedMembers invitedMembers);

  void deleteAllByProjectId(Long projectId);
}
