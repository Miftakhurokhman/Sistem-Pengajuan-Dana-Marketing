package com.acc.backend.scheduler;

import com.acc.backend.domain.entity.LogApprovalHistory;
import com.acc.backend.domain.entity.PengajuanDana;
import com.acc.backend.repository.LogApprovalHistoryRepository;
import com.acc.backend.repository.PengajuanDanaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PengajuanDanaScheduler {

    private static final List<String> PENDING_APPROVAL_STATUSES = List.of(
            "Menunggu Approval BM",
            "Menunggu Approval RRSH",
            "Menunggu Approval BRM",
            "Menunggu Approval RRSDH",
            "Menunggu Approval CMSO",
            "Menunggu Approval COO"
    );

    private final PengajuanDanaRepository pengajuanDanaRepository;
    private final LogApprovalHistoryRepository logApprovalHistoryRepository;

    @Scheduled(cron = "0 0 */12 * * *")
    @Transactional
    public void expirePendingPengajuanDana() {
        List<PengajuanDana> expiredList = pengajuanDanaRepository
                .findByStatusInAndTanggalKegiatanLessThanEqualAndIsActiveTrueAndIsDeletedFalse(
                        PENDING_APPROVAL_STATUSES,
                        LocalDate.now()
                );

        if (expiredList.isEmpty()) {
            return;
        }

        for (PengajuanDana pengajuan : expiredList) {
            pengajuan.setStatus("Kadaluarsa");
            pengajuan.setUpdatedBy("SYSTEM");
            pengajuan.setUpdatedAt(LocalDateTime.now());
            pengajuanDanaRepository.save(pengajuan);

            LogApprovalHistory history = LogApprovalHistory.builder()
                    .pengajuanDana(pengajuan)
                    .approverRole("SYSTEM")
                    .status("Kadaluarsa")
                    .notes("Pengajuan otomatis kadaluarsa karena tanggal kegiatan sudah berjalan atau terlewat.")
                    .actionDate(LocalDateTime.now())
                    .build();

            logApprovalHistoryRepository.save(history);
        }
    }
}
