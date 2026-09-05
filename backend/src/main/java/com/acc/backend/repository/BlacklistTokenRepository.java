package com.acc.backend.repository;

import com.acc.backend.domain.entity.LogBlacklistToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlacklistTokenRepository extends JpaRepository<LogBlacklistToken, Long> {
    boolean existsByToken(String token);
}