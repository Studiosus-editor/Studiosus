package lt.sus.Studiosus.model;

import jakarta.persistence.*;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Project {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "project_id")
  private Long id;

  @Column(name = "project_name")
  private String name;

  @Column(name = "project_phase")
  private String phase;

  @Column(name = "viewer_link", unique = true)
  private String viewerLink;

  @Column(name = "editor_link", unique = true)
  private String editorLink;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "folder_id")
  private Folder parentFolder;

  @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProjectInvitedMembers> projectInvitedMembers;

  public Project(
      String name, String phase, String viewerLink, String editorLink, Folder parentFolder) {
    this.name = name;
    this.phase = phase;
    this.viewerLink = viewerLink;
    this.editorLink = editorLink;
    this.parentFolder = parentFolder;
  }
}
