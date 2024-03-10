package lt.sus.Studiosus.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Autowired private Environment env;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    if (isOAuthEnabled()) {
      return http.authorizeHttpRequests(
              auth -> {
                auth.requestMatchers("/profile").authenticated();
                auth.anyRequest().permitAll();
              })
          .oauth2Login(form -> form.loginPage("/login").permitAll())
          .formLogin(form -> form.loginPage("/login").permitAll())
          .build();
    } else {
      return http.authorizeHttpRequests(
              auth -> {
                auth.requestMatchers("/profile").authenticated();
                auth.anyRequest().permitAll();
              })
          .formLogin(form -> form.loginPage("/login").permitAll())
          .build();
    }
  }

  private boolean isOAuthEnabled() {
    return (env.containsProperty("github.client-id")
            && env.containsProperty("github.client-secret"))
        || (env.containsProperty("gitlab.client-id")
            && env.containsProperty("gitlab.client-secret"));
  }
}
