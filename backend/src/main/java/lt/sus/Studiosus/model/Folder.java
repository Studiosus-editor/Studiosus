package lt.sus.Studiosus.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Folder {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "folder_id")
  private Long id;

  @Column(name = "folder_name")
  private String name;

  @Column(name = "parent_id")
  private Integer parentId;
}
