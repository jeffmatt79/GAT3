package br.ufc.huwc.gat3.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardStatsDtoTest {

    @Test
    void shouldSupportBuilderAndGetters() {
        DashboardStatsDto dto = DashboardStatsDto.builder()
                .total(100L)
                .pendentes(40L)
                .salvas(35L)
                .enviadas(25L)
                .build();

        assertThat(dto)
                .extracting(
                        DashboardStatsDto::getTotal,
                        DashboardStatsDto::getPendentes,
                        DashboardStatsDto::getSalvas,
                        DashboardStatsDto::getEnviadas)
                .containsExactly(100L, 40L, 35L, 25L);
    }

    @Test
    void shouldSupportSettersEqualsHashCodeAndToString() {
        DashboardStatsDto dto1 = new DashboardStatsDto();
        dto1.setTotal(12L);
        dto1.setPendentes(5L);
        dto1.setSalvas(4L);
        dto1.setEnviadas(3L);

        DashboardStatsDto dto2 = new DashboardStatsDto(12L, 5L, 4L, 3L);

        assertThat(dto1)
                .isEqualTo(dto2)
                .hasSameHashCodeAs(dto2);
        assertThat(dto1.toString()).contains("total=12", "pendentes=5", "salvas=4", "enviadas=3");
    }
}
