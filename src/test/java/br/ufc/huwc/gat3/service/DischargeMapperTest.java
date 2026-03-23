package br.ufc.huwc.gat3.service;

import br.ufc.huwc.gat3.dto.AppointmentDTO;
import br.ufc.huwc.gat3.model.Appointment;
import br.ufc.huwc.gat3.dto.DischargeDTO;
import br.ufc.huwc.gat3.model.Discharge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DischargeMapperTest {

    private DischargeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DischargeMapper();
    }

    @Test
    void toReportShouldThrowWhenInputIsNull() {
        assertThatThrownBy(() -> mapper.toReport(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("aghuData");
    }

    @Test
    void toReportShouldMapComprehensivePayload() {
        DischargeDTO dto = new DischargeDTO();

        DischargeDTO.AttendanceDTO attendance = new DischargeDTO.AttendanceDTO();
        attendance.setSeq(123L);
        dto.setAtendimentos(Collections.singletonList(attendance));

        DischargeDTO.SpecialtyDTO specialty = new DischargeDTO.SpecialtyDTO();
        specialty.setNomeEspecialidade("Infectologia");

        DischargeDTO.PersonDTO person = new DischargeDTO.PersonDTO();
        person.setNome("Dra. Silva");

        DischargeDTO.PreceptorDTO preceptor = new DischargeDTO.PreceptorDTO();
        preceptor.setPessoa(person);
        preceptor.setCreMec("12345");
        preceptor.setRqe("999");

        DischargeDTO.GradeDTO grade = new DischargeDTO.GradeDTO();
        grade.setEspecialidade(specialty);
        grade.setPreceptor(preceptor);
        dto.setGrade(grade);

        DischargeDTO.AddressDTO address = new DischargeDTO.AddressDTO();
        address.setLogradouro("Rua A");
        address.setNroLogradouro(10);
        address.setComplLogradouro("Apto 1");
        address.setBairro("Centro");
        DischargeDTO.CityDTO city = new DischargeDTO.CityDTO();
        city.setNome("Fortaleza");
        address.setCidade(city);
        
        DischargeDTO.UfDTO uf = new DischargeDTO.UfDTO();
        uf.setSigla("CE");
        address.setUf(uf);
        address.setBclCloCep(60000123L);

        DischargeDTO.ContactDTO contact = new DischargeDTO.ContactDTO();
        contact.setDdd(85);
        contact.setNroFone("99999999");

        DischargeDTO.PatientDTO patient = new DischargeDTO.PatientDTO();
        patient.setNome("Paciente Teste");
        patient.setProntuario(456L);
        patient.setNomeMae("Mae Teste");
        patient.setSexo("F");
        patient.setDtNascimento(OffsetDateTime.parse("1990-01-01T00:00:00Z"));
        patient.setEnderecos(Collections.singletonList(address));
        patient.setContatos(Collections.singletonList(contact));
        dto.setPaciente(patient);

        DischargeDTO.CidDetailDTO cidDetail = new DischargeDTO.CidDetailDTO();
        cidDetail.setCodigo("A00");
        cidDetail.setDescricao("Colera");
        DischargeDTO.CidProcessDTO cidProcess = new DischargeDTO.CidProcessDTO();
        cidProcess.setCid(cidDetail);
        dto.setCidsPrimarios(Collections.singletonList(cidProcess));

        DischargeDTO.AnamneseItemDTO anamneseItem = new DischargeDTO.AnamneseItemDTO();
        anamneseItem.setDescricao(
                "#HDA: Celulite e artrite\n#EXAME FISICO: bom estado\n#EXAMES COMPLEMENTARES: HIV nao reagente\n#MOTIVO DA ALTA: melhora\n#CONDUTA: usar medicamento\nretorno em 30 dias");
        DischargeDTO.AnamneseDTO anamnese = new DischargeDTO.AnamneseDTO();
        anamnese.setItens(Collections.singletonList(anamneseItem));
        dto.setAnamneses(Collections.singletonList(anamnese));

        DischargeDTO.PrescriptionItemDTO prescriptionItem = new DischargeDTO.PrescriptionItemDTO();
        prescriptionItem.setDescricao("Amoxicilina");
        prescriptionItem.setFormaUso("8/8h");
        prescriptionItem.setQuantidade("14 comprimidos");
        DischargeDTO.PrescriptionDTO prescription = new DischargeDTO.PrescriptionDTO();
        prescription.setItens(Collections.singletonList(prescriptionItem));
        dto.setReceituarios(Collections.singletonList(prescription));

        dto.setDtConsulta(OffsetDateTime.parse("2026-03-01T15:30:00Z"));

        Discharge report = mapper.toReport(dto);

        assertThat(report.getAghuDischargeId()).isEqualTo(123L);
        assertThat(report.getStatus()).isEqualTo("pendente");
        assertThat(report.getServiceInfo().getAmbulatoryName()).isEqualTo("Infectologia");
        assertThat(report.getServiceInfo().getSubspecialtyOrServiceChief()).isEqualTo("Dra. Silva");
        assertThat(report.getPatientInfo().getFullName()).isEqualTo("Paciente Teste");
        assertThat(report.getPatientInfo().getAghuRecord()).isEqualTo("456");
        assertThat(report.getPatientInfo().getGender()).isEqualTo("Feminino");
        assertThat(report.getPatientInfo().getAddress()).contains("Rua A", "No. 10", "CEP 60000123");
        assertThat(report.getPatientInfo().getContacts()).isEqualTo("(85) 99999999");
        assertThat(report.getAmbulatoryProfile().getInitialDiagnosisCid10()).isEqualTo("A00 - Colera");
        assertThat(report.getAmbulatoryProfile().getDischargeReason()).isEqualTo("melhora");
        assertThat(report.getClinicalSummary()).contains("Celulite e artrite", "bom estado", "HIV nao reagente");
        assertThat(report.getProblemList()).contains("Celulite infecciosa do olecrano direito", "Artrite septica do cotovelo direito");
        assertThat(report.getTherapeuticGuidance()).isEqualTo("usar medicamento\nretorno em 30 dias");
        assertThat(report.getSpecializedReturnGuidance()).isEqualTo("retorno em 30 dias");
        assertThat(report.getFollowUpSchedulingSuggestion()).isEqualTo("retorno em 30 dias");
        assertThat(report.getAdditionalInformation()).isEqualTo("HIV nao reagente");
        assertThat(report.getResponsiblePhysician()).isEqualTo("Dra. Silva");
        assertThat(report.getResponsiblePhysicianRegistration()).isEqualTo("CRM: 12345 | RQE: 999");
        assertThat(report.getReportDate()).isEqualTo(dto.getDtConsulta().toLocalDate());
        assertThat(report.getMedications()).hasSize(1);
        assertThat(report.getMedications().get(0).getName()).isEqualTo("Amoxicilina");
    }

    @Test
    void toReportShouldMapEmptyStructuresSafely() {
        DischargeDTO dto = new DischargeDTO();
        dto.setAtendimentos(Arrays.asList(new DischargeDTO.AttendanceDTO()));

        Discharge report = mapper.toReport(dto);

        assertThat(report.getAghuDischargeId()).isNull();
        assertThat(report.getProblemList()).isEmpty();
        assertThat(report.getMedications()).isEmpty();
        assertThat(report.getClinicalSummary()).isNull();
        assertThat(report.getFollowUpSchedulingSuggestion()).isNull();
    }

    @Test
    void toReportShouldPreferNumeroWhenPresent() {
        DischargeDTO dto = new DischargeDTO();
        dto.setNumero(999L);

        DischargeDTO.AttendanceDTO attendance = new DischargeDTO.AttendanceDTO();
        attendance.setSeq(123L);
        dto.setAtendimentos(Collections.singletonList(attendance));

        Discharge report = mapper.toReport(dto);

        assertThat(report.getAghuDischargeId()).isEqualTo(999L);
    }

    @Test
    void toReportShouldFallbackToTeamAndHandleUfNameAndSparseData() {
        DischargeDTO dto = new DischargeDTO();
        dto.setNumero(10L);
        dto.setDtConsulta(OffsetDateTime.parse("2026-03-01T15:30:00Z"));

        DischargeDTO.TeamDTO team = new DischargeDTO.TeamDTO();
        team.setNome("Clinica Medica");
        DischargeDTO.PersonDTO person = new DischargeDTO.PersonDTO();
        person.setNome("Dr. Team");
        DischargeDTO.PreceptorDTO preceptor = new DischargeDTO.PreceptorDTO();
        preceptor.setPessoa(person);
        preceptor.setCreMec("12345");
        DischargeDTO.GradeDTO grade = new DischargeDTO.GradeDTO();
        grade.setEquipe(team);
        grade.setPreceptor(preceptor);
        dto.setGrade(grade);

        DischargeDTO.AddressDTO address = new DischargeDTO.AddressDTO();
        address.setLogradouro("Rua B");
        DischargeDTO.UfDTO uf = new DischargeDTO.UfDTO();
        uf.setNome("Ceara");
        address.setUf(uf);

        DischargeDTO.ContactDTO contact = new DischargeDTO.ContactDTO();
        contact.setNroFone("88888888");

        DischargeDTO.PatientDTO patient = new DischargeDTO.PatientDTO();
        patient.setNome("Paciente 2");
        patient.setCpf("123");
        patient.setSexo("X");
        patient.setEnderecos(Collections.singletonList(address));
        patient.setContatos(Collections.singletonList(contact));
        dto.setPaciente(patient);

        DischargeDTO.CidProcessDTO emptyProcess = new DischargeDTO.CidProcessDTO();
        dto.setCidsPrimarios(Collections.singletonList(emptyProcess));
        dto.setReceituarios(Collections.singletonList(new DischargeDTO.PrescriptionDTO()));

        DischargeDTO.AnamneseItemDTO anamneseItem = new DischargeDTO.AnamneseItemDTO();
        anamneseItem.setDescricao("#CONDUTA: sem retorno definido");
        DischargeDTO.AnamneseDTO anamnese = new DischargeDTO.AnamneseDTO();
        anamnese.setItens(Collections.singletonList(anamneseItem));
        dto.setAnamneses(Collections.singletonList(anamnese));

        Discharge report = mapper.toReport(dto);

        assertThat(report.getServiceInfo().getAmbulatoryName()).isEqualTo("Clinica Medica");
        assertThat(report.getPatientInfo().getAddress()).contains("Rua B", "Ceara");
        assertThat(report.getPatientInfo().getContacts()).isEqualTo("88888888");
        assertThat(report.getPatientInfo().getGender()).isEqualTo("X");
        assertThat(report.getPatientInfo().getCnsOrCpf()).isEqualTo("123");
        assertThat(report.getResponsiblePhysicianRegistration()).isEqualTo("CRM: 12345");
        assertThat(report.getAmbulatoryProfile().getInitialDiagnosisCid10()).isEmpty();
        assertThat(report.getMedications()).isEmpty();
        assertThat(report.getSpecializedReturnGuidance()).isEqualTo("sem retorno definido");
    }

    @Test
    void appointmentToSummaryDTOShouldReturnNullWhenAppointmentIsNull() {
        assertThat(mapper.appointmentToSummaryDTO(null)).isNull();
    }

    @Test
    void appointmentToSummaryDTOShouldUseAppointmentFieldsWhenDischargeIsNull() {
        Appointment appointment = Appointment.builder()
                .seqAtendimento(1L)
                .alteradoEm(LocalDateTime.of(2026, 3, 1, 10, 0))
                .nomePaciente("Paciente")
                .prontuario(100L)
                .especialidade("Clinica")
                .altaAmbulatorial(Boolean.TRUE)
                .status("pendente")
                .pendenciaPreenchimento(Boolean.TRUE)
                .build();

        AppointmentDTO dto = mapper.appointmentToSummaryDTO(appointment);

        assertThat(dto)
                .extracting(
                        AppointmentDTO::getSeqAtendimento,
                        AppointmentDTO::getAlteradoEm,
                        AppointmentDTO::getNomePaciente,
                        AppointmentDTO::getProntuario,
                        AppointmentDTO::getEspecialidade,
                        AppointmentDTO::getAltaAmbulatorial,
                        AppointmentDTO::getStatus,
                        AppointmentDTO::getPendenciaPreenchimento,
                        AppointmentDTO::getMedicoResponsavel)
                .containsExactly(1L,
                        LocalDateTime.of(2026, 3, 1, 10, 0),
                        "Paciente",
                        100L,
                        "Clinica",
                        Boolean.TRUE,
                        "pendente",
                        Boolean.TRUE,
                        null);
    }

    @Test
    void appointmentToSummaryDTOShouldUseDischargeDataAndSetPendenciaTrueWhenRequiredFieldsMissing() {
        Discharge discharge = Discharge.builder()
                .status("salva")
                .responsiblePhysician("Dr. A")
                .ambulatoryProfile(Discharge.AmbulatoryProfile.builder().adequate(Boolean.TRUE).build())
                .therapeuticGuidance("")
                .followUpSchedulingSuggestion(null)
                .build();

        Appointment appointment = Appointment.builder()
                .seqAtendimento(2L)
                .alteradoEm(LocalDateTime.of(2026, 3, 2, 10, 0))
                .nomePaciente("Paciente 2")
                .prontuario(200L)
                .especialidade("Cardio")
                .altaAmbulatorial(Boolean.FALSE)
                .status("pendente")
                .pendenciaPreenchimento(Boolean.FALSE)
                .discharge(discharge)
                .build();

        AppointmentDTO dto = mapper.appointmentToSummaryDTO(appointment);

        assertThat(dto.getStatus()).isEqualTo("salva");
        assertThat(dto.getMedicoResponsavel()).isEqualTo("Dr. A");
        assertThat(dto.getPendenciaPreenchimento()).isTrue();
    }

    @Test
    void appointmentToSummaryDTOShouldUseDischargeDataAndSetPendenciaFalseWhenFieldsArePresent() {
        Discharge discharge = Discharge.builder()
                .status("enviada")
                .responsiblePhysician("Dra. B")
                .ambulatoryProfile(Discharge.AmbulatoryProfile.builder()
                        .adequate(Boolean.TRUE)
                        .dischargeReason("Melhora")
                        .build())
                .therapeuticGuidance("Manter")
                .followUpSchedulingSuggestion("Retorno em 30 dias")
                .build();

        Appointment appointment = Appointment.builder()
                .seqAtendimento(3L)
                .alteradoEm(LocalDateTime.of(2026, 3, 3, 10, 0))
                .nomePaciente("Paciente 3")
                .prontuario(300L)
                .especialidade("Ortopedia")
                .altaAmbulatorial(Boolean.TRUE)
                .status("pendente")
                .pendenciaPreenchimento(Boolean.TRUE)
                .discharge(discharge)
                .build();

        AppointmentDTO dto = mapper.appointmentToSummaryDTO(appointment);

        assertThat(dto.getStatus()).isEqualTo("enviada");
        assertThat(dto.getMedicoResponsavel()).isEqualTo("Dra. B");
        assertThat(dto.getPendenciaPreenchimento()).isFalse();
    }
}
