package com.acc.backend.domain.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResLogin {
    private Long userId;
    private String npk;
    private String fullName;
    private String roleCode;
    private String roleName;
    private String branchName;
    private String token;
}