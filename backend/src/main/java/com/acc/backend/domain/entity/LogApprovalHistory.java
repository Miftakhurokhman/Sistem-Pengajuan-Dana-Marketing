package com.acc.backend.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_approval_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class LogApprovalHistory extends BaseLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private MasterSubmission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private MasterUser approver;

    @Column(name = "approver_role", nullable = false, length = 50)
    private String approverRole;

    @Column(nullable = false, length = 20)
    private String status; // SUBMITTED, APPROVED, REJECTED, PENDING

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "action_date")
    private LocalDateTime actionDate;
}