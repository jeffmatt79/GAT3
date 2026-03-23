package br.ufc.huwc.gat3.model.converter;

import br.ufc.huwc.gat3.model.Discharge.MedicationEntry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicationListConverterTest {

    private ObjectMapper objectMapper;
    private MedicationListConverter converter;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        converter = new MedicationListConverter(objectMapper);
    }

    @Test
    void defaultConstructor_ShouldInstantiateConverter() {
        MedicationListConverter defaultConverter = new MedicationListConverter();

        assertNotNull(defaultConverter);
        assertNull(defaultConverter.convertToDatabaseColumn(Collections.emptyList()));
    }

    @Test
    void convertToDatabaseColumn_ShouldReturnJson_WhenListIsValid() throws JsonProcessingException {
        String jsonInput = "{\"name\":\"Dipirona\",\"dosage\":\"1g\",\"quantity\":\"1\"}";
        MedicationEntry entry = objectMapper.readValue(jsonInput, MedicationEntry.class);

        List<MedicationEntry> list = Collections.singletonList(entry);
        String result = converter.convertToDatabaseColumn(list);

        assertNotNull(result);
        assertTrue(result.contains("Dipirona"));
    }

    @Test
    void convertToDatabaseColumn_ShouldReturnNull_WhenListIsNullOrEmpty() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToDatabaseColumn(Collections.emptyList()));
    }

    @Test
    void convertToEntityAttribute_ShouldReturnList_WhenJsonIsValid() {
        String json = "[{\"name\":\"Paracetamol\",\"dosage\":\"500mg\",\"quantity\":\"1\"}]";
        List<MedicationEntry> result = converter.convertToEntityAttribute(json);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(0));
    }

    @Test
    void convertToEntityAttribute_ShouldReturnEmptyList_WhenDataIsNull() {
        List<MedicationEntry> result = converter.convertToEntityAttribute(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void convertToDatabaseColumn_ShouldThrowException_WhenSerializationFails() throws JsonProcessingException {
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        MedicationListConverter errorConverter = new MedicationListConverter(mockMapper);

        when(mockMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Mock Error") {
        });

        List<MedicationEntry> list = Collections.singletonList(new MedicationEntry());
        assertThrows(IllegalArgumentException.class, () -> errorConverter.convertToDatabaseColumn(list));
    }

    @Test
    void convertToEntityAttribute_ShouldThrowException_WhenJsonIsInvalid() {
        String invalidJson = "{ [ invalid_json ] }";
        assertThrows(IllegalArgumentException.class, () -> {
            converter.convertToEntityAttribute(invalidJson);
        });
    }
}
