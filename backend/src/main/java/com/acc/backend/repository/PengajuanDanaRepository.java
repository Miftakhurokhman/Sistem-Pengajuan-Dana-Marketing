package com.acc.backend.repository;

import com.acc.backend.domain.entity.MasterUser;
import com.acc.backend.domain.entity.PengajuanDana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PengajuanDanaRepository extends JpaRepository<PengajuanDana, Long>, JpaSpecificationExecutor<PengajuanDana> {
    Optional<PengajuanDana> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);
}