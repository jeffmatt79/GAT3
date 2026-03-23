package br.ufc.huwc.gat3.controller;

import br.ufc.huwc.gat3.dto.AppointmentDTO;
import br.ufc.huwc.gat3.dto.DischargeDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgHuMockControllerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void getMockAppointmentsShouldReturnMappedListWhenFileReadWorks() throws Exception {
        AgHuMockController controller = new AgHuMockController(objectMapper);
        List<AppointmentDTO> expected = Collections.singletonList(AppointmentDTO.builder().seqAtendimento(1L).build());

        when(objectMapper.readValue(
                any(InputStream.class),
                ArgumentMatchers.<TypeReference<List<AppointmentDTO>>>any()))
                .thenReturn(expected);

        List<AppointmentDTO> result = controller.getMockAppointments("2026-05-01", "2026-05-01", "meac");

        verify(objectMapper).readValue(any(InputStream.class), ArgumentMatchers.<TypeReference<List<AppointmentDTO>>>any());
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getSeqAtendimento());
    }

    @Test
    void getMockAppointmentsShouldReturnEmptyListWhenMapperThrows() throws Exception {
        AgHuMockController controller = new AgHuMockController(objectMapper);

        when(objectMapper.readValue(
                any(InputStream.class),
                ArgumentMatchers.<TypeReference<List<AppointmentDTO>>>any()))
                .thenThrow(new IOException("Falha de leitura"));

        List<AppointmentDTO> result = controller.getMockAppointments("2026-05-01", "2026-05-01", "meac");

        verify(objectMapper).readValue(any(InputStream.class), ArgumentMatchers.<TypeReference<List<AppointmentDTO>>>any());
        assertTrue(result.isEmpty());
    }

    @Test
    void getMockDischargeBySeqShouldReturnDtoWhenSeqExistsInMap() throws Exception {
        AgHuMockController controller = new AgHuMockController(objectMapper);
        DischargeDTO dto = new DischargeDTO();
        dto.setNumero(1463134L);
        Map<String, DischargeDTO> detalhes = new HashMap<>();
        detalhes.put("1463134", dto);

        when(objectMapper.readValue(
                any(InputStream.class),
                ArgumentMatchers.<TypeReference<Map<String, DischargeDTO>>>any()))
                .thenReturn(detalhes);

        ResponseEntity<DischargeDTO> response = controller.getMockDischargeBySeq(1463134L, "meac");

        verify(objectMapper).readValue(any(InputStream.class), ArgumentMatchers.<TypeReference<Map<String, DischargeDTO>>>any());
        assertNotNull(response.getBody());
        assertEquals(1463134L, response.getBody().getNumero());
    }

    @Test
    void getMockDischargeBySeqShouldReturnGenericDtoWhenSeqIsMissing() throws Exception {
        AgHuMockController controller = new AgHuMockController(objectMapper);

        when(objectMapper.readValue(
                any(InputStream.class),
                ArgumentMatchers.<TypeReference<Map<String, DischargeDTO>>>any()))
                .thenReturn(Collections.emptyMap());

        ResponseEntity<DischargeDTO> response = controller.getMockDischargeBySeq(999L, "meac");

        verify(objectMapper).readValue(any(InputStream.class), ArgumentMatchers.<TypeReference<Map<String, DischargeDTO>>>any());
        assertNotNull(response.getBody());
        assertEquals(999L, response.getBody().getNumero());
    }

    @Test
    void getMockDischargeBySeqShouldReturnGenericDtoWhenMapperThrows() throws Exception {
        AgHuMockController controller = new AgHuMockController(objectMapper);

        when(objectMapper.readValue(
                any(InputStream.class),
                ArgumentMatchers.<TypeReference<Map<String, DischargeDTO>>>any()))
                .thenThrow(new IOException("Falha de leitura"));

        ResponseEntity<DischargeDTO> response = controller.getMockDischargeBySeq(555L, "meac");

        verify(objectMapper).readValue(any(InputStream.class), ArgumentMatchers.<TypeReference<Map<String, DischargeDTO>>>any());
        assertNotNull(response.getBody());
        assertEquals(555L, response.getBody().getNumero());
    }
}
