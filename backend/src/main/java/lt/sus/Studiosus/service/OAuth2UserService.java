package lt.sus.Studiosus.service;

import java.util.Map;
import java.util.Optional;
import lt.sus.Studiosus.model.User;
import lt.sus.Studiosus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

  @Autowired private UserRepository userRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) {
    OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
    return processOAuth2User(oAuth2UserRequest, oAuth2User);
  }

  private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
    Map<String, Object> attributes = oAuth2User.getAttributes();
    String name = attributes.getOrDefault("name", "").toString();
    String id = attributes.getOrDefault("id", "").toString();
    String email = attributes.getOrDefault("email", "").toString();
    String avatar = attributes.getOrDefault("avatar_url", "").toString();

    User user =
        User.builder().username(name).email(email).avatar(avatar).roles("ROLE_USER").build();

    String oauthProvider = oAuth2UserRequest.getClientRegistration().getRegistrationId();
    Optional<User> providerUser = Optional.empty();

    if ("github".equals(oauthProvider)) {
      providerUser = userRepository.findUserByGithubId(id);
    } else if ("gitlab".equals(oauthProvider)) {
      providerUser = userRepository.findUserByGitlabId(id);
    }

    if (providerUser.isPresent()) {
      return oAuth2User;
    }

    Optional<User> emailUser = userRepository.findUserByEmail(email);
    emailUser
        .map(
            existingUser -> {
              if ("github".equals(oauthProvider)) {
                existingUser.setGithubId(id);
              } else if ("gitlab".equals(oauthProvider)) {
                existingUser.setGitlabId(id);
              }
              return userRepository.save(existingUser);
            })
        .orElseGet(
            () -> {
              if ("github".equals(oauthProvider)) {
                user.setGithubId(id);
              } else if ("gitlab".equals(oauthProvider)) {
                user.setGitlabId(id);
              }
              return userRepository.save(user);
            });

    return oAuth2User;
  }
}
