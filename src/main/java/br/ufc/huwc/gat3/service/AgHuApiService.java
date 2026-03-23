package br.ufc.huwc.gat3.service;

import br.ufc.huwc.gat3.dto.AppointmentDTO;
import br.ufc.huwc.gat3.dto.DischargeDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Serviço responsável pela comunicação direta com a API externa do sistema AGHU.
 * <p>
 * Realiza o consumo de endpoints REST utilizando {@link RestTemplate}, gerenciando
 * a montagem dinâmica de URLs, passagem de parâmetros de consulta (Query Params)
 * e autenticação via Token Bearer.
 * </p>
 */
@Service
@Slf4j
public class AgHuApiService {
    private final RestTemplate restTemplate;

    @Value("${aghu.api.base-url}")
    private String aghuBaseUrl;

    @Value("${aghu.api.token}")
    private String apiToken;

    /**
     * Construtor da classe com injeção de dependência do RestTemplate.
     * @param restTemplate Instância configurada para chamadas HTTP.
     */
    public AgHuApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Recupera a lista de atendimentos ambulatoriais do AGHU dentro de um período específico.
     * <p>
     * Este método constrói a URL de consulta incluindo as datas de início e fim, além da unidade hospitalar.
     * A requisição é autenticada automaticamente no cabeçalho HTTP.
     * </p>
     *
     * @param startDate Data de início do período para busca dos atendimentos.
     * @param endDate   Data de término do período para busca dos atendimentos.
     * @param unit      Identificador da unidade ou filial (ex: "meac").
     * @return Uma {@link List} de {@link AppointmentDTO} com os dados retornados pela API.
     * Retorna uma lista vazia em caso de erro de conexão ou resposta inválida.
     */
    public List<AppointmentDTO> fetchAppointmentsRange(LocalDate startDate, LocalDate endDate, String unit) {
        String url = UriComponentsBuilder.fromUriString(aghuBaseUrl)
                .path("/aghu_chufc_api/ficha-ambulatorial/lista")
                .queryParam("dataInicial", startDate.toString())
                .queryParam("dataFinal", endDate.toString())
                .queryParam("filial", unit)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        log.info("API: Buscando intervalo de atendimentos de {} ate {}", startDate, endDate);

        try {
            ResponseEntity<List<AppointmentDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<List<AppointmentDTO>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("API: Sucesso! Encontrados {} registros.", response.getBody().size());
                return response.getBody();
            }

            log.warn("API: Status de resposta inesperado: {}", response.getStatusCode());
            return Collections.emptyList();

        } catch (HttpStatusCodeException exception) {

            log.error("Erro HTTP ao consumir API AGHU.");
            log.error("Status: {}", exception.getStatusCode());
            log.error("Resposta erro: {}", exception.getResponseBodyAsString());

        } catch (Exception exception) {

            log.error("Erro inesperado ao consumir API AGHU.", exception);

        }
        return Collections.emptyList();
    }

    /**
     * Recupera os dados detalhados para o processo de Alta (Discharge) de um atendimento.
     *
     * @param seq  Identificador sequencial do atendimento no AGHU.
     * @param unit Identificador da unidade ou filial (ex: "meac", "huwc").
     * @return Um {@link DischargeDTO} preenchido ou {@code null} em caso de erro.
     */
    public DischargeDTO fetchDischargeData(Long seq, String unit) {
        String url = UriComponentsBuilder.fromUriString(aghuBaseUrl)
                .path("/aghu_chufc_api/ficha-ambulatorial/{seq}")
                .queryParam("filial", unit)
                .buildAndExpand(seq)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        log.info("API: Buscando dados de discharge para o atendimento: {} na filial: {}", seq, unit);

        try {
            ResponseEntity<DischargeDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    DischargeDTO.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("API: Sucesso! Ficha do atendimento {} na filial {} recuperada.", seq, unit);
                return response.getBody();
            }

            log.warn("API: Status inesperado para discharge {} na filial {}: {}", seq, unit, response.getStatusCode());
            return null;

        } catch (HttpStatusCodeException exception) {
            log.error("Erro HTTP ao buscar discharge no AGHU. Status: {} | Filial: {}", exception.getStatusCode(), unit);
            log.error("Resposta de erro: {}", exception.getResponseBodyAsString());
        } catch (Exception exception) {
            log.error("Erro inesperado ao buscar discharge no AGHU para o seq: {} na filial: {}", seq, unit, exception);
        }
        return null;
    }
}