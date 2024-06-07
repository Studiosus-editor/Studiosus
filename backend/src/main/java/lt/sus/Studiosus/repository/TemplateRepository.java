package lt.sus.Studiosus.repository;

import java.util.List;
import lt.sus.Studiosus.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {
  List<Template> findAllByPhase(String phase);

  List<Template> findByNameStartingWithIgnoreCaseAndPhase(String name, String phase);
}
