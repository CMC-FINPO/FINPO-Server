package kr.finpo.api.dto;

public record GoogleTokenDto(
    String access_token,
    String refresh_token,
    String expires_in,
    String refresh_token_expires_in,
    String scope,
    String token_type,
    String id_token
) {
}
