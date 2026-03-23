package br.ufc.huwc.gat3.config;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Classe de configuração para a biblioteca Flyway.
 * Esta classe garante que o versionamento do banco de dados seja verificado e
 * atualizado automaticamente durante a inicialização da aplicação Spring Boot.
 */

@Configuration
public class FlywayConfiguration {

    /**
     * Cria e configura uma instância do Flyway como um Bean do Spring.
     * Este método é responsável por carregar os scripts de migração da pasta
     * 'db/migration' e executá-los no banco de dados configurado.
     * @param dataSource O objeto de conexão com o banco de dados injetado pelo Spring.
     * @return Uma instância configurada do {@link Flyway} após a execução bem-sucedida das migrações.
     */
    @Bean
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure().dataSource(dataSource).load();
        flyway.migrate();
        return flyway;
    }
}