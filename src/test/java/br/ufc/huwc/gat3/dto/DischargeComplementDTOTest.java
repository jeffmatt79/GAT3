package br.ufc.huwc.gat3.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DischargeComplementDTOTest {

    @Test
    void shouldSupportNestedAmbulatoryProfileAndBuilder() {
        DischargeComplementDTO.AmbulatoryProfileUpdate profile =
                new DischargeComplementDTO.AmbulatoryProfileUpdate(Boolean.TRUE, "Alta por melhora");

        DischargeComplementDTO dto = DischargeComplementDTO.builder()
                .ambulatoryProfile(profile)
                .therapeuticGuidance("Manter analgesico")
                .followUpSchedulingSuggestion("Retorno em 30 dias")
                .status("salva")
                .build();

        assertThat(dto.getAmbulatoryProfile())
                .extracting(
                        DischargeComplementDTO.AmbulatoryProfileUpdate::getAdequate,
                        DischargeComplementDTO.AmbulatoryProfileUpdate::getDischargeReason)
                .containsExactly(Boolean.TRUE, "Alta por melhora");
        assertThat(dto)
                .extracting(
                        DischargeComplementDTO::getTherapeuticGuidance,
                        DischargeComplementDTO::getFollowUpSchedulingSuggestion,
                        DischargeComplementDTO::getStatus)
                .containsExactly("Manter analgesico", "Retorno em 30 dias", "salva");
    }

    @Test
    void shouldSupportEqualsHashCodeAndSetters() {
        DischargeComplementDTO dto1 = new DischargeComplementDTO();
        DischargeComplementDTO.AmbulatoryProfileUpdate profile1 = new DischargeComplementDTO.AmbulatoryProfileUpdate();
        profile1.setAdequate(Boolean.FALSE);
        profile1.setDischargeReason("Transferencia");
        dto1.setAmbulatoryProfile(profile1);
        dto1.setTherapeuticGuidance("Orientacao");
        dto1.setFollowUpSchedulingSuggestion("Retorno");
        dto1.setStatus("enviada");

        DischargeComplementDTO dto2 = new DischargeComplementDTO(
                new DischargeComplementDTO.AmbulatoryProfileUpdate(Boolean.FALSE, "Transferencia"),
                "Orientacao",
                "Retorno",
                "enviada");

        assertThat(dto1)
                .isEqualTo(dto2)
                .hasSameHashCodeAs(dto2)
                .satisfies(value -> assertThat(value.toString()).contains("status=enviada", "therapeuticGuidance=Orientacao"));
        assertThat(profile1)
                .isEqualTo(new DischargeComplementDTO.AmbulatoryProfileUpdate(Boolean.FALSE, "Transferencia"))
                .hasSameHashCodeAs(new DischargeComplementDTO.AmbulatoryProfileUpdate(Boolean.FALSE, "Transferencia"))
                .satisfies(value -> assertThat(value.toString()).contains("adequate=false", "dischargeReason=Transferencia"));
    }

    @Test
    void shouldHandleEqualsSpecialCases() {
        DischargeComplementDTO dto = buildDto();
        DischargeComplementDTO.AmbulatoryProfileUpdate profile = buildProfile();

        assertThat(dto)
                .returns(true, value -> value.equals(dto))
                .returns(false, value -> value.equals(null))
                .returns(false, value -> value.equals("outro tipo"));

        assertThat(profile)
                .returns(true, value -> value.equals(profile))
                .returns(false, value -> value.equals(null))
                .returns(false, value -> value.equals("outro tipo"));
    }

    @Test
    void shouldSupportEqualsHashCodeWhenAllFieldsAreNull() {
        DischargeComplementDTO dto1 = new DischargeComplementDTO();
        DischargeComplementDTO dto2 = new DischargeComplementDTO();
        DischargeComplementDTO.AmbulatoryProfileUpdate profile1 = new DischargeComplementDTO.AmbulatoryProfileUpdate();
        DischargeComplementDTO.AmbulatoryProfileUpdate profile2 = new DischargeComplementDTO.AmbulatoryProfileUpdate();

        assertThat(dto1)
                .isEqualTo(dto2)
                .hasSameHashCodeAs(dto2)
                .satisfies(value -> assertThat(value.toString()).contains("ambulatoryProfile=null", "status=null"));

        assertThat(profile1)
                .isEqualTo(profile2)
                .hasSameHashCodeAs(profile2)
                .satisfies(value -> assertThat(value.toString()).contains("adequate=null", "dischargeReason=null"));
    }

    @Test
    void shouldDetectDifferencesInDtoFields() {
        DischargeComplementDTO base = buildDto();

        assertThat(base)
                .isNotEqualTo(new DischargeComplementDTO(
                        new DischargeComplementDTO.AmbulatoryProfileUpdate(Boolean.FALSE, "Outra"),
                        "Orientacao",
                        "Retorno",
                        "enviada"))
                .isNotEqualTo(new DischargeComplementDTO(
                        buildProfile(),
                        "Outra orientacao",
                        "Retorno",
                        "enviada"))
                .isNotEqualTo(new DischargeComplementDTO(
                        buildProfile(),
                        "Orientacao",
                        "Outro retorno",
                        "enviada"))
                .isNotEqualTo(new DischargeComplementDTO(
                        buildProfile(),
                        "Orientacao",
                        "Retorno",
                        "salva"));
    }

    @Test
    void shouldDetectDifferencesInAmbulatoryProfileFields() {
        DischargeComplementDTO.AmbulatoryProfileUpdate base = buildProfile();

        assertThat(base)
                .isNotEqualTo(new DischargeComplementDTO.AmbulatoryProfileUpdate(Boolean.FALSE, "Alta por melhora"))
                .isNotEqualTo(new DischargeComplementDTO.AmbulatoryProfileUpdate(Boolean.TRUE, "Transferencia"));
    }

    private DischargeComplementDTO buildDto() {
        return new DischargeComplementDTO(
                buildProfile(),
                "Orientacao",
                "Retorno",
                "enviada");
    }

    private DischargeComplementDTO.AmbulatoryProfileUpdate buildProfile() {
        return new DischargeComplementDTO.AmbulatoryProfileUpdate(Boolean.TRUE, "Alta por melhora");
    }
}
