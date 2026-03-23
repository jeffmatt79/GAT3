package br.ufc.huwc.gat3.config;

import br.ufc.huwc.gat3.service.AppConfigService;
import br.ufc.huwc.gat3.service.AppointmentWatchdogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.PeriodicTrigger;

/**
 * Configuração responsável por gerenciar o agendamento dinâmico do Watchdog.
 * Permite que a frequência de execução seja lida do banco de dados.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@DependsOn("flyway")
public class SchedulingConfig implements SchedulingConfigurer {

    private final AppConfigService appConfigService;
    private final AppointmentWatchdogService dischargeWatchdogService;

    /**
     * Configura o agendador de tarefas e registra a tarefa periódica com gatilho dinâmico.
     * @param taskRegistrar Objeto injetado pelo Spring responsável pelo registro e
     * gerenciamento do ciclo de vida das tarefas agendadas.
     */
    @Override
    public void configureTasks(@org.springframework.lang.NonNull ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.initialize();
        taskRegistrar.setScheduler(scheduler);

        Runnable runnableTask = dischargeWatchdogService::checkForNewDischarges;

        taskRegistrar.addTriggerTask(
                runnableTask,
                triggerContext -> {
                    long delayMs = appConfigService.getMonitoringPeriodMs();
                    return new PeriodicTrigger(delayMs).nextExecutionTime(triggerContext);
                });
    }
}