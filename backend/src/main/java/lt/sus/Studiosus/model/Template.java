package lt.sus.Studiosus.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Template {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "template_id")
  private Long id;

  @Column(name = "template_name")
  private String name;

  @Column(name = "template_description", length = 600)
  private String description;

  @Column(name = "template_phase")
  private String phase;

  @Column(name = "template_comment")
  private String comment;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "folder_id")
  private Folder parentFolder;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;
}
