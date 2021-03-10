package org.folio.des.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.des.client.AuthClient;
import org.folio.des.domain.dto.AuthCredentials;
import org.folio.spring.integration.XOkapiHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class AuthService {

  private final AuthClient authClient;

  @Value("${folio.username}")
  private String username;
  @Value("${folio.password}")
  private String password;

  public List<String> login(String tenant, String url) {
    AuthCredentials credentials = new AuthCredentials();
    credentials.setUsername(username);
    credentials.setPassword(password);

    ResponseEntity<String> authResponse = authClient.getApiKey(tenant, url, credentials);
    log.info("Logged in as {}.", username);

    return authResponse.getHeaders().get(XOkapiHeaders.TOKEN);
  }

}
