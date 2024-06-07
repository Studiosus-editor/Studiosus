package lt.sus.Studiosus.model;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class UserProjectRole {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(cascade = CascadeType.REMOVE)
  private Project project;

  @Column(nullable = false)
  private String role;

  @Temporal(TemporalType.TIMESTAMP)
  private Date lastEdited;

  public UserProjectRole(User user, Project project, String role) {
    this.user = user;
    this.project = project;
    this.role = role;
    this.lastEdited = new Date();
  }
}
