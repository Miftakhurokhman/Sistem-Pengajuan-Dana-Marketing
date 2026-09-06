package com.acc.backend.repository;

import com.acc.backend.domain.entity.MasterBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasterBrandRepository extends JpaRepository<MasterBrand, Long> {

    /**
     * Mencari brand berdasarkan ID yang statusnya aktif (isActive = true)
     * dan belum dihapus (isDeleted = false).
     */
    Optional<MasterBrand> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    /**
     * Mengecek keberadaan brand berdasarkan ID yang aktif dan belum dihapus.
     */
    boolean existsByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    /**
     * Mengambil daftar semua brand yang aktif dan belum dihapus (misal untuk dropdown pilihan brand).
     */
    List<MasterBrand> findAllByIsActiveTrueAndIsDeletedFalse();
}