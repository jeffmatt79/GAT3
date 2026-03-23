package br.ufc.huwc.gat3.model;

import br.ufc.huwc.gat3.model.converter.MedicationListConverter;
import br.ufc.huwc.gat3.model.converter.StringListConverter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "discharges")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Discharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aghu_discharge_id", unique = true, nullable = false)
    private Long aghuDischargeId;

    @Column(name = "status")
    private String status;

    @Embedded
    private ServiceInfo serviceInfo;

    @Embedded
    private PatientInfo patientInfo;

    @Embedded
    private AmbulatoryProfile ambulatoryProfile;

    @Lob
    @Column(name = "clinical_summary")
    private String clinicalSummary;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT", name = "problem_list_json")
    @Builder.Default
    private List<String> problemList = Collections.emptyList();

    @Convert(converter = MedicationListConverter.class)
    @Column(columnDefinition = "TEXT", name = "medications_json")
    @Builder.Default
    private List<MedicationEntry> medications = Collections.emptyList();

    @Column(name = "therapeutic_guidance")
    private String therapeuticGuidance;

    @Column(name = "specialized_return_guidance")
    private String specializedReturnGuidance;

    @Column(name = "additional_information")
    private String additionalInformation;

    @Column(name = "follow_up_scheduling_suggestion")
    private String followUpSchedulingSuggestion;

    @Column(name = "responsible_physician")
    private String responsiblePhysician;

    @Column(name = "responsible_physician_registration")
    private String responsiblePhysicianRegistration;

    @Column(name = "report_date")
    private LocalDate reportDate;

    @Embeddable
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceInfo {

        @Column(name = "service_info_ambulatory_name")
        private String ambulatoryName;

        @Column(name = "service_info_subspecialty_or_service_chief")
        private String subspecialtyOrServiceChief;
    }

    @Embeddable
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientInfo {

        @Column(name = "patient_info_full_name")
        private String fullName;

        @Column(name = "patient_info_address")
        private String address;

        @Column(name = "patient_info_birth_date")
        private LocalDate birthDate;

        @Column(name = "patient_info_gender")
        private String gender;

        @Column(name = "patient_info_contacts")
        private String contacts;

        @Column(name = "patient_info_cns_or_cpf")
        private String cnsOrCpf;

        @Column(name = "patient_info_aghu_record")
        private String aghuRecord;

        @Column(name = "patient_info_mother_name")
        private String motherName;
    }

    @Embeddable
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AmbulatoryProfile {

        @Column(name = "ambulatory_profile_adequate")
        private Boolean adequate;

        @Column(name = "ambulatory_profile_initial_diagnosis_cid_10")
        private String initialDiagnosisCid10;

        @Column(name = "ambulatory_profile_discharge_reason")
        private String dischargeReason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicationEntry {
        private String name;
        private String dosage;
        private String quantity;
    }

    public boolean canEqual(Object other) {
        return other instanceof Discharge;
    }
}
