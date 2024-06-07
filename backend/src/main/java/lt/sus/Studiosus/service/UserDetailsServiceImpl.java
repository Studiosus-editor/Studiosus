package lt.sus.Studiosus.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lt.sus.Studiosus.model.AuthenticatedUser;
import lt.sus.Studiosus.model.User;
import lt.sus.Studiosus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
            throw new IllegalArgumentException("Email already in use");
          }
          existingUser.setPassword(user.getPassword());
          userRepository.save(existingUser);
        },
        () -> userRepository.save(user));
  }

  public User getUser(String email) throws IllegalArgumentException {
    return userRepository
        .findUserByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }

  public static String getEmail(Authentication authentication) throws IllegalArgumentException {
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new IllegalArgumentException("User is not authenticated");
    }
    if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
      return oauth2User.getAttribute("email");
    } else if (authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser) {
      return authenticatedUser.getEmail();
    }
    return "";
  }
}
