package br.ufc.huwc.gat3.service;

import br.ufc.huwc.gat3.dto.DischargeDTO;
import br.ufc.huwc.gat3.dto.AppointmentDTO;
import br.ufc.huwc.gat3.dto.DashboardStatsDto;
import br.ufc.huwc.gat3.dto.DailyStatsDto;
import br.ufc.huwc.gat3.dto.DischargeComplementDTO;
import br.ufc.huwc.gat3.model.Discharge;
import br.ufc.huwc.gat3.repositories.DischargeRepository;
import br.ufc.huwc.gat3.repositories.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Slf4j
@org.springframework.transaction.annotation.Transactional
public class DischargeService {

    private final DischargeMapper mapper;
    private final DischargeRepository repository;
    private final AppointmentRepository appointmentRepository;
    private final br.ufc.huwc.gat3.service.AgHuApiService agHuApiService;

    public Discharge generateReportFromAgHu(List<DischargeDTO> aghuPayload) {
        if (CollectionUtils.isEmpty(aghuPayload)) {
            throw new IllegalArgumentException("aghuPayload cannot be null or empty");
        }
        return mapper.toReport(aghuPayload.get(0));
    }

    public List<Discharge> findAll() {
        return repository.findAll();
    }

    public Page<AppointmentDTO> findAllSummaries(String status, String busca, java.time.LocalDate dataInicio,
            java.time.LocalDate dataFim, List<String> setores, Pageable pageable) {
        List<String> validSetores = (setores == null || setores.isEmpty()) ? null
                : setores.stream().map(String::toLowerCase).collect(Collectors.toList());
        boolean hasSetores = validSetores != null;

        String processedBusca = (busca == null || busca.trim().isEmpty()) ? null : "%" + busca.toLowerCase() + "%";

        return appointmentRepository
                .findFilteredAppointments(status, processedBusca, dataInicio, dataFim, validSetores, hasSetores,
                        pageable)
                .map(mapper::appointmentToSummaryDTO);
    }

    public Optional<Discharge> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Discharge> findByCompositeKey(Long seqAtendimento, String prontuario) {
        return repository.findByAghuDischargeIdAndPatientInfo_AghuRecord(seqAtendimento, prontuario);
    }

    public DashboardStatsDto getDashboardStats(List<String> setores) {
        List<String> validSetores = (setores == null || setores.isEmpty()) ? null
                : setores.stream().map(String::toLowerCase).collect(Collectors.toList());

        boolean hasSetores = validSetores != null;
        AppointmentRepository.DashboardStatsProjection result = appointmentRepository.getDashboardStats(validSetores,
                hasSetores);

        return DashboardStatsDto.builder()
                .total(result != null && result.getTotal() != null ? result.getTotal() : 0L)
                .pendentes(result != null && result.getPendentes() != null ? result.getPendentes() : 0L)
                .salvas(result != null && result.getSalvas() != null ? result.getSalvas() : 0L)
                .enviadas(result != null && result.getEnviadas() != null ? result.getEnviadas() : 0L)
                .build();
    }

    public DailyStatsDto getDailyStats(int mes, int ano, List<String> setores) {
        List<String> validSetores = (setores == null || setores.isEmpty()) ? null
                : setores.stream().map(String::toLowerCase).collect(Collectors.toList());

        boolean hasSetores = validSetores != null;
        List<AppointmentRepository.DailyStatsProjection> result = appointmentRepository.getDailyStats(mes, ano,
                validSetores,
                hasSetores);

        Map<Integer, Long> dados = new HashMap<>();
        for (AppointmentRepository.DailyStatsProjection row : result) {
            if (row.getDia() != null && row.getQuantidade() != null) {
                dados.put(row.getDia(), row.getQuantidade());
            }
        }

        return DailyStatsDto.builder()
                .mes(mes)
                .ano(ano)
                .dados(dados)
                .build();
    }

    public Discharge upsertByCompositeKey(Long seqAtendimento, String prontuario, DischargeComplementDTO dto) {
        log.info("Iniciando upsert para seqAtendimento={}, prontuario={}", seqAtendimento, prontuario);
        Discharge savedDischarge = repository.findByAghuDischargeIdAndPatientInfo_AghuRecord(seqAtendimento, prontuario)
                .map(existing -> {
                    log.debug("Alta encontrada no banco local. Aplicando updates.");
                    return applyUpdates(existing, dto);
                })
                .orElseGet(() -> {
                    log.info("Alta não encontrada localmente. Buscando no AGHU...");
                    br.ufc.huwc.gat3.dto.DischargeDTO aghuData = agHuApiService.fetchDischargeData(seqAtendimento,
                            "meac");
                    if (aghuData == null) {
                        log.error("Alta não encontrada no AGHU para o atendimento: {}", seqAtendimento);
                        throw new IllegalArgumentException(
                                "Alta não encontrada no AGHU para o atendimento: " + seqAtendimento);
                    }
                    log.debug("Dados do AGHU recuperados para {}", aghuData.getPaciente().getNome());
                    Discharge newReport = generateReportFromAgHu(java.util.Collections.singletonList(aghuData));
                    return applyUpdates(newReport, dto);
                });

        appointmentRepository.findBySeqAtendimento(seqAtendimento).ifPresent(app -> {
            if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
                log.info("Sincronizando status '{}' e pendência com Appointment seq={}", dto.getStatus(),
                        seqAtendimento);
                app.setStatus(dto.getStatus());
                app.setPendenciaPreenchimento(calculatePendencia(savedDischarge));
                appointmentRepository.save(app);
            }
        });

        return savedDischarge;
    }

    private boolean calculatePendencia(Discharge report) {
        boolean hasDischargeReason = report.getAmbulatoryProfile() != null
                && StringUtils.hasText(report.getAmbulatoryProfile().getDischargeReason());
        boolean hasTherapeuticGuidance = StringUtils.hasText(report.getTherapeuticGuidance());
        boolean hasFollowUp = StringUtils.hasText(report.getFollowUpSchedulingSuggestion());

        return !hasDischargeReason || !hasTherapeuticGuidance || !hasFollowUp;
    }

    private Discharge applyUpdates(Discharge report, DischargeComplementDTO dto) {
        if (dto.getAmbulatoryProfile() != null) {
            if (report.getAmbulatoryProfile() == null) {
                report.setAmbulatoryProfile(new Discharge.AmbulatoryProfile());
            }
            if (dto.getAmbulatoryProfile().getAdequate() != null) {
                report.getAmbulatoryProfile().setAdequate(dto.getAmbulatoryProfile().getAdequate());
            }
            if (dto.getAmbulatoryProfile().getDischargeReason() != null) {
                report.getAmbulatoryProfile().setDischargeReason(dto.getAmbulatoryProfile().getDischargeReason());
            }
        }

        if (dto.getTherapeuticGuidance() != null) {
            report.setTherapeuticGuidance(dto.getTherapeuticGuidance());
        }

        if (dto.getFollowUpSchedulingSuggestion() != null) {
            report.setFollowUpSchedulingSuggestion(dto.getFollowUpSchedulingSuggestion());
        }

        if (dto.getStatus() != null && !dto.getStatus().isEmpty()
                && !"enviada".equalsIgnoreCase(report.getStatus())) {
            log.debug("Alterando status de '{}' para '{}'", report.getStatus(), dto.getStatus());
            report.setStatus(dto.getStatus());
        }

        Discharge saved = repository.save(report);
        log.info("Discharge salvo com ID: {} e Status: {}", saved.getId(), saved.getStatus());
        return saved;
    }

    public Discharge save(Discharge report) {
        return repository.save(report);
    }
}
