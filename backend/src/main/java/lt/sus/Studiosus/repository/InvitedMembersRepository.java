package lt.sus.Studiosus.repository;

import java.util.Optional;
import lt.sus.Studiosus.model.InvitedMembers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvitedMembersRepository extends JpaRepository<InvitedMembers, Long> {
  Optional<InvitedMembers> findInvitedMembersByEmail(String email);
}
