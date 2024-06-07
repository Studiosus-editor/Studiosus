package lt.sus.Studiosus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class File {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "file_id")
  private Long id;

  @Column(name = "file_name")
  private String name;

  @Size(max = 50000)
  @Column(name = "file_content", columnDefinition = "TEXT")
  private String content;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "folder_id")
  private Folder folder;

  public File(String name, String content, Folder folder) {
    this.name = name;
    this.content = content;
    this.folder = folder;
  }
}
