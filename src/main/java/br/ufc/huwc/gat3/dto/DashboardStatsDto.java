package br.ufc.huwc.gat3.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private long total;
    private long pendentes;
    private long salvas;
    private long enviadas;
}
