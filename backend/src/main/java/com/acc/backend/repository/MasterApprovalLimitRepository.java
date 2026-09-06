package com.acc.backend.repository;

import com.acc.backend.domain.entity.MasterApprovalLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MasterApprovalLimitRepository extends JpaRepository<MasterApprovalLimit, Long> {

    /**
     * Mencari limit approval berdasarkan Role ID yang aktif dan belum dihapus.
     */
    Optional<MasterApprovalLimit> findByRoleIdAndIsActiveTrueAndIsDeletedFalse(Long roleId);
}