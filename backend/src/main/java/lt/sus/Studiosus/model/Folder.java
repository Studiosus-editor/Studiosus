package lt.sus.Studiosus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;
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

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "parent_id")
  private Folder parentFolder;

  @JsonIgnore
  @OneToMany(mappedBy = "parentFolder", cascade = CascadeType.ALL)
  private List<Folder> childFolders;

  @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL)
  private List<File> files;

  public Folder(String name, Folder parentFolder) {
    this.name = name;
    this.parentFolder = parentFolder;
  }
}
