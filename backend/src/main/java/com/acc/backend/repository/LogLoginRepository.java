package com.acc.backend.repository;

import com.acc.backend.domain.entity.LogLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogLoginRepository extends JpaRepository<LogLogin, Long> {
}