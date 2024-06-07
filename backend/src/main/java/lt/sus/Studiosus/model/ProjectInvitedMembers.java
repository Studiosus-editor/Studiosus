package lt.sus.Studiosus.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class ProjectInvitedMembers {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne private InvitedMembers invitedMembers;

  @ManyToOne(cascade = CascadeType.ALL)
  private Project project;

  @Column(name = "invite_link", unique = true)
  private String inviteLink;

  @Column(name = "invite_type")
  private Integer type;

  public ProjectInvitedMembers(
      InvitedMembers invitedMembers, Project project, String inviteLink, Integer type) {
    this.invitedMembers = invitedMembers;
    this.project = project;
    this.inviteLink = inviteLink;
    this.type = type;
  }
}
