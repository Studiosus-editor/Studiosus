package lt.sus.Studiosus.repository;

import java.util.List;
import lt.sus.Studiosus.model.Template;
import lt.sus.Studiosus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {
  List<Template> findAllByPhase(String phase);

  List<Template> findByNameStartingWithIgnoreCaseAndPhase(String name, String phase);

  List<Template> findAllByUser(User user);

  List<Template> findAllByUserNot(User user);
}
