package lt.sus.Studiosus.repository;

import java.util.Optional;
import lt.sus.Studiosus.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
  Optional<Project> findProjectById(Long id);
}
