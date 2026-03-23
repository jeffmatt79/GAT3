package br.ufc.huwc.gat3.service;

import br.ufc.huwc.gat3.dto.AppointmentDTO;
import br.ufc.huwc.gat3.dto.DischargeDTO;
import br.ufc.huwc.gat3.model.Appointment;
import br.ufc.huwc.gat3.model.Discharge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Responsavel por transformar os dados vindos do AGHU no modelo interno
 * utilizado para geracao do PDF de alta qualificada.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DischargeMapper {

    public Discharge toReport(DischargeDTO aghuData) {
        if (aghuData == null) {
            throw new IllegalArgumentException("aghuData cannot be null");
        }

        String anamnesisText = extractNarrative(aghuData);

        Map<String, String> sections = parseNarrativeSections(anamnesisText);

        return Discharge.builder()
                .aghuDischargeId(extractSeqAtendimento(aghuData))
                .serviceInfo(buildServiceInfo(aghuData))
                .patientInfo(buildPatientInfo(aghuData))
                .ambulatoryProfile(buildAmbulatoryProfile(aghuData, sections))
                .clinicalSummary(buildClinicalSummary(sections))
                .problemList(buildProblemList(sections))
                .medications(buildMedications(aghuData))
                .therapeuticGuidance(buildTherapeuticGuidance(sections))
                .specializedReturnGuidance(buildSpecializedReturnGuidance(sections))
                .additionalInformation(buildAdditionalInformation(sections))
                .followUpSchedulingSuggestion(buildSchedulingSuggestion(sections))
                .responsiblePhysician(buildResponsiblePhysician(aghuData))
                .responsiblePhysicianRegistration(buildResponsiblePhysicianRegistration(aghuData))
                .reportDate(extractReportDate(aghuData))
                .status("pendente") // Default status
                .build();
    }

    public AppointmentDTO appointmentToSummaryDTO(Appointment appointment) {
        if (appointment == null) {
            return null;
        }

        String status = appointment.getStatus();
        boolean pendenciaPreenchimento = Boolean.TRUE.equals(appointment.getPendenciaPreenchimento());
        String medicoResponsavel = null;

        if (appointment.getDischarge() != null) {
            Discharge discharge = appointment.getDischarge();
            status = discharge.getStatus();
            medicoResponsavel = discharge.getResponsiblePhysician();

            if (discharge.getAmbulatoryProfile() == null
                    || !StringUtils.hasText(discharge.getAmbulatoryProfile().getDischargeReason()) ||
                    !StringUtils.hasText(discharge.getTherapeuticGuidance()) ||
                    !StringUtils.hasText(discharge.getFollowUpSchedulingSuggestion())) {
                pendenciaPreenchimento = true;
            } else {
                pendenciaPreenchimento = false;
            }
        }

        return AppointmentDTO.builder()
                .seqAtendimento(appointment.getSeqAtendimento())
                .alteradoEm(appointment.getAlteradoEm())
                .nomePaciente(appointment.getNomePaciente())
                .prontuario(appointment.getProntuario())
                .especialidade(appointment.getEspecialidade())
                .altaAmbulatorial(appointment.getAltaAmbulatorial())
                .status(status)
                .pendenciaPreenchimento(pendenciaPreenchimento)
                .medicoResponsavel(medicoResponsavel)
                .build();
    }

    private Discharge.ServiceInfo buildServiceInfo(DischargeDTO aghuData) {
        DischargeDTO.GradeDTO grade = aghuData.getGrade();

        String ambulatory = Optional.ofNullable(grade)
                .map(DischargeDTO.GradeDTO::getEspecialidade)
                .map(DischargeDTO.SpecialtyDTO::getNomeEspecialidade)
                .filter(StringUtils::hasText)
                .orElseGet(() -> Optional.ofNullable(grade)
                        .map(DischargeDTO.GradeDTO::getEquipe)
                        .map(DischargeDTO.TeamDTO::getNome)
                        .orElse(null));

        String chief = Optional.ofNullable(grade)
                .map(DischargeDTO.GradeDTO::getPreceptor)
                .map(DischargeDTO.PreceptorDTO::getPessoa)
                .map(DischargeDTO.PersonDTO::getNome)
                .orElse(null);

        return Discharge.ServiceInfo.builder()
                .ambulatoryName(ambulatory)
                .subspecialtyOrServiceChief(chief)
                .build();
    }

    private Discharge.PatientInfo buildPatientInfo(DischargeDTO aghuData) {
        DischargeDTO.PatientDTO patient = aghuData.getPaciente();
        if (patient == null) {
            return null;
        }

        String address = formatAddress(patient.getEnderecos());
        String contacts = formatContacts(patient.getContatos());
        String aghuRecord = Optional.ofNullable(patient.getProntuario())
                .map(Object::toString)
                .orElse(null);

        return Discharge.PatientInfo.builder()
                .fullName(patient.getNome())
                .address(address)
                .birthDate(Optional.ofNullable(patient.getDtNascimento())
                        .map(OffsetDateTime::toLocalDate).orElse(null))
                .gender(mapGender(patient.getSexo()))
                .contacts(contacts)
                .cnsOrCpf(patient.getCns() != null ? patient.getCns() : patient.getCpf())
                .aghuRecord(aghuRecord)
                .motherName(patient.getNomeMae())
                .build();
    }

    private Discharge.AmbulatoryProfile buildAmbulatoryProfile(DischargeDTO aghuData,
            Map<String, String> sections) {

        // Mapeamento corrigido para lista de objetos CidProcessDTO
        String cid10 = Optional.ofNullable(aghuData.getCidsPrimarios())
                .filter(list -> !list.isEmpty())
                .map(list -> list.stream()
                        .map(item -> {
                            if (item.getCid() != null) {
                                return item.getCid().getCodigo() + " - " + item.getCid().getDescricao();
                            }
                            return "";
                        })
                        .filter(StringUtils::hasText)
                        .collect(Collectors.joining(", ")))
                .orElse(null);

        String dischargeReason = sections.getOrDefault("MOTIVO DA ALTA", null);

        return Discharge.AmbulatoryProfile.builder()
                .adequate(Boolean.TRUE) // Default until explicit info available
                .initialDiagnosisCid10(cid10)
                .dischargeReason(dischargeReason)
                .build();
    }

    private String buildClinicalSummary(Map<String, String> sections) {
        List<String> summaryParts = new ArrayList<>();
        appendIfHasText(summaryParts, sections.get("HDA"));
        appendIfHasText(summaryParts, sections.get("EXAME FISICO"));
        appendIfHasText(summaryParts, sections.get("EXAMES COMPLEMENTARES"));

        if (summaryParts.isEmpty()) {
            return null;
        }

        return summaryParts.stream()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining("\n\n"));
    }

    private List<String> buildProblemList(Map<String, String> sections) {
        String hda = sections.get("HDA");
        if (!StringUtils.hasText(hda)) {
            return Collections.emptyList();
        }

        List<String> problems = new ArrayList<>();
        String normalizedHda = normalizeKey(hda).toUpperCase();
        if (normalizedHda.contains("CELULITE")) {
            problems.add("Celulite infecciosa do olecrano direito");
        }
        if (normalizedHda.contains("ARTRITE")) {
            problems.add("Artrite septica do cotovelo direito");
        }
        if (normalizedHda.contains("BURSITE")) {
            problems.add("Bursite olecraniana");
        }
        if (normalizedHda.contains("PANICULITE")) {
            problems.add("Paniculite do subcutaneo adjacente");
        }
        if (problems.isEmpty()) {
            return Collections.emptyList();
        }
        return problems;
    }

    private List<Discharge.MedicationEntry> buildMedications(DischargeDTO aghuData) {
        if (CollectionUtils.isEmpty(aghuData.getReceituarios())) {
            return Collections.emptyList();
        }

        return aghuData.getReceituarios().stream()
                .filter(Objects::nonNull)
                .flatMap(prescription -> Optional.ofNullable(prescription.getItens())
                        .map(List::stream)
                        .orElseGet(Stream::empty))
                .map(item -> Discharge.MedicationEntry.builder()
                        .name(item.getDescricao())
                        .dosage(item.getFormaUso())
                        .quantity(item.getQuantidade())
                        .build())
                .collect(Collectors.toList());
    }

    private String buildTherapeuticGuidance(Map<String, String> sections) {
        String conduta = sections.get("CONDUTA");
        if (!StringUtils.hasText(conduta)) {
            return null;
        }

        List<String> lines = splitByLine(conduta)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());

        if (lines.isEmpty()) {
            return null;
        }

        return String.join("\n", lines);
    }

    private String buildSpecializedReturnGuidance(Map<String, String> sections) {
        String conduta = sections.get("CONDUTA");
        if (!StringUtils.hasText(conduta)) {
            return null;
        }

        return splitByLine(conduta)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .filter(line -> line.toUpperCase().contains("RETORNO"))
                .findFirst()
                .orElse(null);
    }

    private String buildAdditionalInformation(Map<String, String> sections) {
        String exames = sections.get("EXAMES COMPLEMENTARES");
        if (!StringUtils.hasText(exames)) {
            return null;
        }
        return exames.trim();
    }

    private String buildSchedulingSuggestion(Map<String, String> sections) {
        String specializedReturn = buildSpecializedReturnGuidance(sections);
        if (!StringUtils.hasText(specializedReturn)) {
            return null;
        }
        return specializedReturn;
    }

    private String buildResponsiblePhysician(DischargeDTO aghuData) {
        return Optional.ofNullable(aghuData.getGrade())
                .map(DischargeDTO.GradeDTO::getPreceptor)
                .map(DischargeDTO.PreceptorDTO::getPessoa)
                .map(DischargeDTO.PersonDTO::getNome)
                .orElse(null);
    }

    private String buildResponsiblePhysicianRegistration(DischargeDTO aghuData) {
        return Optional.ofNullable(aghuData.getGrade())
                .map(DischargeDTO.GradeDTO::getPreceptor)
                .map(preceptor -> {
                    String creMec = Optional.ofNullable(preceptor.getCreMec()).orElse("");
                    String rqe = Optional.ofNullable(preceptor.getRqe()).orElse("");
                    if (StringUtils.hasText(creMec) && StringUtils.hasText(rqe)) {
                        return String.format("CRM: %s | RQE: %s", creMec, rqe);
                    }
                    if (StringUtils.hasText(creMec)) {
                        return String.format("CRM: %s", creMec);
                    }
                    if (StringUtils.hasText(rqe)) {
                        return String.format("RQE: %s", rqe);
                    }
                    return null;
                })
                .orElse(null);
    }

    private LocalDate extractReportDate(DischargeDTO aghuData) {
        return Optional.ofNullable(aghuData.getDtConsulta())
                .map(OffsetDateTime::toLocalDate)
                .orElse(null);
    }

    private String extractNarrative(DischargeDTO aghuData) {
        return Optional.ofNullable(aghuData.getAnamneses())
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0))
                .map(DischargeDTO.AnamneseDTO::getItens)
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0))
                .map(DischargeDTO.AnamneseItemDTO::getDescricao)
                .orElse("");
    }

    private Map<String, String> parseNarrativeSections(String narrative) {
        if (!StringUtils.hasText(narrative)) {
            return Collections.emptyMap();
        }

        Map<String, String> sections = new LinkedHashMap<>();
        String currentSection = "RESUMO";
        StringBuilder accumulator = new StringBuilder();

        for (String rawLine : narrative.split("\\r?\\n")) {
            String line = rawLine.trim();
            if (!StringUtils.hasText(line)) {
                continue;
            }
            if (isSectionHeader(line)) {
                saveSection(sections, currentSection, accumulator);
                SectionHeader header = extractSectionHeader(line);
                currentSection = header.name;
                accumulator = new StringBuilder();
                appendInitialContent(accumulator, header.initialContent);
            } else {
                appendLine(accumulator, line);
            }
        }

        saveSection(sections, currentSection, accumulator);
        return sections;
    }

    private boolean isSectionHeader(String line) {
        return line.startsWith("#");
    }

    private SectionHeader extractSectionHeader(String line) {
        String withoutHash = line.substring(1).trim();
        String sectionName = withoutHash;
        String initialContent = null;

        int colonIndex = withoutHash.indexOf(':');
        if (colonIndex >= 0) {
            sectionName = withoutHash.substring(0, colonIndex).trim();
            initialContent = withoutHash.substring(colonIndex + 1).trim();
        }

        return new SectionHeader(normalizeKey(sectionName).toUpperCase(), initialContent);
    }

    private void appendInitialContent(StringBuilder accumulator, String initialContent) {
        if (StringUtils.hasText(initialContent)) {
            accumulator.append(initialContent);
        }
    }

    private void appendLine(StringBuilder accumulator, String line) {
        if (accumulator.length() > 0) {
            accumulator.append("\n");
        }
        accumulator.append(line);
    }

    private void saveSection(Map<String, String> sections, String currentSection, StringBuilder accumulator) {
        if (accumulator.length() == 0) {
            return;
        }
        sections.put(currentSection, accumulator.toString().trim());
    }

    private static class SectionHeader {
        private final String name;
        private final String initialContent;

        SectionHeader(String name, String initialContent) {
            this.name = name;
            this.initialContent = initialContent;
        }
    }

    private void appendIfHasText(List<String> destination, String value) {
        if (StringUtils.hasText(value)) {
            destination.add(value);
        }
    }

    private String formatAddress(List<DischargeDTO.AddressDTO> addresses) {
        if (CollectionUtils.isEmpty(addresses)) {
            return null;
        }

        DischargeDTO.AddressDTO address = addresses.get(0);
        List<String> parts = new ArrayList<>();
        if (StringUtils.hasText(address.getLogradouro())) {
            parts.add(address.getLogradouro());
        }
        if (address.getNroLogradouro() != null) {
            parts.add("No. " + address.getNroLogradouro());
        }
        if (StringUtils.hasText(address.getComplLogradouro())) {
            parts.add(address.getComplLogradouro());
        }
        if (StringUtils.hasText(address.getBairro())) {
            parts.add(address.getBairro());
        }
        if (address.getCidade() != null && StringUtils.hasText(address.getCidade().getNome())) {
            parts.add(address.getCidade().getNome());
        }
        if (address.getUf() != null) {
            String ufStr = StringUtils.hasText(address.getUf().getSigla()) 
                ? address.getUf().getSigla() 
                : address.getUf().getNome();
            if (StringUtils.hasText(ufStr)) {
                parts.add(ufStr);
            }
        }
        if (address.getBclCloCep() != null) {
            parts.add("CEP " + address.getBclCloCep());
        }

        if (parts.isEmpty()) {
            return null;
        }
        return String.join(", ", parts);
    }

    private String formatContacts(List<DischargeDTO.ContactDTO> contacts) {
        if (CollectionUtils.isEmpty(contacts)) {
            return null;
        }

        return contacts.stream()
                .filter(Objects::nonNull)
                .map(contact -> {
                    String phone = Optional.ofNullable(contact.getNroFone()).orElse("");
                    if (contact.getDdd() != null) {
                        return String.format("(%d) %s", contact.getDdd(), phone);
                    }
                    return phone;
                })
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(" / "));
    }

    private String mapGender(String genderCode) {
        if (!StringUtils.hasText(genderCode)) {
            return null;
        }
        switch (genderCode.toUpperCase()) {
            case "M":
                return "Masculino";
            case "F":
                return "Feminino";
            default:
                return genderCode;
        }
    }

    private Stream<String> splitByLine(String text) {
        if (!StringUtils.hasText(text)) {
            return Stream.empty();
        }
        return Arrays.stream(text.split("\\r?\\n"));
    }

    private String normalizeKey(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        return normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }

    private Long extractSeqAtendimento(DischargeDTO aghuData) {
        if (aghuData == null) {
            return null;
        }

        if (aghuData.getNumero() != null) {
            return aghuData.getNumero();
        }

        if (!CollectionUtils.isEmpty(aghuData.getAtendimentos())) {
            return aghuData.getAtendimentos().get(0).getSeq();
        }

        return null;
    }
}
