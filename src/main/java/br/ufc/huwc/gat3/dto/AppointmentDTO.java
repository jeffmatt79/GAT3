package br.ufc.huwc.gat3.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDTO {
    private Long seqAtendimento;
    private LocalDateTime alteradoEm;
    private String nomePaciente;
    private Long prontuario;
    private String especialidade;
    private Boolean altaAmbulatorial;
    private String status;
    private Boolean pendenciaPreenchimento;
    private String medicoResponsavel;
}
