package br.ufc.huwc.gat3.service;

import br.ufc.huwc.gat3.dto.AppointmentDTO;
import br.ufc.huwc.gat3.dto.DischargeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgHuApiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AgHuApiService agHuApiService;

    private final String baseUrl = "http://localhost:8080";
    private final String testToken = "test-token-123";
    private LocalDate startDate;
    private LocalDate endDate;
    private final String unit = "meac";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(agHuApiService, "aghuBaseUrl", baseUrl);
        ReflectionTestUtils.setField(agHuApiService, "apiToken", testToken);

        startDate = LocalDate.now().minusDays(1);
        endDate = LocalDate.now();
    }

    @Test
    void shouldReturnAppointmentListWhenCallIsSuccessful() {
        AppointmentDTO dto = new AppointmentDTO();
        List<AppointmentDTO> expectedList = Collections.singletonList(dto);
        ResponseEntity<List<AppointmentDTO>> responseEntity = new ResponseEntity<>(expectedList,
                HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        List<AppointmentDTO> result = agHuApiService.fetchAppointmentsRange(startDate, endDate, unit);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenApiReturnsError() {
        ResponseEntity<List<AppointmentDTO>> responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        List<AppointmentDTO> result = agHuApiService.fetchAppointmentsRange(startDate, endDate, unit);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenExceptionOccurs() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenThrow(new RuntimeException("Connection error"));

        List<AppointmentDTO> result = agHuApiService.fetchAppointmentsRange(startDate, endDate, unit);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnDischargeDTOWhenCallIsSuccessful() {
        Long seq = 1463134L;
        String filial = "meac";
        DischargeDTO expectedDto = new DischargeDTO();
        expectedDto.setNumero(seq);
        ResponseEntity<DischargeDTO> responseEntity = new ResponseEntity<>(expectedDto, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(DischargeDTO.class))).thenReturn(responseEntity);

        DischargeDTO result = agHuApiService.fetchDischargeData(seq, filial);

        assertNotNull(result);
        assertEquals(seq, result.getNumero());
    }

    @Test
    void shouldReturnNullWhenDischargeApiReturnsError() {
        ResponseEntity<DischargeDTO> responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(DischargeDTO.class))).thenReturn(responseEntity);

        DischargeDTO result = agHuApiService.fetchDischargeData(1L, "meac");

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenDischargeHttpStatusCodeExceptionOccurs() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(DischargeDTO.class))).thenThrow(new HttpStatusCodeException(HttpStatus.INTERNAL_SERVER_ERROR) {});

        DischargeDTO result = agHuApiService.fetchDischargeData(1L, "meac");

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenDischargeGenericExceptionOccurs() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(DischargeDTO.class))).thenThrow(new RuntimeException("Unexpected error"));

        DischargeDTO result = agHuApiService.fetchDischargeData(1L, "meac");

        assertNull(result);
    }
}