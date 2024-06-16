package lt.sus.Studiosus.repository;

import java.util.List;
import java.util.Optional;
import lt.sus.Studiosus.model.Project;
import lt.sus.Studiosus.model.User;
import lt.sus.Studiosus.model.UserProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface UserProjectRoleRepository extends JpaRepository<UserProjectRole, Long> {
  Optional<UserProjectRole> findUserProjectRoleById(Long id);

  List<UserProjectRole> findByUser(User user);

  List<UserProjectRole> findAllByProject(Project project);

  Optional<UserProjectRole> findByUserAndProject(User user, Project project);

  Optional<UserProjectRole> findUserProjectRoleByUserAndProject(User user, Project project);

  void deleteAllByProjectId(Long projectId);

  @Transactional
  @Modifying
  @Query(
      "DELETE FROM UserProjectRole upr WHERE upr.user = :user AND upr.role = :role AND upr.project.phase = :projectPhase")
  void deleteAllByUserAndRoleAndProjectPhase(User user, String role, String projectPhase);
}
