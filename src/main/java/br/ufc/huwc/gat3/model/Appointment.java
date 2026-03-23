package br.ufc.huwc.gat3.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidade JPA que representa a lista simplificada de atendimentos sincronizados do AGHU.
 * <p>
 * Esta classe é utilizada pelo {@code AppointmentWatchdogService} para rastrear mudanças
 * básicas nos atendimentos diários. Os campos refletem a estrutura da API do AGHU,
 * permitindo identificar novos registros ou atualizações através da data de modificação.
 * </p>
 */
@Entity
@Table(name = "appointments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    private Long seqAtendimento;
    private LocalDateTime alteradoEm;
    private String nomePaciente;
    private Long prontuario;
    private String especialidade;
    private Boolean pendenciaPreenchimento;
    private Boolean altaAmbulatorial;
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @Column(name = "last_synced_at")
    @UpdateTimestamp
    private LocalDateTime lastSyncedAt;

    @javax.persistence.OneToOne(fetch = javax.persistence.FetchType.LAZY)
    @javax.persistence.JoinColumn(name = "seq_atendimento", referencedColumnName = "aghu_discharge_id", insertable = false, updatable = false)
    private Discharge discharge;
}
