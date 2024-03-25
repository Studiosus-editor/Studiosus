package lt.sus.Studiosus.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lt.sus.Studiosus.model.AuthenticatedUser;
import lt.sus.Studiosus.model.User;
import lt.sus.Studiosus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  @Autowired private UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userRepository.findUserByEmail(email).orElse(null);
    if (user == null) {
      throw new UsernameNotFoundException("User not found with email: " + email);
    }

    List<GrantedAuthority> authorities =
        Arrays.stream(user.getRoles().split(","))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

    return new AuthenticatedUser(
        user.getUsername(), user.getEmail(), user.getAvatar(), user.getPassword(), authorities);
  }

  public void save(User user) throws IllegalArgumentException {
    Optional<User> databaseUser = userRepository.findUserByEmail(user.getEmail());

    databaseUser.ifPresentOrElse(
        existingUser -> {
          if (existingUser.getPassword() != null) {
            throw new IllegalArgumentException(
                "Email " + existingUser.getEmail() + " already in use.");
          }
          existingUser.setPassword(user.getPassword());
          userRepository.save(existingUser);
        },
        () -> userRepository.save(user));
  }
}
