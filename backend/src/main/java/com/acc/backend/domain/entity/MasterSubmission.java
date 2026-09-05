package com.acc.backend.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "trn_submission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MasterSubmission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "submission_no", nullable = false, unique = true, length = 50)
    private String submissionNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private MasterUser applicant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private MasterBranch branch;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal nominal;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "proposal_url")
    private String proposalUrl;

    @Column(nullable = false, length = 20)
    private String status; // PENDING, APPROVED, REJECTED

    @Column(name = "current_approver_role", length = 50)
    private String currentApproverRole;
}