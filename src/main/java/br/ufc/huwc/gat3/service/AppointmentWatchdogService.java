package br.ufc.huwc.gat3.service;

import br.ufc.huwc.gat3.dto.AppointmentDTO;
import br.ufc.huwc.gat3.model.Appointment;
import br.ufc.huwc.gat3.model.Unit;
import br.ufc.huwc.gat3.repositories.AppointmentRepository;
import br.ufc.huwc.gat3.repositories.UnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Serviço Watchdog responsável por orquestrar a busca de novos atendimentos.
 * <p>
 * Este serviço atua como um monitor que periodicamente consome a API externa
 * para identificar novos atendimentos ou modificações em registros existentes.
 * A lógica garante a integridade dos dados locais sem duplicar informações.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentWatchdogService {

    private final AgHuApiService agHuApiService;
    private final AppointmentRepository appointmentListRepository;
    private final UnitRepository unitRepository;
    private final AppConfigService appConfigService;

    /**
     * Inicia o processamento de sincronização de atendimentos.
     * <p>
     * O fluxo segue os seguintes passos:
     * <ol>
     * <li>Recupera todas as filiais ativas no banco de dados.</li>
     * <li>Para cada filial, consulta a API externa no intervalo definido.</li>
     * <li>Itera sobre a lista recebida para decidir entre inserção ou atualização.</li>
     * <li>Atualiza o marcador de última execução em caso de sucesso total.</li>
     * </ol>
     * </p>
     */
    public void checkForNewDischarges() {
        log.info("WATCHDOG: Iniciando ciclo de sincronização...");

        List<Unit> activeUnits = unitRepository.findByActiveTrue();

        if (CollectionUtils.isEmpty(activeUnits)) {
            log.warn("WATCHDOG: Nenhuma filial ativa encontrada no banco. Abortando ciclo.");
            return;
        }

        LocalDate startDate = LocalDate.of(2026, 2, 1);
        LocalDate endDate = LocalDate.now();

        // Array para manter contadores entre chamadas de métodos [0]=processados, [1]=atualizados
        int[] globalCounts = {0, 0};

        for (Unit unit : activeUnits) {
            processUnitSync(unit, startDate, endDate, globalCounts);
        }

        appConfigService.updateLastSuccessfulCheckTime(LocalDateTime.now());

        log.info("WATCHDOG: Ciclo concluído com sucesso. Novos: {} | Atualizados: {}", globalCounts[0], globalCounts[1]);
    }

    /**
     * Realiza a sincronização de uma unidade específica.
     *
     * @param unit      A entidade da filial.
     * @param startDate Data inicial da busca.
     * @param endDate   Data final da busca.
     * @param counts    Array de contadores para persistência de estado.
     */
    private void processUnitSync(Unit unit, LocalDate startDate, LocalDate endDate, int[] counts) {
        String currentUnitName = unit.getName();
        log.info("WATCHDOG: Verificando range de {} ate {} para a unidade {}", startDate, endDate, currentUnitName);

        List<AppointmentDTO> apiList = agHuApiService.fetchAppointmentsRange(startDate, endDate, currentUnitName);

        if (CollectionUtils.isEmpty(apiList)) {
            log.info("WATCHDOG: Nenhum dado recebido (lista vazia ou erro de conexão). Ciclo encerrado.");
            return;
        }

        for (AppointmentDTO dto : apiList) {
            syncSingleAppointment(dto, unit, counts);
        }
    }

    /**
     * Orquestra a sincronização de um único atendimento (save ou update).
     *
     * @param dto    DTO vindo da API.
     * @param unit   Unidade associada.
     * @param counts Array de contadores.
     */
    private void syncSingleAppointment(AppointmentDTO dto, Unit unit, int[] counts) {
        Optional<Appointment> existingRecord = appointmentListRepository
                .findBySeqAtendimento(dto.getSeqAtendimento());

        if (!existingRecord.isPresent()) {
            saveNewAppointmentInList(dto, unit);
            counts[0]++;
        } else {
            Appointment entity = existingRecord.get();
            if (shouldUpdate(entity, dto)) {
                updateExistingAppointmentInList(entity, dto, unit);
                counts[1]++;
            }
        }
    }

    /**
     * Valida se o atendimento existente deve ser atualizado com base na data de alteração.
     *
     * @param entity Registro local atual.
     * @param dto    Dados novos da API.
     * @return true se a data da API for posterior à local.
     */
    private boolean shouldUpdate(Appointment entity, AppointmentDTO dto) {
        return dto.getAlteradoEm() != null &&
                (entity.getAlteradoEm() == null ||
                        dto.getAlteradoEm().isAfter(entity.getAlteradoEm()));
    }

    /**
     * Persiste um novo atendimento na base de dados local vinculando a unidade.
     *
     * @param dto  Objeto de transferência contendo os dados vindos do AGHU.
     * @param unit A entidade da filial vinda do banco de dados.
     */
    private void saveNewAppointmentInList(AppointmentDTO dto, Unit unit) {
        Appointment newList = Appointment.builder()
                .seqAtendimento(dto.getSeqAtendimento())
                .alteradoEm(dto.getAlteradoEm())
                .nomePaciente(dto.getNomePaciente())
                .prontuario(dto.getProntuario())
                .especialidade(dto.getEspecialidade())
                .pendenciaPreenchimento(dto.getPendenciaPreenchimento())
                .altaAmbulatorial(dto.getAltaAmbulatorial())
                .status(dto.getStatus())
                .unit(unit)
                .build();

        appointmentListRepository.save(newList);
        log.debug("WATCHDOG: Atendimento {} inserido na lista.", dto.getSeqAtendimento());
    }

    /**
     * Atualiza os campos de uma entidade existente com novos dados da API.
     *
     * @param entity A entidade JPA carregada do banco.
     * @param dto    Os novos dados para atualização.
     * @param unit   A entidade da filial para garantir o vínculo correto.
     */
    private void updateExistingAppointmentInList(Appointment entity, AppointmentDTO dto, Unit unit) {
        entity.setAlteradoEm(dto.getAlteradoEm());
        entity.setNomePaciente(dto.getNomePaciente());
        entity.setProntuario(dto.getProntuario());
        entity.setEspecialidade(dto.getEspecialidade());
        entity.setPendenciaPreenchimento(dto.getPendenciaPreenchimento());
        entity.setAltaAmbulatorial(dto.getAltaAmbulatorial());
        entity.setStatus(dto.getStatus());
        entity.setUnit(unit);

        appointmentListRepository.save(entity);
        log.info("WATCHDOG: Atendimento {} atualizado (mudança detectada).", dto.getSeqAtendimento());
    }
}