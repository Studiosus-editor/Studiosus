package lt.sus.Studiosus.repository;

import java.util.List;
import java.util.Optional;
import lt.sus.Studiosus.model.Project;
import lt.sus.Studiosus.model.User;
import lt.sus.Studiosus.model.UserProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProjectRoleRepository extends JpaRepository<UserProjectRole, Long> {
  Optional<UserProjectRole> findUserProjectRoleById(Long id);

  List<UserProjectRole> findByUser(User user);

  Optional<UserProjectRole> findByUserAndProject(User user, Project project);
}
