package lt.sus.Studiosus.model;

import jakarta.persistence.*;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Long id;

  @Column(name = "username")
  private String username;

  @Column(name = "password")
  private String password;

  @Column(name = "enabled")
  private String enabled;

  @Column(name = "email")
  private String email;

  @Column(name = "avatar")
  private String avatar;

  @Column(name = "roles")
  private String roles;

  @Column(name = "github_id")
  private String githubId;

  @Column(name = "gitlab_id")
  private String gitlabId;

  @Column(name = "reset_token", unique = true)
  private String resetToken;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<UserProjectRole> userProjectRoles;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<UserTemplate> userTemplates;

  public User(String username, String email, String avatar) {
    this.username = username;
    this.email = email;
    this.avatar = avatar;
  }

  public User(String username, String email, String password, String roles) {
    this.username = username;
    this.email = email;
    this.password = password;
    this.roles = roles;
  }
}
