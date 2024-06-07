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
public class UserTemplate {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne private Project project;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(cascade = CascadeType.REMOVE)
  private Template template;

  @Temporal(TemporalType.TIMESTAMP)
  private Date createdDate;

  public UserTemplate(Template template, User user) {
    this.template = template;
    this.user = user;
    this.createdDate = new Date();
  }
}
