package br.ufc.huwc.gat3.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class DischargeTest {

        private final LocalDate now = LocalDate.now();
        private final LocalDate later = now.plusDays(1);

        @Test
        void builderShouldApplyDefaults() {
                Discharge report = Discharge.builder()
                                .aghuDischargeId(100L)
                                .clinicalSummary("sum")
                                .reportDate(LocalDate.now())
                                .build();

                assertThat(report.getProblemList()).isNotNull().isEmpty();
                assertThat(report.getMedications()).isNotNull().isEmpty();
                assertThat(report.getAghuDischargeId()).isEqualTo(100L);
        }

        @Test
        void shouldStoreNestedValueObjects() {
                Discharge.ServiceInfo serviceInfo = Discharge.ServiceInfo.builder()
                                .ambulatoryName("AMB")
                                .subspecialtyOrServiceChief("Chief")
                                .build();
                Discharge.PatientInfo patientInfo = Discharge.PatientInfo.builder()
                                .fullName("Nome")
                                .address("Rua 1")
                                .birthDate(LocalDate.of(1990, 1, 1))
                                .gender("M")
                                .contacts("999")
                                .cnsOrCpf("123")
                                .aghuRecord("PRONT-1")
                                .motherName("Mae")
                                .build();
                Discharge.AmbulatoryProfile profile = Discharge.AmbulatoryProfile.builder()
                                .adequate(Boolean.TRUE)
                                .initialDiagnosisCid10("A00")
                                .dischargeReason("Alta")
                                .build();
                Discharge.MedicationEntry med = Discharge.MedicationEntry.builder()
                                .name("Med")
                                .dosage("1x")
                                .quantity("10")
                                .build();

                Discharge report = Discharge.builder()
                                .aghuDischargeId(200L)
                                .serviceInfo(serviceInfo)
                                .patientInfo(patientInfo)
                                .ambulatoryProfile(profile)
                                .medications(Collections.singletonList(med))
                                .problemList(Collections.singletonList("Prob"))
                                .build();

                assertThat(report.getServiceInfo().getAmbulatoryName()).isEqualTo("AMB");
                assertThat(report.getPatientInfo().getMotherName()).isEqualTo("Mae");
                assertThat(report.getAghuDischargeId()).isEqualTo(200L);
                assertThat(report.getAmbulatoryProfile().getInitialDiagnosisCid10()).isEqualTo("A00");
                assertThat(report.getMedications()).hasSize(1);
                assertThat(report.getProblemList()).contains("Prob");
        }

        @Test
        void shouldExposeAllFieldsViaSetters() {
                Discharge.ServiceInfo serviceInfo = new Discharge.ServiceInfo();
                serviceInfo.setAmbulatoryName("Servicio");
                serviceInfo.setSubspecialtyOrServiceChief("Chief 2");

                Discharge.PatientInfo patientInfo = new Discharge.PatientInfo();
                patientInfo.setFullName("Nome Completo");
                patientInfo.setAddress("Endereco");
                patientInfo.setBirthDate(LocalDate.of(1985, 2, 20));
                patientInfo.setGender("F");
                patientInfo.setContacts("98888");
                patientInfo.setCnsOrCpf("321");
                patientInfo.setAghuRecord("PRONT-2");
                patientInfo.setMotherName("Mae 2");

                Discharge.AmbulatoryProfile profile = new Discharge.AmbulatoryProfile();
                profile.setAdequate(Boolean.FALSE);
                profile.setInitialDiagnosisCid10("B00");
                profile.setDischargeReason("Transferencia");

                Discharge.MedicationEntry medicationEntry = new Discharge.MedicationEntry();
                medicationEntry.setName("Medicamento");
                medicationEntry.setDosage("2x ao dia");
                medicationEntry.setQuantity("20 comprimidos");

                Discharge report = new Discharge();
                report.setId(5L);
                report.setAghuDischargeId(300L);
                report.setServiceInfo(serviceInfo);
                report.setPatientInfo(patientInfo);
                report.setAmbulatoryProfile(profile);
                report.setClinicalSummary("Resumo");
                report.setProblemList(Arrays.asList("P1", "P2"));
                report.setMedications(Collections.singletonList(medicationEntry));
                report.setTherapeuticGuidance("Guia");
                report.setSpecializedReturnGuidance("retorno");
                report.setAdditionalInformation("Info");
                report.setFollowUpSchedulingSuggestion("Agendar");
                report.setResponsiblePhysician("Medico");
                report.setResponsiblePhysicianRegistration("CRM-123");
                report.setReportDate(LocalDate.of(2025, 1, 1));

                assertThat(report.getAghuDischargeId()).isEqualTo(300L);
                assertThat(report.getClinicalSummary()).isEqualTo("Resumo");
                assertThat(report.getProblemList()).containsExactly("P1", "P2");
                assertThat(report.getMedications().get(0).getName()).isEqualTo("Medicamento");
                assertThat(report.getTherapeuticGuidance()).isEqualTo("Guia");
                assertThat(report.getSpecializedReturnGuidance()).isEqualTo("retorno");
                assertThat(report.getAdditionalInformation()).isEqualTo("Info");
                assertThat(report.getFollowUpSchedulingSuggestion()).isEqualTo("Agendar");
                assertThat(report.getResponsiblePhysician()).isEqualTo("Medico");
                assertThat(report.getResponsiblePhysicianRegistration()).isEqualTo("CRM-123");
                assertThat(report.getReportDate()).isEqualTo(LocalDate.of(2025, 1, 1));
                assertThat(report.getServiceInfo().getSubspecialtyOrServiceChief()).isEqualTo("Chief 2");
                assertThat(report.getPatientInfo().getMotherName()).isEqualTo("Mae 2");
                assertThat(report.getAmbulatoryProfile().getDischargeReason()).isEqualTo("Transferencia");
        }

        @Test
        void shouldVerifyEqualsHashCodeAndToStringForServiceInfo() {
                Discharge.ServiceInfo base = new Discharge.ServiceInfo("Amb", "Chief");
                Discharge.ServiceInfo same = new Discharge.ServiceInfo("Amb", "Chief");
                verifyEquality(base, same);

                verifyInequality(base, new Discharge.ServiceInfo("X", "Chief"));
                verifyInequality(base, new Discharge.ServiceInfo("Amb", "X"));
        }

        @Test
        void shouldVerifyEqualsHashCodeAndToStringForPatientInfo() {
                Discharge.PatientInfo base = new Discharge.PatientInfo(
                                "N", "A", now, "M", "C", "123", "P1", "M");
                Discharge.PatientInfo same = new Discharge.PatientInfo(
                                "N", "A", now, "M", "C", "123", "P1", "M");
                verifyEquality(base, same);

                verifyInequality(base, new Discharge.PatientInfo("X", "A", now, "M", "C", "123", "P1", "M"));
                verifyInequality(base, new Discharge.PatientInfo("N", "X", now, "M", "C", "123", "P1", "M"));
                verifyInequality(base, new Discharge.PatientInfo("N", "A", later, "M", "C", "123", "P1", "M"));
                verifyInequality(base, new Discharge.PatientInfo("N", "A", now, "F", "C", "123", "P1", "M"));
                verifyInequality(base, new Discharge.PatientInfo("N", "A", now, "M", "X", "123", "P1", "M"));
                verifyInequality(base, new Discharge.PatientInfo("N", "A", now, "M", "C", "321", "P1", "M"));
                verifyInequality(base, new Discharge.PatientInfo("N", "A", now, "M", "C", "123", "P2", "M"));
                verifyInequality(base, new Discharge.PatientInfo("N", "A", now, "M", "C", "123", "P1", "X"));
        }

        @Test
        void shouldVerifyEqualsHashCodeAndToStringForAmbulatoryProfile() {
                Discharge.AmbulatoryProfile base = new Discharge.AmbulatoryProfile(true, "C", "D");
                Discharge.AmbulatoryProfile same = new Discharge.AmbulatoryProfile(true, "C", "D");
                verifyEquality(base, same);

                verifyInequality(base, new Discharge.AmbulatoryProfile(false, "C", "D"));
                verifyInequality(base, new Discharge.AmbulatoryProfile(true, "X", "D"));
                verifyInequality(base, new Discharge.AmbulatoryProfile(true, "C", "X"));
        }

        @Test
        void shouldVerifyEqualsHashCodeAndToStringForMedicationEntry() {
                Discharge.MedicationEntry base = new Discharge.MedicationEntry("N", "D", "Q");
                Discharge.MedicationEntry same = new Discharge.MedicationEntry("N", "D", "Q");
                verifyEquality(base, same);

                verifyInequality(base, new Discharge.MedicationEntry("X", "D", "Q"));
                verifyInequality(base, new Discharge.MedicationEntry("N", "X", "Q"));
                verifyInequality(base, new Discharge.MedicationEntry("N", "D", "X"));
        }

        @Test
        void shouldVerifyEqualsHashCodeAndToStringForDischarge() {
                Discharge.ServiceInfo serviceInfo = new Discharge.ServiceInfo();
                Discharge.PatientInfo patientInfo = new Discharge.PatientInfo();
                Discharge.AmbulatoryProfile profile = new Discharge.AmbulatoryProfile();
                Discharge.MedicationEntry med = new Discharge.MedicationEntry();

                Discharge base = Discharge.builder()
                                .aghuDischargeId(1L)
                                .serviceInfo(serviceInfo)
                                .patientInfo(patientInfo)
                                .ambulatoryProfile(profile)
                                .clinicalSummary("CS")
                                .problemList(Collections.singletonList("P"))
                                .medications(Collections.singletonList(med))
                                .therapeuticGuidance("TG")
                                .specializedReturnGuidance("SRG")
                                .additionalInformation("AI")
                                .followUpSchedulingSuggestion("FUSS")
                                .responsiblePhysician("RP")
                                .responsiblePhysicianRegistration("RPR")
                                .reportDate(now)
                                .build();

                Discharge same = Discharge.builder()
                                .aghuDischargeId(1L)
                                .serviceInfo(serviceInfo)
                                .patientInfo(patientInfo)
                                .ambulatoryProfile(profile)
                                .clinicalSummary("CS")
                                .problemList(Collections.singletonList("P"))
                                .medications(Collections.singletonList(med))
                                .therapeuticGuidance("TG")
                                .specializedReturnGuidance("SRG")
                                .additionalInformation("AI")
                                .followUpSchedulingSuggestion("FUSS")
                                .responsiblePhysician("RP")
                                .responsiblePhysicianRegistration("RPR")
                                .reportDate(now)
                                .build();

                verifyEquality(base, same);

                verifyInequality(base, Discharge.builder()
                                .aghuDischargeId(2L)
                                .serviceInfo(serviceInfo)
                                .patientInfo(patientInfo)
                                .ambulatoryProfile(profile)
                                .clinicalSummary("CS")
                                .problemList(Collections.singletonList("P"))
                                .medications(Collections.singletonList(med))
                                .therapeuticGuidance("TG")
                                .specializedReturnGuidance("SRG")
                                .additionalInformation("AI")
                                .followUpSchedulingSuggestion("FUSS")
                                .responsiblePhysician("RP")
                                .responsiblePhysicianRegistration("RPR")
                                .reportDate(now)
                                .build());

                verifyInequality(base, Discharge.builder()
                                .aghuDischargeId(1L)
                                .serviceInfo(null)
                                .patientInfo(patientInfo)
                                .ambulatoryProfile(profile)
                                .clinicalSummary("CS")
                                .problemList(Collections.singletonList("P"))
                                .medications(Collections.singletonList(med))
                                .therapeuticGuidance("TG")
                                .specializedReturnGuidance("SRG")
                                .additionalInformation("AI")
                                .followUpSchedulingSuggestion("FUSS")
                                .responsiblePhysician("RP")
                                .responsiblePhysicianRegistration("RPR")
                                .reportDate(now)
                                .build());

                verifyInequality(base, Discharge.builder()
                                .aghuDischargeId(1L)
                                .serviceInfo(serviceInfo)
                                .patientInfo(patientInfo)
                                .ambulatoryProfile(null)
                                .clinicalSummary("CS")
                                .problemList(Collections.singletonList("P"))
                                .medications(Collections.singletonList(med))
                                .therapeuticGuidance("TG")
                                .specializedReturnGuidance("SRG")
                                .additionalInformation("AI")
                                .followUpSchedulingSuggestion("FUSS")
                                .responsiblePhysician("RP")
                                .responsiblePhysicianRegistration("RPR")
                                .reportDate(now)
                                .build());
        }

        @Test
        void shouldSupportEqualsWithAllNullFields() {

                Discharge r1 = new Discharge();
                Discharge r2 = new Discharge();
                verifyEquality(r1, r2);

                Discharge.ServiceInfo s1 = new Discharge.ServiceInfo();
                Discharge.ServiceInfo s2 = new Discharge.ServiceInfo();
                verifyEquality(s1, s2);

                Discharge.PatientInfo p1 = new Discharge.PatientInfo();
                Discharge.PatientInfo p2 = new Discharge.PatientInfo();
                verifyEquality(p1, p2);

                Discharge.AmbulatoryProfile a1 = new Discharge.AmbulatoryProfile();
                Discharge.AmbulatoryProfile a2 = new Discharge.AmbulatoryProfile();
                verifyEquality(a1, a2);

                Discharge.MedicationEntry m1 = new Discharge.MedicationEntry();
                Discharge.MedicationEntry m2 = new Discharge.MedicationEntry();
                verifyEquality(m1, m2);
        }

        @Test
        void shouldCoverBuilderToString() {
                assertThat(Discharge.builder().aghuDischargeId(400L).toString()).isNotBlank();
                assertThat(Discharge.builder().toString()).isNotBlank();
                assertThat(Discharge.ServiceInfo.builder().toString()).isNotBlank();
                assertThat(Discharge.PatientInfo.builder().toString()).isNotBlank();
                assertThat(Discharge.AmbulatoryProfile.builder().toString()).isNotBlank();
                assertThat(Discharge.MedicationEntry.builder().toString()).isNotBlank();
        }

        @Test
        void shouldCoverCanEqual() {
                // Discharge
                Discharge r1 = new Discharge();
                Discharge r2 = new Discharge() {
                        @Override
                        public boolean canEqual(Object other) {
                                return false;
                        }
                };
                assertThat(r1.equals(r2)).isFalse();

                // ServiceInfo
                Discharge.ServiceInfo s1 = new Discharge.ServiceInfo();
                Discharge.ServiceInfo s2 = new Discharge.ServiceInfo() {
                        @Override
                        public boolean canEqual(Object other) {
                                return false;
                        }
                };
                assertThat(s1.equals(s2)).isFalse();

                // PatientInfo
                Discharge.PatientInfo p1 = new Discharge.PatientInfo();
                Discharge.PatientInfo p2 = new Discharge.PatientInfo() {
                        @Override
                        public boolean canEqual(Object other) {
                                return false;
                        }
                };
                assertThat(p1.equals(p2)).isFalse();

                // AmbulatoryProfile
                Discharge.AmbulatoryProfile a1 = new Discharge.AmbulatoryProfile();
                Discharge.AmbulatoryProfile a2 = new Discharge.AmbulatoryProfile() {
                        @Override
                        public boolean canEqual(Object other) {
                                return false;
                        }
                };
                assertThat(a1.equals(a2)).isFalse();

                // MedicationEntry
                Discharge.MedicationEntry m1 = new Discharge.MedicationEntry();
                Discharge.MedicationEntry m2 = new Discharge.MedicationEntry() {
                        @Override
                        public boolean canEqual(Object other) {
                                return false;
                        }
                };
                assertThat(m1.equals(m2)).isFalse();
        }

        @Test
        void shouldCoverBuilderDefaultExplicitNull() {
                Discharge report = Discharge.builder()
                                .aghuDischargeId(600L)
                                .problemList(null)
                                .medications(null)
                                .build();

                assertThat(report.getProblemList()).isNull();
                assertThat(report.getMedications()).isNull();
        }

        @Test
        void shouldSerializeOmittingNullFields() throws Exception {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());

                Discharge report = Discharge.builder()
                                .aghuDischargeId(700L)
                                .clinicalSummary("Resumo")
                                .reportDate(now)
                                .build();

                String json = mapper.writeValueAsString(report);

                assertThat(json).contains("Resumo")
                                .doesNotContain("responsiblePhysician")
                                .doesNotContain("additionalInformation");
        }

        private <T> void verifyEquality(T obj1, T obj2) {
                assertThat(obj1).isEqualTo(obj1);
                assertThat(obj1).isEqualTo(obj2);
                assertThat(obj2).isEqualTo(obj1);
                assertThat(obj1).hasSameHashCodeAs(obj2);
                assertThat(obj1.toString()).isNotBlank();
        }

        private <T> void verifyInequality(T obj1, T obj2) {
                assertThat(obj1).isNotEqualTo(obj2);
                assertThat(obj2).isNotEqualTo(obj1);
                assertThat(obj1).isNotEqualTo(null);
                assertThat(obj1).isNotEqualTo(new Object());
        }
}
