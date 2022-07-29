package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AppleTokenDto(
    String access_token
) {

    public AppleTokenDto {
    }
}
