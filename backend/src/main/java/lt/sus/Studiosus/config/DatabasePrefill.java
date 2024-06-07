package lt.sus.Studiosus.config;

import lt.sus.Studiosus.model.User;
import lt.sus.Studiosus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DatabasePrefill {

  @Value("${PREFILL_DATABASE:false}")
  private boolean prefillDatabase;

  // Demo normal user
  @Value("${PREFILL_username:DEMO user}")
  private String username;

  @Value("${PREFILL_email:user@studiosus.lt}")
  private String email;

  @Value("${PREFILL_password:pass}")
  private String password;

  // Demo admin user
  @Value("${PREFILL_username2:ADMIN user}")
  private String username2;

  @Value("${PREFILL_email2:admin@studiosus.lt}")
  private String email2;

  @Value("${PREFILL_password2:pass}")
  private String password2;

  private final PasswordEncoder encoder;

  public DatabasePrefill(PasswordEncoder encoder) {
    this.encoder = encoder;
  }

  @Bean
  public CommandLineRunner prefill(UserRepository userRepository) {
    return args -> {
      if (prefillDatabase) {
        User user = new User(username, email, encoder.encode(password), "ROLE_USER");

        if (userRepository.findAllUserByEmail(email).isEmpty()) {
          userRepository.save(user);
        }

        User user2 = new User(username2, email2, encoder.encode(password2), "ROLE_ADMIN");

        if (userRepository.findAllUserByEmail(email2).isEmpty()) {
          userRepository.save(user2);
        }
      }
    };
  }
}
