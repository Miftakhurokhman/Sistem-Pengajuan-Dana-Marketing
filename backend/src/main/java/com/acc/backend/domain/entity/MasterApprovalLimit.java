package com.acc.backend.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "mst_approval_limit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MasterApprovalLimit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private MasterRole role;

    // Batas maksimum nominal yang bisa di-approve oleh role ini
    @Column(name = "max_nominal", nullable = false, precision = 15, scale = 2)
    private BigDecimal maxNominal;

    // Urutan hirarki approval (1 = BM, 2 = RRSH, 3 = BRM, dst)
    @Column(name = "level_order", nullable = false)
    private Integer levelOrder;
}