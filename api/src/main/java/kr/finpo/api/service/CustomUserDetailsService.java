package kr.finpo.api.service;

import java.util.Collections;
import kr.finpo.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;


    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findById(Long.parseLong(username))
            .map(this::createUserDetails).get();
    }


    private UserDetails createUserDetails(kr.finpo.api.domain.User user) {
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(user.getRole().toString());

        return new User(
            user.getId().toString(),
            null,
            Collections.singleton(grantedAuthority)
        );
    }
}
