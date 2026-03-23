package br.ufc.huwc.gat3.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DischargeComplementDTO {
    private AmbulatoryProfileUpdate ambulatoryProfile;
    private String therapeuticGuidance;
    private String followUpSchedulingSuggestion;
    private String status;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AmbulatoryProfileUpdate {
        private Boolean adequate;
        private String dischargeReason;
    }
}
