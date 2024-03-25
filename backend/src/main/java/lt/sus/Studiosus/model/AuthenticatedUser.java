package lt.sus.Studiosus.model;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthenticatedUser implements UserDetails {

  private final String password;
  private final String username;
  private final String email;
  private final String avatar;
  private final boolean accountNonExpired;
  private final boolean accountNonLocked;
  private final boolean credentialsNonExpired;
  private final boolean enabled;
  private final List<GrantedAuthority> authorities;

  public AuthenticatedUser(
      String username,
      String email,
      String avatar,
      String password,
      List<GrantedAuthority> authorities) {
    this(username, password, email, avatar, true, true, true, true, authorities);
  }

  public AuthenticatedUser(
      String username,
      String password,
      String email,
      String avatar,
      boolean accountNonExpired,
      boolean accountNonLocked,
      boolean credentialsNonExpired,
      boolean enabled,
      List<GrantedAuthority> authorities) {
    if (((username == null) || username.isEmpty())
        || (password == null)
        || password.isEmpty()
        || (email == null)
        || email.isEmpty()) {
      throw new IllegalArgumentException("Cannot pass null or empty values to constructor");
    }
    this.username = username;
    this.password = password;
    this.email = email;
    this.avatar = avatar;
    this.accountNonExpired = accountNonExpired;
    this.accountNonLocked = accountNonLocked;
    this.credentialsNonExpired = credentialsNonExpired;
    this.enabled = enabled;
    this.authorities = authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  public String getEmail() {
    return email;
  }

  public String getAvatar() {
    return avatar;
  }

  @Override
  public boolean isAccountNonExpired() {
    return accountNonExpired;
  }

  @Override
  public boolean isAccountNonLocked() {
    return accountNonLocked;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return credentialsNonExpired;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }
}
