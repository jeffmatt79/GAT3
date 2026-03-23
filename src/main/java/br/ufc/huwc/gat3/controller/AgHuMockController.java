package br.ufc.huwc.gat3.controller;

import br.ufc.huwc.gat3.dto.AppointmentDTO;
import br.ufc.huwc.gat3.dto.DischargeDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Controller responsável por simular (mockar) o servidor do AGHU.
 * <p>
 * Este componente disponibiliza endpoints que imitam o comportamento da API
 * real do hospital,
 * permitindo o desenvolvimento e testes do robô de sincronização (Watchdog) sem
 * a necessidade
 * de uma conexão ativa com o ambiente de produção do AGHU.
 * </p>
 */
@RestController
@RequestMapping("/aghu_chufc_api/ficha-ambulatorial")
@Profile("dev")
public class AgHuMockController {

    private static final Logger log = LoggerFactory.getLogger(AgHuMockController.class);

    private final ObjectMapper objectMapper;

    private static final String LISTA_MOCK_FILE = "listaAtendimentos01052026.json";
    private static final String DETALHE_MOCK_FILE = "detalheAtendimentoMock.json";

    /**
     * Construtor da classe com injeção de dependência.
     * 
     * @param objectMapper instância do Jackson para manipulação de JSON.
     */
    public AgHuMockController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Recupera uma lista simulada de atendimentos ambulatoriais.
     * <p>
     * O método lê um arquivo estático do diretório de resources e o converte para
     * uma lista
     * de {@link AppointmentDTO}. Este endpoint é consumido periodicamente pelo
     * serviço Watchdog.
     * </p>
     * 
     * @param dataInicial Data de início do filtro (formato YYYY-MM-DD).
     * @param dataFinal   Data de fim do filtro (formato YYYY-MM-DD).
     * @param filial      Código da unidade hospitalar (ex: "meac").
     * @return Uma {@link List} contendo os atendimentos encontrados no arquivo
     *         mock,
     *         ou uma lista vazia em caso de erro na leitura do arquivo.
     * @see br.ufc.huwc.gat3.service.AgHuApiService
     */
    @GetMapping("/lista")
    public List<AppointmentDTO> getMockAppointments(
            @RequestParam(name = "dataInicial") String dataInicial,
            @RequestParam(name = "dataFinal") String dataFinal,
            @RequestParam(name = "filial") String filial) {

        log.info("MOCK API: Consultando lista - Filial: {}, Data: {} até {}", filial, dataInicial, dataFinal);

        try {
            ClassPathResource resource = new ClassPathResource(LISTA_MOCK_FILE);

            try (InputStream inputStream = resource.getInputStream()) {
                return objectMapper.readValue(inputStream, new TypeReference<List<AppointmentDTO>>() {
                });
            }

        } catch (Exception e) {
            log.error("Erro ao ler o arquivo de lista mock: {}", LISTA_MOCK_FILE, e);
            return Collections.emptyList();
        }
    }

    @GetMapping("/{seq}")
    public ResponseEntity<DischargeDTO> getMockDischargeBySeq(
            @PathVariable(name = "seq") Long seq,
            @RequestParam(name = "filial") String filial) {

        log.info("MOCK API: Consultando detalhe - Seq: {}, Filial: {}", seq, filial);
        try {
            ClassPathResource resource = new ClassPathResource(DETALHE_MOCK_FILE);
            try (InputStream inputStream = resource.getInputStream()) {
                Map<String, DischargeDTO> detalhes = objectMapper.readValue(
                        inputStream,
                        new TypeReference<Map<String, DischargeDTO>>() {
                        });

                DischargeDTO dto = detalhes.get(seq.toString());
                if (dto != null) {
                    log.info("MOCK API: Registro encontrado para seq: {}", seq);
                    return ResponseEntity.ok(dto);
                } else {
                    log.warn("MOCK API: Registro NAO encontrado no JSON para seq: {}", seq);
                }
            }
        } catch (Exception e) {
            log.error("Erro ao ler o arquivo de detalhe mock: {}", DETALHE_MOCK_FILE, e);
        }

        log.info("MOCK API: Utilizando fallback genérico para seq: {}", seq);
        // Fallback para evitar 404 se não estiver no JSON
        DischargeDTO genericDto = new DischargeDTO();
        genericDto.setNumero(seq);

        DischargeDTO.PatientDTO patient = new DischargeDTO.PatientDTO();
        patient.setNome("PACIENTE TESTE (MOCK FALLBACK)");
        patient.setProntuario(seq * 10);

        DischargeDTO.AddressDTO address = new DischargeDTO.AddressDTO();
        address.setLogradouro("RUA MOCK FALLBACK");

        DischargeDTO.CityDTO city = new DischargeDTO.CityDTO();
        city.setNome("FORTALEZA");
        address.setCidade(city);

        DischargeDTO.UfDTO uf = new DischargeDTO.UfDTO();
        uf.setSigla("CE");
        address.setUf(uf);

        patient.setEnderecos(java.util.Collections.singletonList(address));
        genericDto.setPaciente(patient);

        return ResponseEntity.ok(genericDto);
    }
}
