package com.acc.backend.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "mst_branch")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MasterBranch extends com.berijalan.spd.domain.entity.BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_code", nullable = false, unique = true, length = 10)
    private String branchCode;

    @Column(name = "branch_name", nullable = false, length = 100)
    private String branchName;
}