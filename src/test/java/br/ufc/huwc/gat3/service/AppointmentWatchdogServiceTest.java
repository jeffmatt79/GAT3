package br.ufc.huwc.gat3.service;

import br.ufc.huwc.gat3.dto.AppointmentDTO;
import br.ufc.huwc.gat3.model.Appointment;
import br.ufc.huwc.gat3.model.Unit;
import br.ufc.huwc.gat3.repositories.AppointmentRepository;
import br.ufc.huwc.gat3.repositories.UnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AppointmentWatchdogServiceTest {

    @Mock
    private AgHuApiService agHuApiService;

    @Mock
    private AppointmentRepository appointmentListRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private AppConfigService appConfigService;

    @InjectMocks
    private AppointmentWatchdogService watchdogService;

    private AppointmentDTO appointmentDTO;
    private LocalDateTime now;
    private Unit meac;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        meac = Unit.builder()
                .id(1L)
                .name("meac")
                .active(true)
                .build();

        appointmentDTO = AppointmentDTO.builder()
                .seqAtendimento(123L)
                .nomePaciente("Paciente Teste")
                .alteradoEm(now)
                .status("ATENDIDO")
                .build();

        lenient().when(unitRepository.findByActiveTrue()).thenReturn(Collections.singletonList(meac));
        lenient().when(appConfigService.getLastSuccessfulCheckTime()).thenReturn(now.minusDays(1));
    }

    @Test
    void shouldAbortWhenNoUnitsAreActive() {
        when(unitRepository.findByActiveTrue()).thenReturn(Collections.emptyList());

        watchdogService.checkForNewDischarges();

        verify(agHuApiService, never()).fetchAppointmentsRange(any(), any(), anyString());
        verify(appConfigService, never()).updateLastSuccessfulCheckTime(any());
    }

    @Test
    void shouldAbortWhenApiListIsEmpty() {
        when(agHuApiService.fetchAppointmentsRange(any(), any(), anyString()))
                .thenReturn(Collections.emptyList());

        watchdogService.checkForNewDischarges();

        verify(appointmentListRepository, never()).findBySeqAtendimento(anyLong());
        verify(appConfigService).updateLastSuccessfulCheckTime(any());
    }

    @Test
    void shouldSaveNewAppointment() {
        when(agHuApiService.fetchAppointmentsRange(any(), any(), anyString()))
                .thenReturn(Collections.singletonList(appointmentDTO));
        when(appointmentListRepository.findBySeqAtendimento(123L))
                .thenReturn(Optional.empty());

        watchdogService.checkForNewDischarges();

        verify(appointmentListRepository, times(1)).save(any(Appointment.class));
        verify(appConfigService, times(1)).updateLastSuccessfulCheckTime(any());
    }

    @Test
    void shouldUpdateExistingAppointment() {
        Appointment existing = Appointment.builder()
                .seqAtendimento(123L)
                .alteradoEm(now.minusDays(2))
                .unit(meac)
                .build();

        when(agHuApiService.fetchAppointmentsRange(any(), any(), anyString()))
                .thenReturn(Collections.singletonList(appointmentDTO));
        when(appointmentListRepository.findBySeqAtendimento(123L))
                .thenReturn(Optional.of(existing));

        watchdogService.checkForNewDischarges();

        verify(appointmentListRepository, times(1)).save(existing);
        assertThat(existing.getAlteradoEm()).isEqualTo(now);
        assertThat(existing.getUnit()).isEqualTo(meac);
    }

    @Test
    void shouldNotUpdateIfDateIsNotNewer() {
        Appointment existing = Appointment.builder()
                .seqAtendimento(123L)
                .alteradoEm(now)
                .unit(meac)
                .build();

        when(agHuApiService.fetchAppointmentsRange(any(), any(), anyString()))
                .thenReturn(Collections.singletonList(appointmentDTO));
        when(appointmentListRepository.findBySeqAtendimento(123L))
                .thenReturn(Optional.of(existing));

        watchdogService.checkForNewDischarges();

        verify(appointmentListRepository, never()).save(any());
    }

    @Test
    void shouldHandleMixedListCorrectly() {
        AppointmentDTO dtoNovo = AppointmentDTO.builder().seqAtendimento(1L).alteradoEm(now).build();
        AppointmentDTO dtoAtualizar = AppointmentDTO.builder().seqAtendimento(2L).alteradoEm(now).build();
        AppointmentDTO dtoIgnorar = AppointmentDTO.builder().seqAtendimento(3L).alteradoEm(now).build();

        Appointment entityAtualizar = Appointment.builder().seqAtendimento(2L).alteradoEm(now.minusDays(1))
                .build();
        Appointment entityIgnorar = Appointment.builder().seqAtendimento(3L).alteradoEm(now).build();

        when(agHuApiService.fetchAppointmentsRange(any(), any(), anyString()))
                .thenReturn(Arrays.asList(dtoNovo, dtoAtualizar, dtoIgnorar));

        when(appointmentListRepository.findBySeqAtendimento(1L)).thenReturn(Optional.empty());
        when(appointmentListRepository.findBySeqAtendimento(2L)).thenReturn(Optional.of(entityAtualizar));
        when(appointmentListRepository.findBySeqAtendimento(3L)).thenReturn(Optional.of(entityIgnorar));

        watchdogService.checkForNewDischarges();

        verify(appointmentListRepository, times(2)).save(any(Appointment.class));
        verify(appConfigService).updateLastSuccessfulCheckTime(any());
    }

    @Test
    void shouldUpdateIfEntityAlteradoEmIsNull() {
        Appointment existing = Appointment.builder()
                .seqAtendimento(123L)
                .alteradoEm(null)
                .unit(meac)
                .build();

        when(agHuApiService.fetchAppointmentsRange(any(), any(), anyString()))
                .thenReturn(Collections.singletonList(appointmentDTO));
        when(appointmentListRepository.findBySeqAtendimento(123L))
                .thenReturn(Optional.of(existing));

        watchdogService.checkForNewDischarges();

        verify(appointmentListRepository, times(1)).save(existing);
    }
}
