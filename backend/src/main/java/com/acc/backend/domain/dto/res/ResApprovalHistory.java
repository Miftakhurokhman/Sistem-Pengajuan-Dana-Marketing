package com.acc.backend.domain.dto.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResApprovalHistory {
    private Long id;
    private String approverName;
    private String approverRole;
    private String action;
    private String notes;

    @JsonFormat(pattern = "dd MMMM yyyy HH:mm", timezone = "Asia/Jakarta", locale = "id")
    private LocalDateTime actionAt;
}