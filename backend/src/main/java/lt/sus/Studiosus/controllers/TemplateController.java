package lt.sus.Studiosus.controllers;

import static lt.sus.Studiosus.service.UserDetailsServiceImpl.getEmail;

import java.time.LocalDateTime;
import java.util.List;
import lt.sus.Studiosus.dto.*;
import lt.sus.Studiosus.model.*;
import lt.sus.Studiosus.model.enums.LogLevel;
import lt.sus.Studiosus.model.enums.TemplatePhase;
import lt.sus.Studiosus.repository.TemplateRepository;
import lt.sus.Studiosus.repository.UserRepository;
import lt.sus.Studiosus.repository.UserTemplateRepository;
import lt.sus.Studiosus.service.LoggingService;
import lt.sus.Studiosus.service.TemplateService;
import lt.sus.Studiosus.service.UserDetailsServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(
    allowCredentials = "true",
    originPatterns = "*",
    allowedHeaders = "*",
    methods = {
      RequestMethod.DELETE,
      RequestMethod.GET,
      RequestMethod.POST,
      RequestMethod.PUT,
      RequestMethod.OPTIONS
    })
@RestController
@RequestMapping("/api/")
public class TemplateController {
  private final UserDetailsServiceImpl userDetailsService;
  private final TemplateRepository templateRepository;
  private final UserRepository userRepository;
  private final UserTemplateRepository userTemplateRepository;
  private final TemplateService templateService;
  private final LoggingService loggingService;

  public TemplateController(
      UserDetailsServiceImpl userDetailsService,
      TemplateRepository templateRepository,
      UserRepository userRepository,
      UserTemplateRepository userTemplateRepository,
      TemplateService templateService,
      LoggingService loggingService) {
    this.userDetailsService = userDetailsService;
    this.templateRepository = templateRepository;
    this.userRepository = userRepository;
    this.userTemplateRepository = userTemplateRepository;
    this.templateService = templateService;
    this.loggingService = loggingService;
  }

  @GetMapping("/templates")
  public ResponseEntity<List<TemplateDTO>> getReviewedTemplates(Authentication authentication) {
    try {
      User user = userDetailsService.getUser(getEmail(authentication));
      List<TemplateDTO> templatesNotOwnedByCurrentUser =
          userTemplateRepository.findAllByUserNot(user).stream()
              .filter(
                  userTemplate ->
                      userTemplate.getTemplate().getPhase().equals(TemplatePhase.APPROVED.name()))
              .map(
                  userTemplate ->
                      new TemplateDTO(
                          userTemplate.getTemplate().getId(),
                          userTemplate.getTemplate().getName(),
                          userTemplate.getTemplate().getDescription(),
                          userTemplate.getUser().getUsername()))
              .toList();

      return ResponseEntity.status(HttpStatus.OK).body(templatesNotOwnedByCurrentUser);
    } catch (IllegalArgumentException e) {
      List<UserTemplate> pendingUserTemplates =
          userTemplateRepository.findAllByTemplatePhase(TemplatePhase.APPROVED.name());

      List<TemplateDTO> templateDetails =
          pendingUserTemplates.stream()
              .map(
                  userTemplate ->
                      new TemplateDTO(
                          userTemplate.getTemplate().getId(),
                          userTemplate.getTemplate().getName(),
                          userTemplate.getTemplate().getDescription(),
                          userTemplate.getUser().getUsername()))
              .toList();

      return ResponseEntity.status(HttpStatus.OK).body(templateDetails);
    }
  }

