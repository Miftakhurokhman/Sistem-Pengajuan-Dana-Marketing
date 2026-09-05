package com.acc.backend.security;

import com.acc.backend.domain.entity.MasterUser;
import com.acc.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository masterUserRepository;

    @Override
    @Transactional(readOnly = true) // Tahan DB Session tetap aktif
    public UserDetails loadUserByUsername(String npk) throws UsernameNotFoundException {
        MasterUser user = masterUserRepository.findByNpk(npk)
                .orElseThrow(() -> new UsernameNotFoundException("User tidak ditemukan: " + npk));

        return new CustomUserDetails(user); // Di sini line 30 CustomUserDetails dipanggil
    }
}