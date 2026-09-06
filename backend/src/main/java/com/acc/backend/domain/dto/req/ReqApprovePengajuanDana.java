package com.acc.backend.domain.dto.req;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReqApprovePengajuanDana {

    @Size(max = 255, message = "Catatan approval maksimal 255 karakter")
    private String catatan;
}