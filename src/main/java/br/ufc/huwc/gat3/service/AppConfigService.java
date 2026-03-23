package br.ufc.huwc.gat3.service;

import br.ufc.huwc.gat3.model.AppConfig;
import br.ufc.huwc.gat3.repositories.AppConfigRepository;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Gerencia o acesso e atualização das configurações dinâmicas (Chave/Valor) no banco de dados.
 */
@Service
public class AppConfigService {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private final AppConfigRepository repository;
    
    public static final String MONITORING_KEY = "MONITORING_DISCHARGE_PERIOD_MS";
    public static final String LAST_CHECK_KEY = "LAST_SUCCESSFUL_CHECK_TIME";
    
    private static final long DEFAULT_PERIOD_MS = 300000;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME; 

    public AppConfigService(AppConfigRepository repository) {
        this.repository = repository;
    }

    /**
     * Busca o período de monitoramento no banco de dados para determinar a frequência do watchdog.
     */
    public long getMonitoringPeriodMs() {
        return repository.findById(MONITORING_KEY)
                .map(config -> {
                    try {
                        return Long.parseLong(config.getConfigValue());
                    } catch (NumberFormatException e) {
                        logger.log(Level.SEVERE, "Valor inválido no BD para {0}. Usando padrão.", MONITORING_KEY);
                        return DEFAULT_PERIOD_MS;
                    }
                })
                .orElse(DEFAULT_PERIOD_MS);
    }
    

    /**
     * Busca a última data/hora de verificação salva no banco de dados.
     * Isso garante que o serviço sempre inicie a busca de altas desde a última execução bem-sucedida.
     */
    public LocalDateTime getLastSuccessfulCheckTime() {
        Optional<AppConfig> config = repository.findById(LAST_CHECK_KEY);
        
        if (config.isPresent()) {
            try {
                return LocalDateTime.parse(config.get().getConfigValue(), FORMATTER);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Erro ao ler ou formatar LAST_CHECK_KEY. Usando data fallback.", e);
            }
        }
        return LocalDateTime.now().minusDays(30); 
    }
    
    /**
     * Salva a data e hora da verificação mais recente no banco de dados.
     */
    public void updateLastSuccessfulCheckTime(LocalDateTime newTime) {
        String timeString = newTime.format(FORMATTER);
        
        AppConfig config = repository.findById(LAST_CHECK_KEY).orElseGet(AppConfig::new);

        config.setConfigKey(LAST_CHECK_KEY);
        config.setConfigValue(timeString);
        
        repository.save(config);
    }
}