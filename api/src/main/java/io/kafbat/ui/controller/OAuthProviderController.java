package io.kafbat.ui.controller;


import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OAuthProviderController {

  //  private final InMemoryClientRegistrationRepository clientRegistrationRepository;
//  private final InMemoryReactiveClientRegistrationRepository clientRegistrationRepository;
  private final ApplicationContext applicationContext;


  @GetMapping("/auth2")
  public AuthenticationInfo bruh() {
    return new AuthenticationInfo(getAuthType(), getOAuthProviders());
  }

  private String getAuthType() {
    applicationContext.getEnvironment().getProperty("auth.type")
    "OAUTH2", "LOGIN_FORM", "DISABLED", "LDAP",
  }

  @SuppressWarnings("unchecked")
  private Collection<OAuthProvider> getOAuthProviders() {
    if (!getAuthType().equalsIgnoreCase("OAUTH2")) {
      return Collections.emptySet();
    }
    var type = ResolvableType.forClassWithGenerics(Iterable.class, ClientRegistration.class);
    String[] names = this.applicationContext.getBeanNamesForType(type);
    var bean = (Iterable<ClientRegistration>) (names.length == 1 ? this.applicationContext.getBean(names[0]) : null);

    if (bean == null) {
      return Collections.emptySet();
    }

    return Streams.stream(bean.iterator())
        .filter(r -> AuthorizationGrantType.AUTHORIZATION_CODE.equals(r.getAuthorizationGrantType()))
        .map(r -> new OAuthProvider(r.getClientName(), "/oauth2/authorization/" + r.getRegistrationId()))
        .collect(Collectors.toSet());
  }

  @Value
  public static class AuthenticationInfo {
    String authType;
    Collection<OAuthProvider> oAuthProviders;
  }

  @Value
  public static class OAuthProvider {

    String clientName;
    String authorizationUri;

    public OAuthProvider(String clientName, String authorizationUri) {
      this.clientName = clientName;
      this.authorizationUri = authorizationUri;
    }
  }
}
