package com.acc.backend.domain.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReqRejectPengajuanDana {

    @NotBlank(message = "Catatan penolakan wajib diisi")
    @Size(max = 255, message = "Catatan penolakan maksimal 255 karakter")
    private String catatan;
}