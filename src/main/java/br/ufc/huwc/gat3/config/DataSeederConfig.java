package br.ufc.huwc.gat3.config;

import br.ufc.huwc.gat3.service.AppointmentWatchdogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeederConfig {

    private final AppointmentWatchdogService watchdogService;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            log.info("SEEDER: Verificando e importando dados iniciais da API...");
           try {
                watchdogService.checkForNewDischarges();
                log.info("SEEDER: Dados importados com sucesso na inicialização.");
            } catch (Exception e) {
                log.error("SEEDER: Erro ao tentar importar dados na inicialização: {}", e.getMessage());
            }
        };
    }
}
