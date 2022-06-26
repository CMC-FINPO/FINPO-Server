package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AppleAuthDto(
    String code,
    String id_token,
    String status
) {
  public AppleAuthDto {
  }
}
