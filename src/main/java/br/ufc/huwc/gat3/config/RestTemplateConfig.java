package br.ufc.huwc.gat3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Classe de configuração para beans de infraestrutura HTTP.
 * Define o RestTemplate como um Bean injetável para uso no serviço de produção.
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
