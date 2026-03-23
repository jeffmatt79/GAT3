package br.ufc.huwc.gat3.repositories;

import br.ufc.huwc.gat3.model.Discharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface DischargeRepository extends JpaRepository<Discharge, Long> {

        interface DashboardStatsProjection {
                Long getTotal();

                Long getPendentes();

                Long getSalvas();

                Long getEnviadas();
        }

        interface DailyStatsProjection {
                Integer getDia();

                Long getQuantidade();
        }

        Optional<Discharge> findByAghuDischargeIdAndPatientInfo_AghuRecord(Long seqAtendimento, String prontuario);

        @Query("SELECT COUNT(d) as total, " +
                        "SUM(CASE WHEN d.status IN ('pendente', 'pendente-cinza', 'pendente-laranja') THEN 1 ELSE 0 END) as pendentes, "
                        +
                        "SUM(CASE WHEN d.status = 'salva' THEN 1 ELSE 0 END) as salvas, " +
                        "SUM(CASE WHEN d.status = 'enviada' THEN 1 ELSE 0 END) as enviadas " +
                        "FROM Discharge d " +
                        "WHERE (:hasSetores = false OR LOWER(d.serviceInfo.subspecialtyOrServiceChief) IN :setores)")
        DashboardStatsProjection getDashboardStats(@Param("setores") List<String> setores,
                        @Param("hasSetores") boolean hasSetores);

        @Query("SELECT EXTRACT(DAY FROM d.reportDate) as dia, COUNT(d) as quantidade " +
                        "FROM Discharge d " +
                        "WHERE (:hasSetores = false OR LOWER(d.serviceInfo.subspecialtyOrServiceChief) IN :setores) " +
                        "  AND EXTRACT(MONTH FROM d.reportDate) = :mes " +
                        "  AND EXTRACT(YEAR FROM d.reportDate) = :ano " +
                        "GROUP BY EXTRACT(DAY FROM d.reportDate)")
        List<DailyStatsProjection> getDailyStats(@Param("mes") int mes, @Param("ano") int ano,
                        @Param("setores") List<String> setores, @Param("hasSetores") boolean hasSetores);

}
