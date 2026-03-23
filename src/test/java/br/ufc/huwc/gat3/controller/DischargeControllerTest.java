package br.ufc.huwc.gat3.controller;

import br.ufc.huwc.gat3.dto.AppointmentDTO;
import br.ufc.huwc.gat3.dto.DailyStatsDto;
import br.ufc.huwc.gat3.dto.DashboardStatsDto;
import br.ufc.huwc.gat3.dto.DischargeComplementDTO;
import br.ufc.huwc.gat3.dto.DischargeDTO;
import br.ufc.huwc.gat3.model.Discharge;
import br.ufc.huwc.gat3.service.AgHuApiService;
import br.ufc.huwc.gat3.service.DischargeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DischargeController.class)
@ActiveProfiles("dev")
@AutoConfigureMockMvc(addFilters = false)
class DischargeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DischargeService service;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AgHuApiService agHuApiService;

    @Test
    @WithMockUser
    void generateFromAgHuPayloadShouldReturnReport() throws Exception {
        DischargeDTO dto = new DischargeDTO();
        Discharge report = Discharge.builder().id(1L).status("pendente").build();

        when(service.generateReportFromAgHu(any())).thenReturn(report);

        mockMvc.perform(post("/v1/discharges/aghu")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Collections.singletonList(dto))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("pendente"));
    }

    @Test
    @WithMockUser
    void generateFromAgHuPayloadShouldReturnBadRequestForEmptyPayload() throws Exception {
        mockMvc.perform(post("/v1/discharges/aghu")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getAllReportsShouldReturnList() throws Exception {
        Discharge report = Discharge.builder().id(10L).status("salva").build();
        when(service.findAll()).thenReturn(Collections.singletonList(report));

        mockMvc.perform(get("/v1/discharges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].status").value("salva"));
    }

    @Test
    @WithMockUser(roles = "gestor")
    void getDashboardStatsShouldReturnStats() throws Exception {
        DashboardStatsDto dto = DashboardStatsDto.builder()
                .total(10L)
                .pendentes(4L)
                .salvas(3L)
                .enviadas(3L)
                .build();

        when(service.getDashboardStats(any())).thenReturn(dto);

        mockMvc.perform(get("/v1/discharges/stats")
                        .param("setores", "Clinica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(10))
                .andExpect(jsonPath("$.pendentes").value(4));
    }

    @Test
    @WithMockUser(roles = "gestor")
    void getDailyStatsShouldReturnStats() throws Exception {
        DailyStatsDto dto = DailyStatsDto.builder()
                .mes(3)
                .ano(2026)
                .build();

        when(service.getDailyStats(eq(3), eq(2026), any())).thenReturn(dto);

        mockMvc.perform(get("/v1/discharges/daily")
                        .param("mes", "3")
                        .param("ano", "2026")
                        .param("setores", "Clinica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mes").value(3))
                .andExpect(jsonPath("$.ano").value(2026));
    }

    @Test
    @WithMockUser
    void getSummariesShouldReturnList() throws Exception {
        AppointmentDTO dto = AppointmentDTO.builder().seqAtendimento(123L).build();

        Page<AppointmentDTO> pageResp = new PageImpl<>(Arrays.asList(dto));

        when(service.findAllSummaries(any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(pageResp);

        mockMvc.perform(get("/v1/discharges/summary")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].seqAtendimento").value(123));
    }

    @Test
    @WithMockUser
    void getReportByIdShouldReturnFound() throws Exception {
        Discharge report = Discharge.builder().id(55L).status("pendente").build();
        when(service.findById(55L)).thenReturn(Optional.of(report));

        mockMvc.perform(get("/v1/discharges/55"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(55));
    }

    @Test
    @WithMockUser
    void getReportByIdShouldReturnNotFound() throws Exception {
        when(service.findById(90L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/discharges/90"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getByRecordShouldReturnFound() throws Exception {
        Discharge report = Discharge.builder().id(70L).build();
        when(service.findByCompositeKey(100L, "200")).thenReturn(Optional.of(report));

        mockMvc.perform(get("/v1/discharges/by-record")
                        .param("seqAtendimento", "100")
                        .param("prontuario", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(70));
    }

    @Test
    @WithMockUser
    void getByRecordShouldReturnNotFound() throws Exception {
        when(service.findByCompositeKey(100L, "200")).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/discharges/by-record")
                        .param("seqAtendimento", "100")
                        .param("prontuario", "200"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnDischargeFromAgHuWhenSuccessful() throws Exception {
        Long seqAtendimento = 1463134L;
        String filial = "meac";
        DischargeDTO mockDto = new DischargeDTO();
        mockDto.setNumero(seqAtendimento);

        when(agHuApiService.fetchDischargeData(seqAtendimento, filial)).thenReturn(mockDto);

        mockMvc.perform(get("/v1/discharges/aghu/{seq}", seqAtendimento)
                        .param("filial", filial)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value(seqAtendimento));
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenAgHuReturnNull() throws Exception {
        Long seqAtendimento = 999L;
        String filial = "meac";

        when(agHuApiService.fetchDischargeData(seqAtendimento, filial)).thenReturn(null);

        mockMvc.perform(get("/v1/discharges/aghu/{seq}", seqAtendimento)
                        .param("filial", filial))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void upsertShouldReturnUpdatedDischarge() throws Exception {
        DischargeComplementDTO dto = DischargeComplementDTO.builder()
                .status("salva")
                .build();
        Discharge response = Discharge.builder().id(88L).status("salva").build();

        when(service.upsertByCompositeKey(eq(100L), eq("200"), any())).thenReturn(response);

        mockMvc.perform(put("/v1/discharges/by-record")
                        .with(csrf())
                        .param("seqAtendimento", "100")
                        .param("prontuario", "200")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(88))
                .andExpect(jsonPath("$.status").value("salva"));
    }
}
