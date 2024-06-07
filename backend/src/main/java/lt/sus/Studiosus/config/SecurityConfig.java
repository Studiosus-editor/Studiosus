package lt.sus.Studiosus.config;

import static lt.sus.Studiosus.service.UserDetailsServiceImpl.getEmail;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lt.sus.Studiosus.model.*;
import lt.sus.Studiosus.model.enums.Role;
import lt.sus.Studiosus.repository.*;
import lt.sus.Studiosus.service.OAuth2UserService;
import lt.sus.Studiosus.service.ProjectService;
import lt.sus.Studiosus.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.header.writers.ContentSecurityPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Value("${app.frontendUrl}")
  private String frontendUrl;

  // Secrets.properties file configuration
  @Autowired private Environment env;

  @Autowired private ProjectService projectService;
  @Autowired private UserRepository userRepository;
  @Autowired private ProjectRepository projectRepository;
  @Autowired private UserProjectRoleRepository userProjectRoleRepository;
  @Autowired private ProjectInvitedMembersRepository projectInvitedMembersRepository;
  @Autowired private InvitedMembersRepository invitedMembersRepository;

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
              csrf ->
                  csrf.ignoringRequestMatchers(
                      "/h2-console/**", "/login", "/register", "/api/**", "/ai/**"))
          .authorizeHttpRequests(
              auth -> {
                auth.requestMatchers("/profile", "/api/createProject").authenticated();
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
                      .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService()))
                      .successHandler(
                          (request, response, authentication) -> {
                            Cookie[] cookies = request.getCookies();
                            if (cookies != null) {
                              for (Cookie cookie : cookies) {
                                if (cookie.getName().equals("linkAndRole")) {
                                  // Decode the cookie value
                                  String decodedLinkAndRole =
                                      new String(Base64.getDecoder().decode(cookie.getValue()));

                                  // Parse the JSON string back to a map
                                  Map<String, String> linkAndRole =
                                      new ObjectMapper()
                                          .readValue(
                                              decodedLinkAndRole,
                                              new TypeReference<Map<String, String>>() {});

                                  String link = linkAndRole.get("link");
                                  Role role = Role.valueOf(linkAndRole.get("role"));

                                  // remove cookie
                                  cookie.setMaxAge(0);
                                  response.addCookie(cookie);

                                  if (assignRoleToTheProject(link, role, authentication)) {
                                    response.sendRedirect(
                                        frontendUrl
                                            + "/projects?status=addedToProjectSuccessfully");
                                    return;
                                  } else {
                                    response.sendRedirect(
                                        frontendUrl + "/projects?status=invalidLink");
                                    return;
                                  }
                                }
                              }
                            }

                            String referer = request.getHeader("Referer");
                            if (referer != null) {
                              response.sendRedirect(referer);
                            } else {
                              response.sendRedirect(frontendUrl);
                            }
                          }))
          .formLogin(
              form ->
                  form.loginPage("/login")
                      .permitAll()
                      .failureHandler(authenticationFailureHandler())
                      .successHandler(
                          (request, response, authentication) -> {
                            Cookie[] cookies = request.getCookies();
                            if (cookies != null) {
                              for (Cookie cookie : cookies) {
                                if (cookie.getName().equals("linkAndRole")) {
                                  // Decode the cookie value
                                  String decodedLinkAndRole =
                                      new String(Base64.getDecoder().decode(cookie.getValue()));

                                  // Parse the JSON string back to a map
                                  Map<String, String> linkAndRole =
                                      new ObjectMapper()
                                          .readValue(
                                              decodedLinkAndRole,
                                              new TypeReference<Map<String, String>>() {});

                                  String link = linkAndRole.get("link");
                                  Role role = Role.valueOf(linkAndRole.get("role"));

                                  // remove cookie
                                  cookie.setMaxAge(0);
                                  response.addCookie(cookie);

                                  if (assignRoleToTheProject(link, role, authentication)) {
                                    response.sendRedirect(
                                        frontendUrl
                                            + "/projects?status=addedToProjectSuccessfully");
                                    return;
                                  } else {
                                    response.sendRedirect(
                                        frontendUrl + "/projects?status=invalidLink");
                                    return;
                                  }
                                }
                              }
                            }

                            String referer = request.getHeader("Referer");
                            if (referer != null) {
                              response.sendRedirect(referer);
                            } else {
                              response.sendRedirect(frontendUrl);
                            }
                          }))
          .logout(
              logout -> {
                logout.invalidateHttpSession(true);
                logout.deleteCookies("JSESSIONID");
              })
          .securityContext(
              security -> {
                security.securityContextRepository(new HttpSessionSecurityContextRepository());
              })
          .build();
    } else {
      return http.csrf(
              csrf ->
                  csrf.ignoringRequestMatchers(
                      "/h2-console/**", "/login", "/register", "/api/**", "/ai/**"))
          .authorizeHttpRequests(
              auth -> {
                auth.requestMatchers("/profile", "/api/createProject").authenticated();
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
                      .failureHandler(authenticationFailureHandler())
                      .successHandler(
                          (request, response, authentication) -> {
                            Cookie[] cookies = request.getCookies();
                            if (cookies != null) {
                              for (Cookie cookie : cookies) {
                                if (cookie.getName().equals("linkAndRole")) {
                                  // Decode the cookie value
                                  String decodedLinkAndRole =
                                      new String(Base64.getDecoder().decode(cookie.getValue()));

                                  // Parse the JSON string back to a map
                                  Map<String, String> linkAndRole =
                                      new ObjectMapper()
                                          .readValue(
                                              decodedLinkAndRole,
                                              new TypeReference<Map<String, String>>() {});

                                  String link = linkAndRole.get("link");
                                  Role role = Role.valueOf(linkAndRole.get("role"));

                                  // remove cookie
                                  cookie.setMaxAge(0);
                                  response.addCookie(cookie);

                                  if (assignRoleToTheProject(link, role, authentication)) {
                                    response.sendRedirect(
                                        frontendUrl
                                            + "/projects?status=addedToProjectSuccessfully");
                                    return;
                                  } else {
                                    response.sendRedirect(
                                        frontendUrl + "/projects?status=invalidLink");
                                    return;
                                  }
                                }
                              }
                            }

                            String referer = request.getHeader("Referer");
                            if (referer != null) {
                              response.sendRedirect(referer);
                            } else {
                              response.sendRedirect(frontendUrl);
                            }
                          }))
          .logout(
              logout -> {
                logout.invalidateHttpSession(true);
                logout.deleteCookies("JSESSIONID");
              })
          .securityContext(
              security -> {
                security.securityContextRepository(new HttpSessionSecurityContextRepository());
              })
          .build();
    }
  }

  private boolean assignRoleToTheProject(String link, Role role, Authentication authentication) {

    String email;
    User user;
    Project project;
    try {
      email = getEmail(authentication);

      user =
          userRepository
              .findUserByEmail(email)
              .orElseThrow(() -> new IllegalArgumentException("User not found"));

      if (role != Role.EMAIL) {
        project =
            (role == Role.EDITOR
                    ? projectRepository.findProjectByEditorLink(link)
                    : projectRepository.findProjectByViewerLink(link))
                .orElseThrow(() -> new IllegalArgumentException("Invalid link"));

        Optional<UserProjectRole> userProjectRole =
            userProjectRoleRepository.findUserProjectRoleByUserAndProject(user, project);

        if (userProjectRole.isPresent()) {
          Role userRole = Role.valueOf(userProjectRole.get().getRole().toUpperCase());

          if (userRole.getValue() >= role.getValue()) {
            throw new IllegalArgumentException("User already has higher or same authority");
          }
        }
        if (role == Role.VIEWER) {
          projectService.addViewerToProjectByLink(email, link);
        } else {
          projectService.addEditorToProjectByLink(email, link);
        }
        return true;
      }

      ProjectInvitedMembers projectInvitedMembers;

      try {
        projectInvitedMembers =
            projectInvitedMembersRepository
                .findByInviteLink(link)
                .orElseThrow(() -> new IllegalArgumentException("Invalid link"));
        project = projectInvitedMembers.getProject();

      } catch (IllegalArgumentException e) {
        return false;
      }

      Optional<UserProjectRole> userProjectRole =
          userProjectRoleRepository.findUserProjectRoleByUserAndProject(user, project);

      addUserToProjectFromEmailInvite(userProjectRole, user, projectInvitedMembers);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  public void addUserToProjectFromEmailInvite(
      Optional<UserProjectRole> userProjectRole,
      User user,
      ProjectInvitedMembers projectInvitedMembers) {

    if (userProjectRole.isPresent()) {
      Role userRole = Role.valueOf(userProjectRole.get().getRole().toUpperCase());

      if (userRole.getValue() >= projectInvitedMembers.getType()) {
        throw new IllegalArgumentException("User already has higher or same authority");
      }

      UserProjectRole newUserProjectRole = userProjectRole.get();
      newUserProjectRole.setRole(Role.getRoleByValue(projectInvitedMembers.getType()).name());
      userProjectRoleRepository.save(newUserProjectRole);
      projectInvitedMembersRepository.delete(projectInvitedMembers);

      // Check if the invited member is not invited to any other project
      // if so delete it
      InvitedMembers invitedMembers = projectInvitedMembers.getInvitedMembers();
      List<ProjectInvitedMembers> projectInvitedMembersList =
          projectInvitedMembersRepository.findAllByInvitedMembers(invitedMembers);
      if (projectInvitedMembersList.isEmpty()) {
        invitedMembersRepository.delete(invitedMembers);
      }
      return;
    }

    UserProjectRole newUserProjectRole =
        new UserProjectRole(
            user,
            projectInvitedMembers.getProject(),
            Role.getRoleByValue(projectInvitedMembers.getType()).name());
    userProjectRoleRepository.save(newUserProjectRole);
    projectInvitedMembersRepository.delete(projectInvitedMembers);

    // Check if the invited member is not invited to any other project
    // if so delete it
    InvitedMembers invitedMembers = projectInvitedMembers.getInvitedMembers();
    List<ProjectInvitedMembers> projectInvitedMembersList =
        projectInvitedMembersRepository.findAllByInvitedMembers(invitedMembers);
    if (projectInvitedMembersList.isEmpty()) {
      invitedMembersRepository.delete(invitedMembers);
    }
  }

  @Bean
  public AuthenticationFailureHandler authenticationFailureHandler() {
    return new CustomAuthenticationFailureHandler(frontendUrl);
  }

  private boolean isOAuthEnabled() {
    return (env.containsProperty("github.client-id")
            && env.containsProperty("github.client-secret"))
        || (env.containsProperty("gitlab.client-id")
            && env.containsProperty("gitlab.client-secret"));
  }
}

class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
  String frontendUrl;

  public CustomAuthenticationFailureHandler(String frontendUrl) {
    this.frontendUrl = frontendUrl;
  }

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException, ServletException {
    String exceptionType = exception.getClass().getSimpleName();

    String targetUrl =
        frontendUrl
            + "/login?exception="
            + URLEncoder.encode(exceptionType, StandardCharsets.UTF_8.toString());

    response.sendRedirect(targetUrl);
  }
}
