package lt.sus.Studiosus.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Invited_members")
public class InvitedMembers {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "invited_id")
  private Long id;

  @Column(name = "invited_email", unique = true)
  private String email;

  public InvitedMembers(String email) {
    this.email = email;
  }
}
