package lt.sus.Studiosus.repository;

import java.util.List;
import java.util.Optional;
import lt.sus.Studiosus.model.Template;
import lt.sus.Studiosus.model.User;
import lt.sus.Studiosus.model.UserTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserTemplateRepository extends JpaRepository<UserTemplate, Long> {

  List<UserTemplate> findAllByUser(User user);

  List<UserTemplate> findAllByUserNot(User user);

  Optional<UserTemplate> findUserTemplateByTemplate(Template template);

  @Query("SELECT ut FROM UserTemplate ut WHERE ut.template.phase = :phase")
  List<UserTemplate> findAllByTemplatePhase(@Param("phase") String phase);
}
