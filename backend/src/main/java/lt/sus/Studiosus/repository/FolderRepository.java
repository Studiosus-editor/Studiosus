package lt.sus.Studiosus.repository;

import java.util.List;
import lt.sus.Studiosus.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
  List<Folder> findAllByParentFolder(Folder parentFolder);
}
