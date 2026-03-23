package br.ufc.huwc.gat3.repositories;

import br.ufc.huwc.gat3.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

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

       @Query("SELECT COUNT(a) as total, " +
                     "SUM(CASE WHEN (a.altaAmbulatorial = true OR d IS NOT NULL) AND LOWER(COALESCE(d.status, a.status, 'pendente')) IN ('pendente', 'pendente-laranja') THEN 1 ELSE 0 END) as pendentes, " +
                     "SUM(CASE WHEN LOWER(COALESCE(d.status, a.status)) = 'salva' THEN 1 ELSE 0 END) as salvas, " +
                     "SUM(CASE WHEN LOWER(COALESCE(d.status, a.status)) = 'enviada' THEN 1 ELSE 0 END) as enviadas " +
                     "FROM Appointment a LEFT JOIN a.discharge d " +
                     "WHERE (:hasSetores = false OR LOWER(a.especialidade) IN :setores)")
       DashboardStatsProjection getDashboardStats(@Param("setores") java.util.List<String> setores,
                     @Param("hasSetores") boolean hasSetores);

       @Query("SELECT EXTRACT(DAY FROM a.alteradoEm) as dia, COUNT(a) as quantidade " +
                     "FROM Appointment a LEFT JOIN a.discharge d " +
                     "WHERE (:hasSetores = false OR LOWER(a.especialidade) IN :setores) AND " +
                     "EXTRACT(MONTH FROM a.alteradoEm) = :mes AND " +
                     "EXTRACT(YEAR FROM a.alteradoEm) = :ano " +
                     "GROUP BY EXTRACT(DAY FROM a.alteradoEm)")
       java.util.List<DailyStatsProjection> getDailyStats(@Param("mes") int mes, @Param("ano") int ano,
                     @Param("setores") java.util.List<String> setores, @Param("hasSetores") boolean hasSetores);

       Optional<Appointment> findBySeqAtendimento(Long seqAtendimento);

       @Query("SELECT a FROM Appointment a LEFT JOIN a.discharge d WHERE " +
                     "(:status IS NULL OR :status = 'todos' OR " +
                     " (:status = 'pendente' AND (a.altaAmbulatorial = true OR d IS NOT NULL)) OR " +
                     " (:status = 'pendente-cinza' AND a.altaAmbulatorial = false) OR " +
                     " (:status <> 'pendente' AND :status <> 'pendente-cinza' AND d IS NOT NULL)) AND " +
                     "(:status IS NULL OR :status = 'todos' OR " +
                     " (:status = 'pendente' AND LOWER(COALESCE(d.status, a.status, 'pendente')) IN ('pendente', 'pendente-laranja')) OR " +
                     " (:status = 'pendente-cinza' AND a.altaAmbulatorial = false AND (d IS NULL OR LOWER(d.status) NOT IN ('enviada', 'salva', 'rascunho'))) OR " +
                     " (:status <> 'pendente' AND :status <> 'pendente-cinza' AND LOWER(COALESCE(d.status, a.status)) = LOWER(:status))) AND " +
                     "(cast(:dataInicio as date) IS NULL OR a.alteradoEm >= :dataInicio) AND " +
                     "(cast(:dataFim as date) IS NULL OR a.alteradoEm <= :dataFim) AND " +
                     "(:busca IS NULL OR LOWER(a.nomePaciente) LIKE LOWER(:busca) OR CAST(a.seqAtendimento AS text) LIKE :busca) AND " +
                     "(:hasSetores = false OR LOWER(a.especialidade) IN :setores)")
       Page<Appointment> findFilteredAppointments(
                     @Param("status") String status,
                     @Param("busca") String busca,
                     @Param("dataInicio") java.time.LocalDate dataInicio,
                     @Param("dataFim") java.time.LocalDate dataFim,
                     @Param("setores") java.util.List<String> setores,
                     @Param("hasSetores") boolean hasSetores,
                     Pageable pageable);
}