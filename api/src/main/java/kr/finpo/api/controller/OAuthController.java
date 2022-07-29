package kr.finpo.api.controller;

import kr.finpo.api.dto.DataResponse;
import kr.finpo.api.dto.TokenDto;
import kr.finpo.api.dto.UserDto;
import kr.finpo.api.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/oauth")
public class OAuthController {

    private final OAuthService oAuthService;

    @GetMapping("/login/{oAuthType}")
    public Object loginWithOAuthToken(@RequestHeader("Authorization") String kakaoAccessToken,
        @PathVariable String oAuthType) {
        Object loginRes = oAuthService.loginWithOAuthToken(kakaoAccessToken, oAuthType);
        if (loginRes.getClass().equals(UserDto.class)) { // not registered
            return new ResponseEntity<>(DataResponse.of(loginRes, "need register"), HttpStatus.ACCEPTED);
        }
        return DataResponse.of(loginRes);
    }

    @PostMapping("/register/{oAuth}")
    public DataResponse<Object> registerWithOAuthToken(
        @RequestHeader("Authorization") String oAuthAccessToken,
        @ModelAttribute UserDto body,
        @PathVariable String oAuth
    ) {
        return DataResponse.of(oAuthService.register(oAuthAccessToken, oAuth, body));
    }

    @PostMapping("/reissue")
    public DataResponse<Object> reissueTokens(@RequestBody TokenDto tokenDto) {
        return DataResponse.of(oAuthService.reissueTokens(tokenDto));
    }
}

