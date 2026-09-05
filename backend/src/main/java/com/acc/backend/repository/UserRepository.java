package com.acc.backend.repository;

import com.acc.backend.domain.entity.MasterUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<MasterUser, Long> {
    Optional<MasterUser> findByNpkAndIsDeletedFalseAndIsActiveTrue(String npk);

    @EntityGraph(attributePaths = {"role", "branch", "branch.area"})
    Optional<MasterUser> findByNpk(String npk);
}