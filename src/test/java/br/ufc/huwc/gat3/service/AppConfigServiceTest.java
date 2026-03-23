package br.ufc.huwc.gat3.service;

import br.ufc.huwc.gat3.model.AppConfig;
import br.ufc.huwc.gat3.repositories.AppConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppConfigServiceTest {

    @Mock
    private AppConfigRepository repository;

    @InjectMocks
    private AppConfigService service;

    @Test
    void getMonitoringPeriodMs_ShouldReturnValidValue_WhenConfigExists() {
        AppConfig config = new AppConfig();
        config.setConfigValue("5000");
        when(repository.findById(AppConfigService.MONITORING_KEY)).thenReturn(Optional.of(config));

        long result = service.getMonitoringPeriodMs();

        assertEquals(5000L, result);
    }

    @Test
    void getMonitoringPeriodMs_ShouldReturnDefault_WhenValueIsInvalidOrMissing() {
        AppConfig invalidConfig = new AppConfig();
        invalidConfig.setConfigValue("ABC");
        when(repository.findById(AppConfigService.MONITORING_KEY)).thenReturn(Optional.of(invalidConfig));
        assertEquals(300000L, service.getMonitoringPeriodMs());

        when(repository.findById(AppConfigService.MONITORING_KEY)).thenReturn(Optional.empty());
        assertEquals(300000L, service.getMonitoringPeriodMs());
    }

    @Test
    void getLastSuccessfulCheckTime_ShouldReturnParsedDate_WhenValid() {
        LocalDateTime now = LocalDateTime.now();
        AppConfig config = new AppConfig();
        config.setConfigValue(now.toString());
        when(repository.findById(AppConfigService.LAST_CHECK_KEY)).thenReturn(Optional.of(config));

        LocalDateTime result = service.getLastSuccessfulCheckTime();

        assertEquals(now.getMinute(), result.getMinute());
    }

    @Test
    void getLastSuccessfulCheckTime_ShouldReturnFallback_WhenInvalidOrMissing() {
        AppConfig invalidDate = new AppConfig();
        invalidDate.setConfigValue("data-errada");
        when(repository.findById(AppConfigService.LAST_CHECK_KEY)).thenReturn(Optional.of(invalidDate));
        assertNotNull(service.getLastSuccessfulCheckTime());

        when(repository.findById(AppConfigService.LAST_CHECK_KEY)).thenReturn(Optional.empty());
        assertNotNull(service.getLastSuccessfulCheckTime());
    }

    @Test
    void updateLastSuccessfulCheckTime_ShouldSaveNewConfig() {
        LocalDateTime now = LocalDateTime.now();
        when(repository.findById(any())).thenReturn(Optional.empty());

        service.updateLastSuccessfulCheckTime(now);

        verify(repository, times(1)).save(any(AppConfig.class));
    }
}