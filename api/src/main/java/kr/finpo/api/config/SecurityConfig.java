package kr.finpo.api.config;

import kr.finpo.api.jwt.JwtAccessDeniedHandler;
import kr.finpo.api.jwt.JwtAuthenticationEntryPoint;
import kr.finpo.api.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  private final TokenProvider tokenProvider;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Override
  public void configure(WebSecurity web) {
//    web.ignoring().antMatchers("/h2-console/**", "/favicon.ico");
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // CSRF 설정 Disable
    http.csrf().disable()
        // exception handling
        .exceptionHandling()
        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
        .accessDeniedHandler(jwtAccessDeniedHandler)

        // disable session
        .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

        // non-token request url
        .and()
        .authorizeRequests()
        .antMatchers("/oauth/**").permitAll()
        .anyRequest().authenticated()   // 나머지 API 는 전부 인증 필요

        .and()
        .apply(new JwtSecurityConfig(tokenProvider));
  }
}
