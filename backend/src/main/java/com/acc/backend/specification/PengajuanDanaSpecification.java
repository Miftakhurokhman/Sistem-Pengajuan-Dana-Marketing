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
            String search,
            String status) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if ("ROLE_SALES".equalsIgnoreCase(roleCode)) {
                if (userId != null) predicates.add(cb.equal(root.get("requester").get("id"), userId));
            } else if ("ROLE_BM".equalsIgnoreCase(roleCode)) {
                if (branchId != null) predicates.add(cb.equal(root.get("branch").get("id"), branchId));
            } else if ("ROLE_RRSH".equalsIgnoreCase(roleCode) || "ROLE_BRM".equalsIgnoreCase(roleCode)) {
                if (areaId != null) predicates.add(cb.equal(root.get("branch").get("area").get("id"), areaId));
            }

            if (StringUtils.hasText(search)) {
                String pattern = "%" + search.toLowerCase() + "%";
                Predicate searchNomor = cb.like(cb.lower(root.get("nomorPengajuan")), pattern);
                Predicate searchJudul = cb.like(cb.lower(root.get("judulKegiatan")), pattern);
                predicates.add(cb.or(searchNomor, searchJudul));
            }

            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            predicates.add(cb.equal(root.get("isDeleted"), false));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}