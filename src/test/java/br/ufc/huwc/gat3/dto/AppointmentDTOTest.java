package br.ufc.huwc.gat3.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AppointmentDTOTest {

    @Test
    void shouldSupportBuilderAndGetters() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 8, 10, 0);

        AppointmentDTO dto = AppointmentDTO.builder()
                .seqAtendimento(1L)
                .alteradoEm(now)
                .nomePaciente("Paciente")
                .prontuario(100L)
                .especialidade("Infectologia")
                .altaAmbulatorial(Boolean.TRUE)
                .status("pendente")
                .pendenciaPreenchimento(Boolean.FALSE)
                .build();

        assertThat(dto)
                .extracting(
                        AppointmentDTO::getSeqAtendimento,
                        AppointmentDTO::getAlteradoEm,
                        AppointmentDTO::getNomePaciente,
                        AppointmentDTO::getProntuario,
                        AppointmentDTO::getEspecialidade,
                        AppointmentDTO::getAltaAmbulatorial,
                        AppointmentDTO::getStatus,
                        AppointmentDTO::getPendenciaPreenchimento)
                .containsExactly(1L, now, "Paciente", 100L, "Infectologia", Boolean.TRUE, "pendente", Boolean.FALSE);
    }

    @Test
    void shouldSupportSettersAndEqualsHashCode() {
        AppointmentDTO dto1 = new AppointmentDTO();
        dto1.setSeqAtendimento(20L);
        dto1.setAlteradoEm(LocalDateTime.of(2026, 3, 8, 10, 0));
        dto1.setNomePaciente("Nome");
        dto1.setProntuario(100L);
        dto1.setEspecialidade("Clinica");
        dto1.setAltaAmbulatorial(Boolean.TRUE);
        dto1.setStatus("pendente");
        dto1.setPendenciaPreenchimento(Boolean.FALSE);
        dto1.setMedicoResponsavel("Dr. A");

        AppointmentDTO dto2 = new AppointmentDTO(
                20L,
                LocalDateTime.of(2026, 3, 8, 10, 0),
                "Nome",
                100L,
                "Clinica",
                Boolean.TRUE,
                "pendente",
                Boolean.FALSE,
                "Dr. A");

        assertThat(dto1)
                .isEqualTo(dto2)
                .hasSameHashCodeAs(dto2)
                .satisfies(value -> assertThat(value.toString())
                        .contains("seqAtendimento=20", "nomePaciente=Nome", "medicoResponsavel=Dr. A"));
    }

    @Test
    void shouldHandleEqualsSpecialCases() {
        AppointmentDTO dto = buildAppointmentDto();

        assertThat(dto)
                .returns(true, value -> value.equals(dto))
                .returns(false, value -> value.equals(null))
                .returns(false, value -> value.equals("outro tipo"));
    }

    @Test
    void shouldSupportEqualsAndHashCodeWhenAllFieldsAreNull() {
        AppointmentDTO dto1 = new AppointmentDTO();
        AppointmentDTO dto2 = new AppointmentDTO();

        assertThat(dto1)
                .isEqualTo(dto2)
                .hasSameHashCodeAs(dto2)
                .satisfies(value -> assertThat(value.toString()).contains("seqAtendimento=null", "medicoResponsavel=null"));
    }

    @Test
    void shouldDetectDifferenceForEachFieldInEquals() {
        AppointmentDTO base = buildAppointmentDto();

        assertThat(base)
                .isNotEqualTo(copyWith(base, 2L, null, null, null, null, null, null, null, null))
                .isNotEqualTo(copyWith(base, null, LocalDateTime.of(2026, 3, 9, 10, 0), null, null, null, null, null, null, null))
                .isNotEqualTo(copyWith(base, null, null, "Outro Paciente", null, null, null, null, null, null))
                .isNotEqualTo(copyWith(base, null, null, null, 200L, null, null, null, null, null))
                .isNotEqualTo(copyWith(base, null, null, null, null, "Pediatria", null, null, null, null))
                .isNotEqualTo(copyWith(base, null, null, null, null, null, Boolean.FALSE, null, null, null))
                .isNotEqualTo(copyWith(base, null, null, null, null, null, null, "salva", null, null))
                .isNotEqualTo(copyWith(base, null, null, null, null, null, null, null, Boolean.TRUE, null))
                .isNotEqualTo(copyWith(base, null, null, null, null, null, null, null, null, "Dr. B"));
    }

    private AppointmentDTO buildAppointmentDto() {
        return new AppointmentDTO(
                1L,
                LocalDateTime.of(2026, 3, 8, 10, 0),
                "Paciente",
                100L,
                "Infectologia",
                Boolean.TRUE,
                "pendente",
                Boolean.FALSE,
                "Dr. A");
    }

    private AppointmentDTO copyWith(
            AppointmentDTO source,
            Long seqAtendimento,
            LocalDateTime alteradoEm,
            String nomePaciente,
            Long prontuario,
            String especialidade,
            Boolean altaAmbulatorial,
            String status,
            Boolean pendenciaPreenchimento,
            String medicoResponsavel) {
        return new AppointmentDTO(
                seqAtendimento != null ? seqAtendimento : source.getSeqAtendimento(),
                alteradoEm != null ? alteradoEm : source.getAlteradoEm(),
                nomePaciente != null ? nomePaciente : source.getNomePaciente(),
                prontuario != null ? prontuario : source.getProntuario(),
                especialidade != null ? especialidade : source.getEspecialidade(),
                altaAmbulatorial != null ? altaAmbulatorial : source.getAltaAmbulatorial(),
                status != null ? status : source.getStatus(),
                pendenciaPreenchimento != null ? pendenciaPreenchimento : source.getPendenciaPreenchimento(),
                medicoResponsavel != null ? medicoResponsavel : source.getMedicoResponsavel());
    }
}
