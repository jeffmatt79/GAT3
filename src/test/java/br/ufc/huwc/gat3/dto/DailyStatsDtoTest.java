package br.ufc.huwc.gat3.dto;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DailyStatsDtoTest {

    @Test
    void shouldSupportBuilderAndGetters() {
        Map<Integer, Long> dados = new HashMap<>();
        dados.put(1, 10L);
        dados.put(2, 20L);

        DailyStatsDto dto = DailyStatsDto.builder()
                .mes(3)
                .ano(2026)
                .dados(dados)
                .build();

        assertThat(dto)
                .extracting(
                        DailyStatsDto::getMes,
                        DailyStatsDto::getAno,
                        DailyStatsDto::getDados)
                .containsExactly(3, 2026, dados);
    }

    @Test
    void shouldSupportSettersEqualsHashCodeAndToString() {
        Map<Integer, Long> dados = new HashMap<>();
        dados.put(10, 5L);

        DailyStatsDto dto1 = new DailyStatsDto();
        dto1.setMes(10);
        dto1.setAno(2025);
        dto1.setDados(dados);

        DailyStatsDto dto2 = new DailyStatsDto(10, 2025, dados);

        assertThat(dto1)
                .isEqualTo(dto2)
                .hasSameHashCodeAs(dto2);
        assertThat(dto1.toString()).contains("mes=10", "ano=2025", "dados={10=5}");
    }
}
