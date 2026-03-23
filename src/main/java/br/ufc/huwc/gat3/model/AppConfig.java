package br.ufc.huwc.gat3.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Entidade de configuração global do sistema.
 * Esta classe é utilizada para persistir variáveis de controle no banco de dados,
 * permitindo que parâmetros como o horário da última verificação bem-sucedida e
 * o intervalo entre execuções do Watchdog sejam mantidos entre reinicializações.
 */
@Entity
@Table(name = "app_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppConfig {

    @Id
    private String configKey;
    private String configValue;
}