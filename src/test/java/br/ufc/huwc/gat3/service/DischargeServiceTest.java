package br.ufc.huwc.gat3.service;

import br.ufc.huwc.gat3.dto.AppointmentDTO;
import br.ufc.huwc.gat3.dto.DailyStatsDto;
import br.ufc.huwc.gat3.dto.DashboardStatsDto;
import br.ufc.huwc.gat3.dto.DischargeComplementDTO;
import br.ufc.huwc.gat3.dto.DischargeDTO;
import br.ufc.huwc.gat3.model.Appointment;
import br.ufc.huwc.gat3.model.Discharge;
import br.ufc.huwc.gat3.repositories.AppointmentRepository;
import br.ufc.huwc.gat3.repositories.DischargeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DischargeServiceTest {

    @Mock
    private DischargeMapper mapper;

    @Mock
    private DischargeRepository repository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AgHuApiService agHuApiService;

    private DischargeService service;

    @BeforeEach
    void setUp() {
        service = new DischargeService(mapper, repository, appointmentRepository, agHuApiService);
    }

    @Test
    void generateReportFromAgHuShouldDelegateToMapper() {
        DischargeDTO dto = new DischargeDTO();
        Discharge expectedReport = Discharge.builder().clinicalSummary("ok").build();

        when(mapper.toReport(dto)).thenReturn(expectedReport);

        Discharge result = service.generateReportFromAgHu(Collections.singletonList(dto));

        assertThat(result).isSameAs(expectedReport);
        verify(mapper).toReport(dto);
    }

    @Test
    void generateReportFromAgHuShouldRejectNullOrEmptyPayload() {
        List<DischargeDTO> emptyPayload = Collections.emptyList();

        assertThatThrownBy(() -> service.generateReportFromAgHu(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("aghuPayload");

        assertThatThrownBy(() -> service.generateReportFromAgHu(emptyPayload))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("aghuPayload");
    }

    @Test
    void findAllShouldDelegateToRepository() {
        List<Discharge> reports = Arrays.asList(Discharge.builder().id(1L).build(), Discharge.builder().id(2L).build());
        when(repository.findAll()).thenReturn(reports);

        assertThat(service.findAll()).containsExactlyElementsOf(reports);
    }

    @Test
    void findAllSummariesShouldMapEachAppointment() {
        Appointment appointment1 = Appointment.builder().seqAtendimento(1L).build();
        Appointment appointment2 = Appointment.builder().seqAtendimento(2L).build();
        AppointmentDTO dto1 = AppointmentDTO.builder().seqAtendimento(1L).build();
        AppointmentDTO dto2 = AppointmentDTO.builder().seqAtendimento(2L).build();
        Page<Appointment> appointmentsPage = new PageImpl<>(Arrays.asList(appointment1, appointment2));
        PageRequest pageable = PageRequest.of(0, 10);

        when(appointmentRepository.findFilteredAppointments(null, null, null, null, null, false, pageable))
                .thenReturn(appointmentsPage);
        when(mapper.appointmentToSummaryDTO(appointment1)).thenReturn(dto1);
        when(mapper.appointmentToSummaryDTO(appointment2)).thenReturn(dto2);

        Page<AppointmentDTO> result = service.findAllSummaries(null, null, null, null, null, pageable);

        assertThat(result.getContent()).containsExactly(dto1, dto2);
        verify(mapper).appointmentToSummaryDTO(appointment1);
        verify(mapper).appointmentToSummaryDTO(appointment2);
    }

    @Test
    void findAllSummariesShouldNormalizeBuscaAndSetores() {
        PageRequest pageable = PageRequest.of(0, 5);
        Appointment appointment = Appointment.builder().seqAtendimento(9L).build();
        Page<Appointment> appointmentsPage = new PageImpl<>(Collections.singletonList(appointment));
        AppointmentDTO mapped = AppointmentDTO.builder().seqAtendimento(9L).build();

        when(appointmentRepository.findFilteredAppointments(
                "salva",
                "%  maria  %",
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31),
                Arrays.asList("clinica", "uti"),
                true,
                pageable))
                .thenReturn(appointmentsPage);
        when(mapper.appointmentToSummaryDTO(appointment)).thenReturn(mapped);

        Page<AppointmentDTO> result = service.findAllSummaries(
                "salva",
                "  Maria  ",
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31),
                Arrays.asList("CLINICA", "UTI"),
                pageable);

        assertThat(result.getContent()).containsExactly(mapped);
    }

    @Test
    void findByIdShouldDelegateToRepository() {
        Discharge report = Discharge.builder().id(5L).build();
        when(repository.findById(5L)).thenReturn(Optional.of(report));

        assertThat(service.findById(5L)).contains(report);
    }

    @Test
    void findByCompositeKeyShouldDelegateToRepository() {
        Discharge report = Discharge.builder().id(7L).build();
        when(repository.findByAghuDischargeIdAndPatientInfo_AghuRecord(123L, "456")).thenReturn(Optional.of(report));

        assertThat(service.findByCompositeKey(123L, "456")).contains(report);
    }

    @Test
    void getDashboardStatsShouldReturnZeroesWhenProjectionIsNull() {
        DashboardStatsDto result = service.getDashboardStats(null);

        assertThat(result)
                .extracting(
                        DashboardStatsDto::getTotal,
                        DashboardStatsDto::getPendentes,
                        DashboardStatsDto::getSalvas,
                        DashboardStatsDto::getEnviadas)
                .containsExactly(0L, 0L, 0L, 0L);
    }

    @Test
    void getDashboardStatsShouldNormalizeSetoresAndHandleNullCounters() {
        AppointmentRepository.DashboardStatsProjection projection = new AppointmentRepository.DashboardStatsProjection() {
            @Override
            public Long getTotal() {
                return 12L;
            }

            @Override
            public Long getPendentes() {
                return null;
            }

            @Override
            public Long getSalvas() {
                return 5L;
            }

            @Override
            public Long getEnviadas() {
                return null;
            }
        };
        when(appointmentRepository.getDashboardStats(Arrays.asList("clinica", "pediatria"), true))
                .thenReturn(projection);

        DashboardStatsDto result = service.getDashboardStats(Arrays.asList("CLINICA", "PEDIATRIA"));

        assertThat(result)
                .extracting(
                        DashboardStatsDto::getTotal,
                        DashboardStatsDto::getPendentes,
                        DashboardStatsDto::getSalvas,
                        DashboardStatsDto::getEnviadas)
                .containsExactly(12L, 0L, 5L, 0L);
    }

    @Test
    void getDailyStatsShouldNormalizeSetoresAndIgnoreIncompleteRows() {
        AppointmentRepository.DailyStatsProjection validRow = dailyStatsRow(3, 7L);
        AppointmentRepository.DailyStatsProjection nullDayRow = dailyStatsRow(null, 2L);
        AppointmentRepository.DailyStatsProjection nullQuantityRow = dailyStatsRow(4, null);

        when(appointmentRepository.getDailyStats(3, 2026, Arrays.asList("clinica"), true))
                .thenReturn(Arrays.asList(validRow, nullDayRow, nullQuantityRow));

        DailyStatsDto result = service.getDailyStats(3, 2026, Collections.singletonList("CLINICA"));

        assertThat(result)
                .extracting(DailyStatsDto::getMes, DailyStatsDto::getAno)
                .containsExactly(3, 2026);
        assertThat(result.getDados()).containsEntry(3, 7L).hasSize(1);
    }

    @Test
    void upsertByCompositeKeyShouldUpdateExistingDischargeAndSyncAppointment() {
        Long seqAtendimento = 100L;
        Discharge existing = Discharge.builder()
                .id(10L)
                .aghuDischargeId(seqAtendimento)
                .status("pendente")
                .therapeuticGuidance(null)
                .followUpSchedulingSuggestion(null)
                .build();
        DischargeComplementDTO dto = DischargeComplementDTO.builder()
                .ambulatoryProfile(new DischargeComplementDTO.AmbulatoryProfileUpdate(Boolean.TRUE, "Alta programada"))
                .therapeuticGuidance("Orientacao")
                .followUpSchedulingSuggestion("Retorno em 30 dias")
                .status("salva")
                .build();
        Appointment appointment = Appointment.builder()
                .seqAtendimento(seqAtendimento)
                .status("pendente")
                .pendenciaPreenchimento(Boolean.TRUE)
                .build();

        when(repository.findByAghuDischargeIdAndPatientInfo_AghuRecord(seqAtendimento, "123"))
                .thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenAnswer(invocation -> invocation.getArgument(0));
        when(appointmentRepository.findBySeqAtendimento(seqAtendimento)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        Discharge result = service.upsertByCompositeKey(seqAtendimento, "123", dto);

        assertThat(result)
                .extracting(Discharge::getStatus, Discharge::getTherapeuticGuidance, Discharge::getFollowUpSchedulingSuggestion)
                .containsExactly("salva", "Orientacao", "Retorno em 30 dias");
        assertThat(result.getAmbulatoryProfile())
                .extracting(Discharge.AmbulatoryProfile::getAdequate, Discharge.AmbulatoryProfile::getDischargeReason)
                .containsExactly(Boolean.TRUE, "Alta programada");
        assertThat(appointment)
                .extracting(Appointment::getStatus, Appointment::getPendenciaPreenchimento)
                .containsExactly("salva", false);
        verify(repository).save(existing);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void upsertByCompositeKeyShouldKeepStatusWhenExistingDischargeIsAlreadyEnviada() {
        Long seqAtendimento = 101L;
        Discharge existing = Discharge.builder()
                .id(11L)
                .aghuDischargeId(seqAtendimento)
                .status("enviada")
                .ambulatoryProfile(Discharge.AmbulatoryProfile.builder().adequate(Boolean.FALSE).dischargeReason("Anterior").build())
                .therapeuticGuidance("Orientacao existente")
                .followUpSchedulingSuggestion("Retorno existente")
                .build();
        DischargeComplementDTO dto = DischargeComplementDTO.builder()
                .ambulatoryProfile(new DischargeComplementDTO.AmbulatoryProfileUpdate(null, "Motivo novo"))
                .therapeuticGuidance("Atualizada")
                .followUpSchedulingSuggestion("Retorno atualizado")
                .status("salva")
                .build();
        Appointment appointment = Appointment.builder().seqAtendimento(seqAtendimento).status("pendente").build();

        when(repository.findByAghuDischargeIdAndPatientInfo_AghuRecord(seqAtendimento, "456"))
                .thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenAnswer(invocation -> invocation.getArgument(0));
        when(appointmentRepository.findBySeqAtendimento(seqAtendimento)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        Discharge result = service.upsertByCompositeKey(seqAtendimento, "456", dto);

        assertThat(result.getStatus()).isEqualTo("enviada");
        assertThat(result.getAmbulatoryProfile())
                .extracting(Discharge.AmbulatoryProfile::getAdequate, Discharge.AmbulatoryProfile::getDischargeReason)
                .containsExactly(Boolean.FALSE, "Motivo novo");
        assertThat(appointment.getStatus()).isEqualTo("salva");
        assertThat(appointment.getPendenciaPreenchimento()).isFalse();
    }

    @Test
    void upsertByCompositeKeyShouldFetchFromAgHuWhenNotFoundLocally() {
        Long seqAtendimento = 200L;
        DischargeDTO aghuData = new DischargeDTO();
        Discharge mapped = Discharge.builder()
                .id(20L)
                .aghuDischargeId(seqAtendimento)
                .status("pendente")
                .build();
        DischargeDTO.PatientDTO patient = new DischargeDTO.PatientDTO();
        patient.setNome("Paciente AGHU");
        aghuData.setPaciente(patient);
        DischargeComplementDTO dto = DischargeComplementDTO.builder()
                .ambulatoryProfile(new DischargeComplementDTO.AmbulatoryProfileUpdate(Boolean.TRUE, "Motivo"))
                .therapeuticGuidance("Orientacao")
                .followUpSchedulingSuggestion("Sem retorno definido")
                .status("salva")
                .build();

        when(repository.findByAghuDischargeIdAndPatientInfo_AghuRecord(seqAtendimento, "789"))
                .thenReturn(Optional.empty());
        when(agHuApiService.fetchDischargeData(seqAtendimento, "meac")).thenReturn(aghuData);
        when(mapper.toReport(aghuData)).thenReturn(mapped);
        when(repository.save(mapped)).thenAnswer(invocation -> invocation.getArgument(0));
        when(appointmentRepository.findBySeqAtendimento(seqAtendimento)).thenReturn(Optional.empty());

        Discharge result = service.upsertByCompositeKey(seqAtendimento, "789", dto);

        assertThat(result)
                .extracting(Discharge::getAghuDischargeId, Discharge::getStatus, Discharge::getTherapeuticGuidance)
                .containsExactly(seqAtendimento, "salva", "Orientacao");
        verify(agHuApiService).fetchDischargeData(seqAtendimento, "meac");
        verify(mapper).toReport(aghuData);
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void upsertByCompositeKeyShouldThrowWhenAgHuReturnsNull() {
        DischargeComplementDTO dto = DischargeComplementDTO.builder().build();

        when(repository.findByAghuDischargeIdAndPatientInfo_AghuRecord(300L, "999"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.upsertByCompositeKey(300L, "999", dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("300");
    }

    @Test
    void upsertByCompositeKeyShouldNotSyncAppointmentWhenStatusIsBlank() {
        Long seqAtendimento = 400L;
        Discharge existing = Discharge.builder()
                .id(40L)
                .aghuDischargeId(seqAtendimento)
                .status("pendente")
                .build();
        DischargeComplementDTO dto = DischargeComplementDTO.builder()
                .therapeuticGuidance("Nova orientacao")
                .status("")
                .build();
        Appointment appointment = Appointment.builder().seqAtendimento(seqAtendimento).status("pendente").build();

        when(repository.findByAghuDischargeIdAndPatientInfo_AghuRecord(seqAtendimento, "111"))
                .thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenAnswer(invocation -> invocation.getArgument(0));
        when(appointmentRepository.findBySeqAtendimento(seqAtendimento)).thenReturn(Optional.of(appointment));

        Discharge result = service.upsertByCompositeKey(seqAtendimento, "111", dto);

        assertThat(result.getStatus()).isEqualTo("pendente");
        assertThat(result.getTherapeuticGuidance()).isEqualTo("Nova orientacao");
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void saveShouldDelegateToRepository() {
        Discharge report = Discharge.builder().id(30L).build();
        when(repository.save(report)).thenReturn(report);

        assertThat(service.save(report)).isSameAs(report);
        verify(repository).save(report);
    }

    private AppointmentRepository.DailyStatsProjection dailyStatsRow(Integer dia, Long quantidade) {
        return new AppointmentRepository.DailyStatsProjection() {
            @Override
            public Integer getDia() {
                return dia;
            }

            @Override
            public Long getQuantidade() {
                return quantidade;
            }
        };
    }
}
