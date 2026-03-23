package br.ufc.huwc.gat3.controller;

import br.ufc.huwc.gat3.dto.DischargeDTO;
import br.ufc.huwc.gat3.dto.DashboardStatsDto;
import br.ufc.huwc.gat3.dto.DailyStatsDto;
import br.ufc.huwc.gat3.dto.AppointmentDTO;
import br.ufc.huwc.gat3.dto.DischargeComplementDTO;
import br.ufc.huwc.gat3.model.Discharge;
import br.ufc.huwc.gat3.service.DischargeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/v1/discharges")
@RequiredArgsConstructor
public class DischargeController {

    private final DischargeService dischargeService;
    private final br.ufc.huwc.gat3.service.AgHuApiService agHuApiService;

    @PostMapping("/aghu")
    @ResponseStatus(HttpStatus.OK)
    public Discharge generateFromAgHuPayload(@RequestBody List<DischargeDTO> aghuPayload) {
        if (CollectionUtils.isEmpty(aghuPayload)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Payload de alta qualificada nao pode ser vazio.");
        }
        return dischargeService.generateReportFromAgHu(aghuPayload);
    }

    @GetMapping
    public List<Discharge> getAllReports() {
        return dischargeService.findAll();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('gestor', 'admin')")
    public DashboardStatsDto getDashboardStats(@RequestParam(required = false) List<String> setores) {
        return dischargeService.getDashboardStats(setores);
    }

    @GetMapping("/daily")
    @PreAuthorize("hasAnyRole('gestor', 'admin')")
    public DailyStatsDto getDailyStats(
            @RequestParam int mes,
            @RequestParam int ano,
            @RequestParam(required = false) List<String> setores) {
        return dischargeService.getDailyStats(mes, ano, setores);
    }

    @GetMapping("/summary")
    public Page<AppointmentDTO> getSummaries(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate dataInicio,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate dataFim,
            @RequestParam(required = false) List<String> setores,
            @PageableDefault(size = 10, sort = "alteradoEm", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return dischargeService.findAllSummaries(status, busca, dataInicio, dataFim, setores, pageable);
    }

    @GetMapping("/{id}")
    public Discharge getReportById(@PathVariable Long id) {
        return dischargeService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Alta não encontrada"));
    }

    @GetMapping("/by-record")
    public ResponseEntity<Discharge> getByRecord(
            @RequestParam Long seqAtendimento,
            @RequestParam String prontuario) {
        return dischargeService.findByCompositeKey(seqAtendimento, prontuario)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/aghu/{seqAtendimento}")
    public ResponseEntity<DischargeDTO> getFullDischargeFromAgHu(
            @PathVariable Long seqAtendimento,
            @RequestParam String filial) {
        DischargeDTO discharge = agHuApiService.fetchDischargeData(seqAtendimento, filial);

        if (discharge == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(discharge);
    }

    @PutMapping("/by-record")
    public ResponseEntity<Discharge> upsert(
            @RequestParam Long seqAtendimento,
            @RequestParam String prontuario,
            @RequestBody DischargeComplementDTO dto) {
        return ResponseEntity.ok(dischargeService.upsertByCompositeKey(seqAtendimento, prontuario, dto));
    }
}
