package com.acc.backend.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "mst_role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MasterRole extends com.berijalan.spd.domain.entity.BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_code", nullable = false, unique = true, length = 50)
    private String roleCode; // Contoh: PIC_SALES, BRM, BM

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName; // Contoh: PIC Sales, Branch Risk Manager, Branch Manager

    @Column(columnDefinition = "TEXT")
    private String description;
}