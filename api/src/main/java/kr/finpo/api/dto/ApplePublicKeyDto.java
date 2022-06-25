package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.constant.OpenApiType;
import kr.finpo.api.domain.Policy;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApplePublicKeyDto(
    ArrayList<Key> keys
) {
  public record Key(
      String kty,
      String kid,
      String use,
      String alg,
      String n,
      String e
  ) {
  }

  public Optional<Key> getMatchedKeyBy(String kid, String alg) {
    return this.keys.stream()
        .filter(key -> key.kid().equals(kid) && key.alg().equals(alg))
        .findFirst();
  }
}
