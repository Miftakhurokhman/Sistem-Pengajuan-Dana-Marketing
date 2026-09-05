package com.acc.backend.security;

import com.acc.backend.domain.entity.MasterUser;
import lombok.Getter; // 1. Import Getter
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final MasterUser user;

    public CustomUserDetails(MasterUser user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user.getRole() != null) {
            String roleCode = user.getRole().getRoleCode();
            String authority = roleCode.startsWith("ROLE_") ? roleCode : "ROLE_" + roleCode;
            return List.of(new SimpleGrantedAuthority(authority));
        }
        return List.of();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getNpk();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(user.getIsActive());
    }
}