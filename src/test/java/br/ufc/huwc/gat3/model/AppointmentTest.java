package br.ufc.huwc.gat3.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AppointmentTest {

    @Test
    void shouldSupportAllArgsConstructorAndGetters() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 15, 10, 0);
        Unit unit = buildUnit(1L, "meac");

        Appointment appointment = new Appointment(1L, now, "Joao", 10L, "Clinica", Boolean.TRUE, Boolean.FALSE, "salva", unit, now, null);

        assertThat(appointment)
                .extracting(
                        Appointment::getSeqAtendimento,
                        Appointment::getAlteradoEm,
                        Appointment::getNomePaciente,
                        Appointment::getProntuario,
                        Appointment::getEspecialidade,
                        Appointment::getPendenciaPreenchimento,
                        Appointment::getAltaAmbulatorial,
                        Appointment::getStatus,
                        Appointment::getUnit,
                        Appointment::getLastSyncedAt)
                .containsExactly(1L, now, "Joao", 10L, "Clinica", Boolean.TRUE, Boolean.FALSE, "salva", unit, now);
    }

    @Test
    void shouldSupportNoArgsConstructorAndSetters() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 15, 11, 0);
        Unit unit = buildUnit(2L, "huwc");

        Appointment appointment = new Appointment();
        appointment.setSeqAtendimento(2L);
        appointment.setAlteradoEm(now);
        appointment.setNomePaciente("Maria");
        appointment.setProntuario(20L);
        appointment.setEspecialidade("Pediatria");
        appointment.setPendenciaPreenchimento(Boolean.FALSE);
        appointment.setAltaAmbulatorial(Boolean.TRUE);
        appointment.setStatus("pendente");
        appointment.setUnit(unit);
        appointment.setLastSyncedAt(now);

        assertThat(appointment)
                .extracting(
                        Appointment::getSeqAtendimento,
                        Appointment::getAlteradoEm,
                        Appointment::getNomePaciente,
                        Appointment::getProntuario,
                        Appointment::getEspecialidade,
                        Appointment::getPendenciaPreenchimento,
                        Appointment::getAltaAmbulatorial,
                        Appointment::getStatus,
                        Appointment::getUnit,
                        Appointment::getLastSyncedAt)
                .containsExactly(2L, now, "Maria", 20L, "Pediatria", Boolean.FALSE, Boolean.TRUE, "pendente", unit, now);
    }

    @Test
    void shouldSupportBuilder() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 15, 12, 0);
        Unit unit = buildUnit(3L, "meac");

        Appointment.AppointmentBuilder builder = Appointment.builder()
                .seqAtendimento(3L)
                .alteradoEm(now)
                .nomePaciente("Carlos")
                .prontuario(30L)
                .especialidade("Infectologia")
                .pendenciaPreenchimento(Boolean.TRUE)
                .altaAmbulatorial(Boolean.FALSE)
                .status("enviada")
                .unit(unit)
                .lastSyncedAt(now);

        Appointment appointment = builder.build();

        assertThat(appointment)
                .extracting(
                        Appointment::getSeqAtendimento,
                        Appointment::getAlteradoEm,
                        Appointment::getNomePaciente,
                        Appointment::getProntuario,
                        Appointment::getEspecialidade,
                        Appointment::getPendenciaPreenchimento,
                        Appointment::getAltaAmbulatorial,
                        Appointment::getStatus,
                        Appointment::getUnit,
                        Appointment::getLastSyncedAt)
                .containsExactly(3L, now, "Carlos", 30L, "Infectologia", Boolean.TRUE, Boolean.FALSE, "enviada", unit, now);
        assertThat(builder.toString()).isNotBlank();
    }

    @Test
    void shouldSupportEqualsHashCodeAndToString() {
        Unit sharedUnit = buildUnit(1L, "meac");
        Appointment appointment1 = buildAppointment(sharedUnit);
        Appointment appointment2 = buildAppointment(sharedUnit);
        Appointment different = new Appointment(
                2L,
                appointment1.getAlteradoEm(),
                "Outro",
                20L,
                "Pediatria",
                Boolean.FALSE,
                Boolean.TRUE,
                "pendente",
                buildUnit(2L, "huwc"),
                appointment1.getLastSyncedAt(),
                null);

        assertThat(appointment1)
                .isEqualTo(appointment2)
                .hasSameHashCodeAs(appointment2)
                .isNotEqualTo(different)
                .satisfies(value -> assertThat(value.toString()).contains("seqAtendimento=1", "nomePaciente=Paciente", "status=salva", "unit="));
    }

    @Test
    void shouldHandleEqualsSpecialCases() {
        Appointment appointment = buildAppointment(buildUnit(1L, "meac"));

        assertThat(appointment)
                .returns(true, value -> value.equals(appointment))
                .returns(false, value -> value.equals(null))
                .returns(false, value -> value.equals("outro tipo"));
    }

    @Test
    void shouldSupportEqualsHashCodeWhenAllFieldsAreNull() {
        Appointment appointment1 = new Appointment();
        Appointment appointment2 = new Appointment();

        assertThat(appointment1)
                .isEqualTo(appointment2)
                .hasSameHashCodeAs(appointment2)
                .satisfies(value -> assertThat(value.toString()).contains("seqAtendimento=null", "status=null", "lastSyncedAt=null"));
    }

    @Test
    void shouldDetectDifferenceForEachFieldInEquals() {
        Appointment base = buildAppointment(buildUnit(1L, "meac"));
        LocalDateTime otherTime = LocalDateTime.of(2026, 3, 16, 10, 0);

        assertThat(base)
                .isNotEqualTo(copyWith(base, 2L, null, null, null, null, null, null, null, null, null))
                .isNotEqualTo(copyWith(base, null, otherTime, null, null, null, null, null, null, null, null))
                .isNotEqualTo(copyWith(base, null, null, "Outro Paciente", null, null, null, null, null, null, null))
                .isNotEqualTo(copyWith(base, null, null, null, 200L, null, null, null, null, null, null))
                .isNotEqualTo(copyWith(base, null, null, null, null, "Pediatria", null, null, null, null, null))
                .isNotEqualTo(copyWith(base, null, null, null, null, null, Boolean.FALSE, null, null, null, null))
                .isNotEqualTo(copyWith(base, null, null, null, null, null, null, Boolean.TRUE, null, null, null))
                .isNotEqualTo(copyWith(base, null, null, null, null, null, null, null, "pendente", null, null))
                .isNotEqualTo(copyWith(base, null, null, null, null, null, null, null, null, buildUnit(9L, "unit-x"), null))
                .isNotEqualTo(copyWith(base, null, null, null, null, null, null, null, null, null, otherTime));
    }

    private Appointment buildAppointment(Unit unit) {
        LocalDateTime now = LocalDateTime.of(2026, 3, 15, 9, 0);
        return new Appointment(1L, now, "Paciente", 100L, "Clinica", Boolean.TRUE, Boolean.FALSE, "salva", unit, now, null);
    }

    private Appointment copyWith(
            Appointment source,
            Long seqAtendimento,
            LocalDateTime alteradoEm,
            String nomePaciente,
            Long prontuario,
            String especialidade,
            Boolean pendenciaPreenchimento,
            Boolean altaAmbulatorial,
            String status,
            Unit unit,
            LocalDateTime lastSyncedAt) {
        return new Appointment(
                seqAtendimento != null ? seqAtendimento : source.getSeqAtendimento(),
                alteradoEm != null ? alteradoEm : source.getAlteradoEm(),
                nomePaciente != null ? nomePaciente : source.getNomePaciente(),
                prontuario != null ? prontuario : source.getProntuario(),
                especialidade != null ? especialidade : source.getEspecialidade(),
                pendenciaPreenchimento != null ? pendenciaPreenchimento : source.getPendenciaPreenchimento(),
                altaAmbulatorial != null ? altaAmbulatorial : source.getAltaAmbulatorial(),
                status != null ? status : source.getStatus(),
                unit != null ? unit : source.getUnit(),
                lastSyncedAt != null ? lastSyncedAt : source.getLastSyncedAt(),
                source.getDischarge());
    }

    private Unit buildUnit(Long id, String name) {
        return Unit.builder()
                .id(id)
                .name(name)
                .build();
    }
}
