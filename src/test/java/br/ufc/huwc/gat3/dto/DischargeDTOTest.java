package br.ufc.huwc.gat3.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class DischargeDTOTest {

    private final OffsetDateTime now = OffsetDateTime.parse("2026-03-08T12:00:00Z");

    @Test
    void shouldSerializeAndDeserializeWithNestedFields() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        DischargeDTO dto = buildDischarge(123L);

        String json = mapper.writeValueAsString(dto);
        DischargeDTO result = mapper.readValue(json, DischargeDTO.class);

        assertThat(result)
                .extracting(
                        DischargeDTO::getNumero,
                        DischargeDTO::getDtConsulta,
                        d -> d.getPaciente().getNome(),
                        d -> d.getGrade().getEquipe().getNome(),
                        d -> d.getAtendimentos().get(0).getSeq(),
                        d -> d.getAnamneses().get(0).getItens().get(0).getDescricao(),
                        d -> d.getReceituarios().get(0).getItens().get(0).getFormaUso(),
                        d -> d.getCidsPrimarios().get(0).getCid().getCodigo(),
                        d -> d.getEvolucoes().get(0).getItens().get(0).getDescricao())
                .containsExactly(123L, now, "Paciente Teste", "Equipe A", 999L, "HDA", "12/12h", "A00", "Boa evolucao");
    }

    @Test
    void shouldIgnoreUnknownProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        String json = "{\"numero\": 123, \"unknownField\": \"value\"}";
        DischargeDTO result = mapper.readValue(json, DischargeDTO.class);

        assertThat(result.getNumero()).isEqualTo(123L);
    }

    @Test
    void shouldSupportDataMethodsForDischargeDTO() {
        DischargeDTO dto1 = buildDischarge(1L);
        DischargeDTO dto2 = buildDischarge(1L);
        DischargeDTO dto3 = buildDischarge(2L);

        assertThat(dto1)
                .isEqualTo(dto2)
                .hasSameHashCodeAs(dto2)
                .isNotEqualTo(dto3)
                .satisfies(d -> assertThat(d.toString()).contains("numero=1", "paciente=", "grade=", "atendimentos="));
    }

    @Test
    void shouldHandleEqualsSpecialCasesForRootAndNestedDtos() {
        assertSpecialEqualsCases(buildDischarge(1L));
        assertSpecialEqualsCases(buildAnamnese("HDA"));
        assertSpecialEqualsCases(newAnamneseItem("Descricao 1"));
        assertSpecialEqualsCases(buildPrescription("Dipirona"));
        assertSpecialEqualsCases(newPrescriptionItem("Dipirona", "12/12h", "10"));
        assertSpecialEqualsCases(buildPatient());
        assertSpecialEqualsCases(buildAddress());
        assertSpecialEqualsCases(newCity("Fortaleza"));
        assertSpecialEqualsCases(newUf("CE", "Ceara"));
        assertSpecialEqualsCases(buildContact());
        assertSpecialEqualsCases(buildGrade());
        assertSpecialEqualsCases(newTeam("Equipe A"));
        assertSpecialEqualsCases(newPreceptor("Dr. House", "12345", "67890"));
        assertSpecialEqualsCases(newPerson("Dr. House"));
        assertSpecialEqualsCases(newSpecialty("Cardiologia"));
        assertSpecialEqualsCases(buildAttendance(999L));
        assertSpecialEqualsCases(buildEvolution("Boa evolucao"));
        assertSpecialEqualsCases(newEvolutionItem("Boa evolucao"));
        assertSpecialEqualsCases(buildCidProcess("A00", "Colera"));
        assertSpecialEqualsCases(newCidDetail("A00", "Colera"));
    }

    @Test
    void shouldSupportEqualsHashCodeWhenAllFieldsAreNullForRootAndNestedDtos() {
        assertNullEqualityContract(new DischargeDTO(), new DischargeDTO(), "numero=null", "evolucoes=null");
        assertNullEqualityContract(new DischargeDTO.AnamneseDTO(), new DischargeDTO.AnamneseDTO(), "itens=null");
        assertNullEqualityContract(new DischargeDTO.AnamneseItemDTO(), new DischargeDTO.AnamneseItemDTO(), "descricao=null");
        assertNullEqualityContract(new DischargeDTO.PrescriptionDTO(), new DischargeDTO.PrescriptionDTO(), "itens=null");
        assertNullEqualityContract(new DischargeDTO.PrescriptionItemDTO(), new DischargeDTO.PrescriptionItemDTO(), "descricao=null", "formaUso=null", "quantidade=null");
        assertNullEqualityContract(new DischargeDTO.PatientDTO(), new DischargeDTO.PatientDTO(), "prontuario=null", "cns=null");
        assertNullEqualityContract(new DischargeDTO.AddressDTO(), new DischargeDTO.AddressDTO(), "logradouro=null", "bclCloCep=null");
        assertNullEqualityContract(new DischargeDTO.CityDTO(), new DischargeDTO.CityDTO(), "nome=null");
        assertNullEqualityContract(new DischargeDTO.UfDTO(), new DischargeDTO.UfDTO(), "sigla=null", "nome=null");
        assertNullEqualityContract(new DischargeDTO.ContactDTO(), new DischargeDTO.ContactDTO(), "ddd=null", "nroFone=null");
        assertNullEqualityContract(new DischargeDTO.GradeDTO(), new DischargeDTO.GradeDTO(), "equipe=null", "especialidade=null");
        assertNullEqualityContract(new DischargeDTO.TeamDTO(), new DischargeDTO.TeamDTO(), "nome=null");
        assertNullEqualityContract(new DischargeDTO.PreceptorDTO(), new DischargeDTO.PreceptorDTO(), "pessoa=null", "rqe=null");
        assertNullEqualityContract(new DischargeDTO.PersonDTO(), new DischargeDTO.PersonDTO(), "nome=null");
        assertNullEqualityContract(new DischargeDTO.SpecialtyDTO(), new DischargeDTO.SpecialtyDTO(), "nomeEspecialidade=null");
        assertNullEqualityContract(new DischargeDTO.AttendanceDTO(), new DischargeDTO.AttendanceDTO(), "seq=null");
        assertNullEqualityContract(new DischargeDTO.EvolutionDTO(), new DischargeDTO.EvolutionDTO(), "itens=null");
        assertNullEqualityContract(new DischargeDTO.EvolutionItemDTO(), new DischargeDTO.EvolutionItemDTO(), "descricao=null");
        assertNullEqualityContract(new DischargeDTO.CidProcessDTO(), new DischargeDTO.CidProcessDTO(), "cid=null");
        assertNullEqualityContract(new DischargeDTO.CidDetailDTO(), new DischargeDTO.CidDetailDTO(), "codigo=null", "descricao=null");
    }

    @Test
    void shouldSupportDataMethodsForAnamneseDtos() {
        DischargeDTO.AnamneseItemDTO item1 = new DischargeDTO.AnamneseItemDTO();
        item1.setDescricao("Descricao 1");
        DischargeDTO.AnamneseItemDTO item2 = new DischargeDTO.AnamneseItemDTO();
        item2.setDescricao("Descricao 1");
        DischargeDTO.AnamneseItemDTO item3 = new DischargeDTO.AnamneseItemDTO();
        item3.setDescricao("Descricao 2");

        assertThat(item1)
                .isEqualTo(item2)
                .hasSameHashCodeAs(item2)
                .isNotEqualTo(item3)
                .satisfies(i -> assertThat(i.toString()).contains("descricao=Descricao 1"));

        DischargeDTO.AnamneseDTO anamnese1 = new DischargeDTO.AnamneseDTO();
        anamnese1.setItens(Collections.singletonList(item1));
        DischargeDTO.AnamneseDTO anamnese2 = new DischargeDTO.AnamneseDTO();
        anamnese2.setItens(Collections.singletonList(item2));
        DischargeDTO.AnamneseDTO anamnese3 = new DischargeDTO.AnamneseDTO();
        anamnese3.setItens(Collections.singletonList(item3));

        assertThat(anamnese1)
                .isEqualTo(anamnese2)
                .hasSameHashCodeAs(anamnese2)
                .isNotEqualTo(anamnese3)
                .satisfies(a -> assertThat(a.toString()).contains("itens="));
    }

    @Test
    void shouldSupportDataMethodsForPrescriptionDtos() {
        DischargeDTO.PrescriptionItemDTO item1 = new DischargeDTO.PrescriptionItemDTO();
        item1.setDescricao("Dipirona");
        item1.setFormaUso("12/12h");
        item1.setQuantidade("10");
        DischargeDTO.PrescriptionItemDTO item2 = new DischargeDTO.PrescriptionItemDTO();
        item2.setDescricao("Dipirona");
        item2.setFormaUso("12/12h");
        item2.setQuantidade("10");
        DischargeDTO.PrescriptionItemDTO item3 = new DischargeDTO.PrescriptionItemDTO();
        item3.setDescricao("Paracetamol");
        item3.setFormaUso("8/8h");
        item3.setQuantidade("5");

        assertThat(item1)
                .isEqualTo(item2)
                .hasSameHashCodeAs(item2)
                .isNotEqualTo(item3)
                .satisfies(i -> assertThat(i.toString()).contains("descricao=Dipirona", "formaUso=12/12h", "quantidade=10"));

        DischargeDTO.PrescriptionDTO p1 = new DischargeDTO.PrescriptionDTO();
        p1.setItens(Collections.singletonList(item1));
        DischargeDTO.PrescriptionDTO p2 = new DischargeDTO.PrescriptionDTO();
        p2.setItens(Collections.singletonList(item2));
        DischargeDTO.PrescriptionDTO p3 = new DischargeDTO.PrescriptionDTO();
        p3.setItens(Collections.singletonList(item3));

        assertThat(p1)
                .isEqualTo(p2)
                .hasSameHashCodeAs(p2)
                .isNotEqualTo(p3)
                .satisfies(p -> assertThat(p.toString()).contains("itens="));
    }

    @Test
    void shouldSupportDataMethodsForPatientContactAndAddressDtos() {
        DischargeDTO.AddressDTO address1 = new DischargeDTO.AddressDTO();
        address1.setLogradouro("Rua A");
        address1.setNroLogradouro(10);
        address1.setComplLogradouro("Casa");
        address1.setBairro("Centro");
        address1.setCidade(newCity("Fortaleza"));
        address1.setUf(newUf("CE", null));
        address1.setBclCloCep(60000000L);

        DischargeDTO.AddressDTO address2 = new DischargeDTO.AddressDTO();
        address2.setLogradouro("Rua A");
        address2.setNroLogradouro(10);
        address2.setComplLogradouro("Casa");
        address2.setBairro("Centro");
        address2.setCidade(newCity("Fortaleza"));
        address2.setUf(newUf("CE", null));
        address2.setBclCloCep(60000000L);

        DischargeDTO.AddressDTO address3 = new DischargeDTO.AddressDTO();
        address3.setCidade(newCity("Sobral"));

        assertThat(address1)
                .isEqualTo(address2)
                .hasSameHashCodeAs(address2)
                .isNotEqualTo(address3)
                .satisfies(a -> assertThat(a.toString()).contains("logradouro=Rua A", "cidade=", "uf="));

        DischargeDTO.ContactDTO contact1 = new DischargeDTO.ContactDTO();
        contact1.setDdd(85);
        contact1.setNroFone("999999999");
        DischargeDTO.ContactDTO contact2 = new DischargeDTO.ContactDTO();
        contact2.setDdd(85);
        contact2.setNroFone("999999999");
        DischargeDTO.ContactDTO contact3 = new DischargeDTO.ContactDTO();
        contact3.setDdd(11);
        contact3.setNroFone("988888888");

        assertThat(contact1)
                .isEqualTo(contact2)
                .hasSameHashCodeAs(contact2)
                .isNotEqualTo(contact3)
                .satisfies(c -> assertThat(c.toString()).contains("ddd=85", "nroFone=999999999"));

        DischargeDTO.PatientDTO patient1 = buildPatient();
        DischargeDTO.PatientDTO patient2 = buildPatient();
        DischargeDTO.PatientDTO patient3 = buildPatient();
        patient3.setNome("Outro Paciente");

        assertThat(patient1)
                .isEqualTo(patient2)
                .hasSameHashCodeAs(patient2)
                .isNotEqualTo(patient3)
                .satisfies(p -> assertThat(p.toString()).contains("prontuario=12345", "nome=Paciente Teste", "cpf=00011122233"));
    }

    @Test
    void shouldSupportDataMethodsForGradeTeamPreceptorPersonSpecialtyAndAttendanceDtos() {
        DischargeDTO.TeamDTO team1 = new DischargeDTO.TeamDTO();
        team1.setNome("Equipe A");
        DischargeDTO.TeamDTO team2 = new DischargeDTO.TeamDTO();
        team2.setNome("Equipe A");
        DischargeDTO.TeamDTO team3 = new DischargeDTO.TeamDTO();
        team3.setNome("Equipe B");

        assertThat(team1)
                .isEqualTo(team2)
                .hasSameHashCodeAs(team2)
                .isNotEqualTo(team3)
                .satisfies(t -> assertThat(t.toString()).contains("nome=Equipe A"));

        DischargeDTO.PersonDTO person1 = new DischargeDTO.PersonDTO();
        person1.setNome("Dr. House");
        DischargeDTO.PersonDTO person2 = new DischargeDTO.PersonDTO();
        person2.setNome("Dr. House");
        DischargeDTO.PersonDTO person3 = new DischargeDTO.PersonDTO();
        person3.setNome("Dr. Who");

        assertThat(person1)
                .isEqualTo(person2)
                .hasSameHashCodeAs(person2)
                .isNotEqualTo(person3)
                .satisfies(p -> assertThat(p.toString()).contains("nome=Dr. House"));

        DischargeDTO.PreceptorDTO preceptor1 = new DischargeDTO.PreceptorDTO();
        preceptor1.setPessoa(person1);
        preceptor1.setCreMec("12345");
        preceptor1.setRqe("67890");
        DischargeDTO.PreceptorDTO preceptor2 = new DischargeDTO.PreceptorDTO();
        preceptor2.setPessoa(person2);
        preceptor2.setCreMec("12345");
        preceptor2.setRqe("67890");
        DischargeDTO.PreceptorDTO preceptor3 = new DischargeDTO.PreceptorDTO();
        preceptor3.setPessoa(person3);
        preceptor3.setCreMec("00000");
        preceptor3.setRqe("11111");

        assertThat(preceptor1)
                .isEqualTo(preceptor2)
                .hasSameHashCodeAs(preceptor2)
                .isNotEqualTo(preceptor3)
                .satisfies(p -> assertThat(p.toString()).contains("creMec=12345", "rqe=67890"));

        DischargeDTO.SpecialtyDTO specialty1 = new DischargeDTO.SpecialtyDTO();
        specialty1.setNomeEspecialidade("Cardiologia");
        DischargeDTO.SpecialtyDTO specialty2 = new DischargeDTO.SpecialtyDTO();
        specialty2.setNomeEspecialidade("Cardiologia");
        DischargeDTO.SpecialtyDTO specialty3 = new DischargeDTO.SpecialtyDTO();
        specialty3.setNomeEspecialidade("Pediatria");

        assertThat(specialty1)
                .isEqualTo(specialty2)
                .hasSameHashCodeAs(specialty2)
                .isNotEqualTo(specialty3)
                .satisfies(s -> assertThat(s.toString()).contains("nomeEspecialidade=Cardiologia"));

        DischargeDTO.GradeDTO grade1 = new DischargeDTO.GradeDTO();
        grade1.setEquipe(team1);
        grade1.setPreceptor(preceptor1);
        grade1.setEspecialidade(specialty1);
        DischargeDTO.GradeDTO grade2 = new DischargeDTO.GradeDTO();
        grade2.setEquipe(team2);
        grade2.setPreceptor(preceptor2);
        grade2.setEspecialidade(specialty2);
        DischargeDTO.GradeDTO grade3 = new DischargeDTO.GradeDTO();
        grade3.setEquipe(team3);
        grade3.setPreceptor(preceptor3);
        grade3.setEspecialidade(specialty3);

        assertThat(grade1)
                .isEqualTo(grade2)
                .hasSameHashCodeAs(grade2)
                .isNotEqualTo(grade3)
                .satisfies(g -> assertThat(g.toString()).contains("equipe=", "preceptor=", "especialidade="));

        DischargeDTO.AttendanceDTO attendance1 = new DischargeDTO.AttendanceDTO();
        attendance1.setSeq(999L);
        DischargeDTO.AttendanceDTO attendance2 = new DischargeDTO.AttendanceDTO();
        attendance2.setSeq(999L);
        DischargeDTO.AttendanceDTO attendance3 = new DischargeDTO.AttendanceDTO();
        attendance3.setSeq(1000L);

        assertThat(attendance1)
                .isEqualTo(attendance2)
                .hasSameHashCodeAs(attendance2)
                .isNotEqualTo(attendance3)
                .satisfies(a -> assertThat(a.toString()).contains("seq=999"));
    }

    @Test
    void shouldSupportDataMethodsForEvolutionAndCidDtos() {
        DischargeDTO.EvolutionItemDTO evolutionItem1 = new DischargeDTO.EvolutionItemDTO();
        evolutionItem1.setDescricao("Boa evolucao");
        DischargeDTO.EvolutionItemDTO evolutionItem2 = new DischargeDTO.EvolutionItemDTO();
        evolutionItem2.setDescricao("Boa evolucao");
        DischargeDTO.EvolutionItemDTO evolutionItem3 = new DischargeDTO.EvolutionItemDTO();
        evolutionItem3.setDescricao("Piora clinica");

        assertThat(evolutionItem1)
                .isEqualTo(evolutionItem2)
                .hasSameHashCodeAs(evolutionItem2)
                .isNotEqualTo(evolutionItem3)
                .satisfies(e -> assertThat(e.toString()).contains("descricao=Boa evolucao"));

        DischargeDTO.EvolutionDTO evolution1 = new DischargeDTO.EvolutionDTO();
        evolution1.setItens(Collections.singletonList(evolutionItem1));
        DischargeDTO.EvolutionDTO evolution2 = new DischargeDTO.EvolutionDTO();
        evolution2.setItens(Collections.singletonList(evolutionItem2));
        DischargeDTO.EvolutionDTO evolution3 = new DischargeDTO.EvolutionDTO();
        evolution3.setItens(Collections.singletonList(evolutionItem3));

        assertThat(evolution1)
                .isEqualTo(evolution2)
                .hasSameHashCodeAs(evolution2)
                .isNotEqualTo(evolution3)
                .satisfies(e -> assertThat(e.toString()).contains("itens="));

        DischargeDTO.CidDetailDTO cidDetail1 = new DischargeDTO.CidDetailDTO();
        cidDetail1.setCodigo("A00");
        cidDetail1.setDescricao("Colera");
        DischargeDTO.CidDetailDTO cidDetail2 = new DischargeDTO.CidDetailDTO();
        cidDetail2.setCodigo("A00");
        cidDetail2.setDescricao("Colera");
        DischargeDTO.CidDetailDTO cidDetail3 = new DischargeDTO.CidDetailDTO();
        cidDetail3.setCodigo("B00");
        cidDetail3.setDescricao("Outra");

        assertThat(cidDetail1)
                .isEqualTo(cidDetail2)
                .hasSameHashCodeAs(cidDetail2)
                .isNotEqualTo(cidDetail3)
                .satisfies(c -> assertThat(c.toString()).contains("codigo=A00", "descricao=Colera"));

        DischargeDTO.CidProcessDTO cidProcess1 = new DischargeDTO.CidProcessDTO();
        cidProcess1.setCid(cidDetail1);
        DischargeDTO.CidProcessDTO cidProcess2 = new DischargeDTO.CidProcessDTO();
        cidProcess2.setCid(cidDetail2);
        DischargeDTO.CidProcessDTO cidProcess3 = new DischargeDTO.CidProcessDTO();
        cidProcess3.setCid(cidDetail3);

        assertThat(cidProcess1)
                .isEqualTo(cidProcess2)
                .hasSameHashCodeAs(cidProcess2)
                .isNotEqualTo(cidProcess3)
                .satisfies(c -> assertThat(c.toString()).contains("cid="));
    }

    private DischargeDTO buildDischarge(Long numero) {
        DischargeDTO dto = new DischargeDTO();
        dto.setNumero(numero);
        dto.setDtConsulta(now);
        dto.setPaciente(buildPatient());
        dto.setGrade(buildGrade());
        dto.setAtendimentos(Collections.singletonList(buildAttendance(999L)));
        dto.setAnamneses(Collections.singletonList(buildAnamnese("HDA")));
        dto.setReceituarios(Collections.singletonList(buildPrescription("Dipirona")));
        dto.setCidsPrimarios(Collections.singletonList(buildCidProcess("A00", "Colera")));
        dto.setCidsSecundarios(Collections.singletonList(buildCidProcess("B00", "Outra")));
        dto.setEvolucoes(Collections.singletonList(buildEvolution("Boa evolucao")));
        return dto;
    }

    private DischargeDTO.PatientDTO buildPatient() {
        DischargeDTO.PatientDTO patient = new DischargeDTO.PatientDTO();
        patient.setProntuario(12345L);
        patient.setNome("Paciente Teste");
        patient.setDtNascimento(now.minusYears(20));
        patient.setSexo("F");
        patient.setNomeMae("Maria Teste");
        patient.setEnderecos(Collections.singletonList(buildAddress()));
        patient.setContatos(Collections.singletonList(buildContact()));
        patient.setCpf("00011122233");
        patient.setCns("898001160000000");
        return patient;
    }

    private DischargeDTO.AddressDTO buildAddress() {
        DischargeDTO.AddressDTO address = new DischargeDTO.AddressDTO();
        address.setLogradouro("Rua A");
        address.setNroLogradouro(10);
        address.setComplLogradouro("Casa");
        address.setBairro("Centro");
        DischargeDTO.CityDTO city = new DischargeDTO.CityDTO();
        city.setNome("Fortaleza");
        address.setCidade(city);
        DischargeDTO.UfDTO uf = new DischargeDTO.UfDTO();
        uf.setSigla("CE");
        address.setUf(uf);
        address.setBclCloCep(60000000L);
        return address;
    }

    private DischargeDTO.ContactDTO buildContact() {
        DischargeDTO.ContactDTO contact = new DischargeDTO.ContactDTO();
        contact.setDdd(85);
        contact.setNroFone("999999999");
        return contact;
    }

    private DischargeDTO.GradeDTO buildGrade() {
        DischargeDTO.TeamDTO team = new DischargeDTO.TeamDTO();
        team.setNome("Equipe A");

        DischargeDTO.PersonDTO person = new DischargeDTO.PersonDTO();
        person.setNome("Dr. House");

        DischargeDTO.PreceptorDTO preceptor = new DischargeDTO.PreceptorDTO();
        preceptor.setPessoa(person);
        preceptor.setCreMec("12345");
        preceptor.setRqe("67890");

        DischargeDTO.SpecialtyDTO specialty = new DischargeDTO.SpecialtyDTO();
        specialty.setNomeEspecialidade("Cardiologia");

        DischargeDTO.GradeDTO grade = new DischargeDTO.GradeDTO();
        grade.setEquipe(team);
        grade.setPreceptor(preceptor);
        grade.setEspecialidade(specialty);
        return grade;
    }

    private DischargeDTO.AttendanceDTO buildAttendance(Long seq) {
        DischargeDTO.AttendanceDTO attendance = new DischargeDTO.AttendanceDTO();
        attendance.setSeq(seq);
        return attendance;
    }

    private DischargeDTO.AnamneseDTO buildAnamnese(String descricaoItem) {
        DischargeDTO.AnamneseItemDTO item = newAnamneseItem(descricaoItem);

        DischargeDTO.AnamneseDTO anamnese = new DischargeDTO.AnamneseDTO();
        anamnese.setItens(Collections.singletonList(item));
        return anamnese;
    }

    private DischargeDTO.PrescriptionDTO buildPrescription(String descricao) {
        DischargeDTO.PrescriptionItemDTO item = newPrescriptionItem(descricao, "12/12h", "10");

        DischargeDTO.PrescriptionDTO prescription = new DischargeDTO.PrescriptionDTO();
        prescription.setItens(Arrays.asList(item));
        return prescription;
    }

    private DischargeDTO.CidProcessDTO buildCidProcess(String codigo, String descricao) {
        DischargeDTO.CidDetailDTO detail = newCidDetail(codigo, descricao);

        DischargeDTO.CidProcessDTO process = new DischargeDTO.CidProcessDTO();
        process.setCid(detail);
        return process;
    }

    private DischargeDTO.EvolutionDTO buildEvolution(String descricaoItem) {
        DischargeDTO.EvolutionItemDTO item = newEvolutionItem(descricaoItem);

        DischargeDTO.EvolutionDTO evolution = new DischargeDTO.EvolutionDTO();
        evolution.setItens(Collections.singletonList(item));
        return evolution;
    }

    private DischargeDTO.AnamneseItemDTO newAnamneseItem(String descricao) {
        DischargeDTO.AnamneseItemDTO item = new DischargeDTO.AnamneseItemDTO();
        item.setDescricao(descricao);
        return item;
    }

    private DischargeDTO.PrescriptionItemDTO newPrescriptionItem(String descricao, String formaUso, String quantidade) {
        DischargeDTO.PrescriptionItemDTO item = new DischargeDTO.PrescriptionItemDTO();
        item.setDescricao(descricao);
        item.setFormaUso(formaUso);
        item.setQuantidade(quantidade);
        return item;
    }

    private DischargeDTO.TeamDTO newTeam(String nome) {
        DischargeDTO.TeamDTO team = new DischargeDTO.TeamDTO();
        team.setNome(nome);
        return team;
    }

    private DischargeDTO.CityDTO newCity(String nome) {
        DischargeDTO.CityDTO city = new DischargeDTO.CityDTO();
        city.setNome(nome);
        return city;
    }

    private DischargeDTO.UfDTO newUf(String sigla, String nome) {
        DischargeDTO.UfDTO uf = new DischargeDTO.UfDTO();
        uf.setSigla(sigla);
        uf.setNome(nome);
        return uf;
    }

    private DischargeDTO.PersonDTO newPerson(String nome) {
        DischargeDTO.PersonDTO person = new DischargeDTO.PersonDTO();
        person.setNome(nome);
        return person;
    }

    private DischargeDTO.PreceptorDTO newPreceptor(String nomePessoa, String creMec, String rqe) {
        DischargeDTO.PreceptorDTO preceptor = new DischargeDTO.PreceptorDTO();
        preceptor.setPessoa(newPerson(nomePessoa));
        preceptor.setCreMec(creMec);
        preceptor.setRqe(rqe);
        return preceptor;
    }

    private DischargeDTO.SpecialtyDTO newSpecialty(String nomeEspecialidade) {
        DischargeDTO.SpecialtyDTO specialty = new DischargeDTO.SpecialtyDTO();
        specialty.setNomeEspecialidade(nomeEspecialidade);
        return specialty;
    }

    private DischargeDTO.EvolutionItemDTO newEvolutionItem(String descricao) {
        DischargeDTO.EvolutionItemDTO item = new DischargeDTO.EvolutionItemDTO();
        item.setDescricao(descricao);
        return item;
    }

    private DischargeDTO.CidDetailDTO newCidDetail(String codigo, String descricao) {
        DischargeDTO.CidDetailDTO detail = new DischargeDTO.CidDetailDTO();
        detail.setCodigo(codigo);
        detail.setDescricao(descricao);
        return detail;
    }

    private void assertSpecialEqualsCases(Object value) {
        assertThat(value)
                .returns(true, current -> current.equals(value))
                .returns(false, current -> current.equals(null))
                .returns(false, current -> current.equals("outro tipo"));
    }

    private void assertNullEqualityContract(Object left, Object right, String... toStringFragments) {
        assertThat(left)
                .isEqualTo(right)
                .hasSameHashCodeAs(right)
                .satisfies(current -> assertThat(current.toString()).contains(toStringFragments));
    }
}
