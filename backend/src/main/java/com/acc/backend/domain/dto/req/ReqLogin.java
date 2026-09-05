package com.acc.backend.domain.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReqLogin {

    @NotBlank(message = "NPK wajib diisi")
    private String npk;

    @NotBlank(message = "Password wajib diisi")
    private String password;
}