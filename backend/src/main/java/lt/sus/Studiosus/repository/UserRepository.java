package lt.sus.Studiosus.repository;

import java.util.Optional;
import lt.sus.Studiosus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findUserByEmail(String email);

  Optional<User> findUserByGithubId(String githubId);

  Optional<User> findUserByGitlabId(String gitlabId);
}
