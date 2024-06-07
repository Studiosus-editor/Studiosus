package lt.sus.Studiosus.repository;

import java.util.List;
import lt.sus.Studiosus.model.File;
import lt.sus.Studiosus.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
  List<File> findAllByFolder(Folder folder);
}
