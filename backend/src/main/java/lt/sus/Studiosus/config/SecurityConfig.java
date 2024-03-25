package lt.sus.Studiosus.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lt.sus.Studiosus.service.OAuth2UserService;
import lt.sus.Studiosus.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.header.writers.ContentSecurityPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  // Secrets.properties file configuration
  @Autowired private Environment env;

  // Local database configuration
  @Bean
  EmbeddedDatabase datasource() {
    return new EmbeddedDatabaseBuilder()
        .setType(EmbeddedDatabaseType.H2)
        .setName("studiosus")
        .build();
  }

  // Password encoding
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // Authentication
  @Bean
  UserDetailsService userDetailsService() {
    return new UserDetailsServiceImpl();
  }

  @Bean
  OAuth2UserService oAuth2UserService() {
    return new OAuth2UserService();
  }

  @Bean
  DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService());
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  // Authorization
  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    if (isOAuthEnabled()) {
      return http.csrf(
              csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/login", "/register"))
          .authorizeHttpRequests(
              auth -> {
                auth.requestMatchers("/profile").authenticated();
                auth.anyRequest().permitAll();
              })
          .headers(
              headers ->
                  headers.addHeaderWriter(
                      new ContentSecurityPolicyHeaderWriter("frame-ancestors 'self'")))
          .oauth2Login(
              login ->
                  login
                      .loginPage("/login")
                      .permitAll()
                      .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService())))
          .formLogin(
              form ->
                  form.loginPage("/login")
                      .permitAll()
                      .failureHandler(authenticationFailureHandler()))
          .logout(
              logout -> {
                logout.invalidateHttpSession(true);
                logout.deleteCookies("JSESSIONID");
              })
          .build();
    } else {
      return http.csrf(
              csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/login", "/register"))
          .authorizeHttpRequests(
              auth -> {
                auth.requestMatchers("/profile").authenticated();
                auth.anyRequest().permitAll();
              })
          .headers(
              headers ->
                  headers.addHeaderWriter(
                      new ContentSecurityPolicyHeaderWriter("frame-ancestors 'self'")))
          .formLogin(
              form ->
                  form.loginPage("/login")
                      .permitAll()
                      .failureHandler(authenticationFailureHandler()))
          .logout(
              logout -> {
                logout.invalidateHttpSession(true);
                logout.deleteCookies("JSESSIONID");
              })
          .build();
    }
  }

  @Bean
  public AuthenticationFailureHandler authenticationFailureHandler() {
    return new CustomAuthenticationFailureHandler();
  }

  private boolean isOAuthEnabled() {
    return (env.containsProperty("github.client-id")
            && env.containsProperty("github.client-secret"))
        || (env.containsProperty("gitlab.client-id")
            && env.containsProperty("gitlab.client-secret"));
  }
}

class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException, ServletException {
    String errorMessage = "Invalid username or password.";
    if (exception instanceof LockedException) {
      errorMessage = "Your account is locked. Please contact support.";
    } else if (exception instanceof DisabledException) {
      errorMessage = "Your account is disabled. Please contact support.";
    } else if (exception instanceof BadCredentialsException) {
      errorMessage = "Invalid username or password.";
    } else if (exception instanceof AccountExpiredException) {
      errorMessage = "Your account has expired. Please contact support.";
    }

    String targetUrl =
        "/login?error=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8.toString());
    response.sendRedirect(targetUrl);
  }
}
