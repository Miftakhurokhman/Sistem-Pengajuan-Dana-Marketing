package com.acc.backend.specification;

import com.acc.backend.domain.entity.PengajuanDana;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class PengajuanDanaSpecification {

    public static Specification<PengajuanDana> filter(
            String roleCode,
            Long userId,
            Long branchId,
            Long areaId,
            Long brandId,
            Boolean isNeedApproval,
            String searchBy,
            String searchValue) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String role = roleCode != null ? roleCode.toUpperCase() : "";

            // ==========================================
            // 1. ROLE SCOPING & APPROVAL STATUS FILTER
            // ==========================================

            // A. PIC SALES
            if (role.contains("PIC SALES") || role.contains("SALES")) {
                if (userId != null) {
                    predicates.add(cb.equal(root.get("requester").get("id"), userId));
                }
            }
            // B. BRANCH MANAGER (BM)
            else if (role.contains("BM") && !role.contains("BRM")) {
                if (branchId != null) {
                    predicates.add(cb.equal(root.get("branch").get("id"), branchId));
                }
                if (Boolean.TRUE.equals(isNeedApproval)) {
                    predicates.add(cb.equal(root.get("status"), "Menunggu Approval BM"));
                }
            }
            // C. REGIONAL SALES HEAD (RRSH)
            else if (role.contains("RRSH")) {
                if (areaId != null) {
                    predicates.add(cb.equal(root.get("branch").get("area").get("id"), areaId));
                }
                if (Boolean.TRUE.equals(isNeedApproval)) {
                    predicates.add(cb.equal(root.get("status"), "Menunggu Approval RRSH"));
                }
            }
            // D. BRAND MANAGER (BRM)
            else if (role.contains("BRM")) {
                if (brandId != null) {
                    predicates.add(cb.equal(root.get("brand").get("id"), brandId));
                }
                if (Boolean.TRUE.equals(isNeedApproval)) {
                    predicates.add(cb.equal(root.get("status"), "Menunggu Approval BRM"));
                }
            }
            // E. REGIONAL SALES DEPT HEAD (RRSDH) - Global Scope
            else if (role.contains("RRSDH")) {
                if (Boolean.TRUE.equals(isNeedApproval)) {
                    predicates.add(cb.equal(root.get("status"), "Menunggu Approval RRSDH"));
                }
            }
            // F. CHIEF MARKETING & SALES OFFICER (CMSO) - Global Scope
            else if (role.contains("CMSO")) {
                if (Boolean.TRUE.equals(isNeedApproval)) {
                    predicates.add(cb.equal(root.get("status"), "Menunggu Approval CMSO"));
                }
            }
            // G. CHIEF OPERATING OFFICER (COO) - Global Scope
            else if (role.contains("COO")) {
                if (Boolean.TRUE.equals(isNeedApproval)) {
                    predicates.add(cb.equal(root.get("status"), "Menunggu Approval COO"));
                }
            }

            else if (role.contains("PIC FINANCE")) {
                if (Boolean.TRUE.equals(isNeedApproval)) {
                    predicates.add(cb.equal(root.get("status"), "Siap Dicairkan"));
                }
            }

            // ==========================================
            // 2. DYNAMIC SEARCH FILTER (searchBy & searchValue)
            // ==========================================
            if (StringUtils.hasText(searchValue)) {
                String pattern = "%" + searchValue.toLowerCase() + "%";

                if (StringUtils.hasText(searchBy)) {
                    switch (searchBy) {
                        case "nomorPengajuan":
                            predicates.add(cb.like(cb.lower(root.get("nomorPengajuan")), pattern));
                            break;
                        case "judulKegiatan":
                            predicates.add(cb.like(cb.lower(root.get("judulKegiatan")), pattern));
                            break;
                        case "status":
                            predicates.add(cb.equal(cb.lower(root.get("status")), searchValue.toLowerCase()));
                            break;
                        case "branchName":
                            predicates.add(cb.like(cb.lower(root.get("branch").get("branchName")), pattern));
                            break;
                        default:
                            break;
                    }
                } else {
                    // Fallback search jika searchBy kosong
                    Predicate searchNomor = cb.like(cb.lower(root.get("nomorPengajuan")), pattern);
                    Predicate searchJudul = cb.like(cb.lower(root.get("judulKegiatan")), pattern);
                    predicates.add(cb.or(searchNomor, searchJudul));
                }
            }

            // ==========================================
            // 3. SOFT DELETE FILTER
            // ==========================================
            predicates.add(cb.equal(root.get("isDeleted"), false));
            predicates.add(cb.equal(root.get("isActive"), true));


            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}