  @GetMapping("/templates/pending")
  public ResponseEntity<List<TemplateDTO>> getPendingTemplates(Authentication authentication) {
    try {
      User user = userDetailsService.getUser(getEmail(authentication));

      if (!user.getRoles().equals("ROLE_ADMIN")) {
        loggingService.saveLogToDatabase(
            new Log(
                "Non admin user: %s ,tried to access pending templates".formatted(user.getEmail()),
                LogLevel.WARN,
                LocalDateTime.now()));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }
    } catch (IllegalArgumentException e) {
      loggingService.saveLogToDatabase(
          new Log(
              "Non authorized user tried to access pending templates",
              LogLevel.WARN,
              LocalDateTime.now()));
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    List<UserTemplate> pendingUserTemplates =
        userTemplateRepository.findAllByTemplatePhase(TemplatePhase.PENDING.name());

    List<TemplateDTO> templateDetails =
        pendingUserTemplates.stream()
            .map(
                userTemplate ->
                    new TemplateDTO(
                        userTemplate.getTemplate().getId(),
                        userTemplate.getTemplate().getName(),
                        userTemplate.getTemplate().getDescription(),
                        userTemplate.getUser().getUsername()))
            .toList();

    return ResponseEntity.status(HttpStatus.OK).body(templateDetails);
  }

  @GetMapping("/templates/my")
  public ResponseEntity<List<TemplateDetailsDTO>> getMyTemplates(Authentication authentication) {
    User user;
    try {
      user = userDetailsService.getUser(getEmail(authentication));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    return ResponseEntity.status(HttpStatus.OK)
        .body(
            userTemplateRepository.findAllByUser(user).stream()
                .map(UserTemplate::getTemplate)
                .map(
                    template ->
                        new TemplateDetailsDTO(
                            template.getId(),
                            template.getName(),
                            template.getDescription(),
                            template.getPhase(),
                            template.getComment()))
                .toList());
  }

  @PostMapping("/template/{templateId}/project/{projectName}")
  public ResponseEntity<Project> createProjectFromTemplate(
      @PathVariable Long templateId,
      @PathVariable String projectName,
      Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String email = getEmail(authentication);
    try {
      Project project = templateService.createProjectFromTemplate(email, projectName, templateId);
      return ResponseEntity.status(HttpStatus.CREATED).body(project);
    } catch (Exception e) {
      loggingService.saveLogToDatabase(
          new Log(
              "Error creating project from template id: %s, for user: %s, error: %s"
                  .formatted(templateId, email, e.getMessage()),
              LogLevel.WARN,
              LocalDateTime.now()));
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @PutMapping("/template/{id}:review")
  public ResponseEntity<String> changeProjectPhase(
      @PathVariable Long id,
      @RequestBody TemplateReview templateReview,
      Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String email = getEmail(authentication);
    User user = userRepository.findUserByEmail(email).orElseThrow();

    // Check if the user is an admin
    if (!user.getRoles().equals("ROLE_ADMIN")) {
      loggingService.saveLogToDatabase(
          new Log(
              "Non admin user %s tried to review template id: %s".formatted(email, id),
              LogLevel.WARN,
              LocalDateTime.now()));
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body("User does not have permission to change the project phase");
    }

    try {
      Template template = templateRepository.findById(id).orElseThrow();
      template.setPhase(templateReview.phase().name());
      if (templateReview.comment() != null) {
        template.setComment(templateReview.comment());
      }
      templateRepository.save(template);
      return ResponseEntity.status(HttpStatus.OK).body("Changed template phase to REVIEWED");
    } catch (Exception e) {
      loggingService.saveLogToDatabase(
          new Log(
              "Error changing templates phase: %s".formatted(e.getMessage()),
              LogLevel.WARN,
              LocalDateTime.now()));
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @DeleteMapping("/template/{id}")
  public ResponseEntity<String> deleteTemplate(
      @PathVariable Long id, Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    try {
      User user = userRepository.findUserByEmail(getEmail(authentication)).orElseThrow();
      Template template = templateRepository.findById(id).orElseThrow();
      UserTemplate userTemplate =
          userTemplateRepository.findUserTemplateByTemplate(template).orElseThrow();

      if (!userTemplate.getUser().equals(user)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body("User does not have permission to delete the template");
      }
      userTemplateRepository.delete(userTemplate);
      templateRepository.delete(template);

      return ResponseEntity.status(HttpStatus.OK).body("Deleted template successfully");
    } catch (Exception e) {
      loggingService.saveLogToDatabase(
          new Log(
              "Error deleting template id: %s, error: %s".formatted(id, e.getMessage()),
              LogLevel.WARN,
              LocalDateTime.now()));
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }
  }

  @GetMapping("/template/{id}/folders")
  public ResponseEntity<FolderDTO> getTemplateFolders(@PathVariable Long id) {
    return ResponseEntity.status(HttpStatus.OK).body(templateService.getTemplateFolders(id));
  }

  @GetMapping("/template/{id}/file/{fileId}")
  public ResponseEntity<String> getTemplatesFileContent(
      @PathVariable Long id, @PathVariable Long fileId) {
    try {
      File file = templateService.getFileForUser(id, fileId);
      return ResponseEntity.status(HttpStatus.OK).body(file.getContent());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }
  }

  @PostMapping("/templates/search")
  public ResponseEntity<List<TemplateDTO>> searchTemplates(
      @RequestBody TemplateSearchRequest templateSearchRequest) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(templateService.searchTemplates(templateSearchRequest));
  }
}
