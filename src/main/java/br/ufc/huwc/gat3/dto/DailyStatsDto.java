package br.ufc.huwc.gat3.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatsDto {
    private int mes;
    private int ano;
    private Map<Integer, Long> dados;
}
