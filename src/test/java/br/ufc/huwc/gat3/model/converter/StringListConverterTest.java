package br.ufc.huwc.gat3.model.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StringListConverterTest {

    private ObjectMapper objectMapper;
    private StringListConverter converter;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        converter = new StringListConverter(objectMapper);
    }

    @Test
    void convertToDatabaseColumn_ShouldReturnJson_WhenListIsValid() {
        List<String> list = Arrays.asList("item1", "item2");
        String result = converter.convertToDatabaseColumn(list);

        assertNotNull(result);
        assertEquals("[\"item1\",\"item2\"]", result);
    }

    @Test
    void convertToDatabaseColumn_ShouldReturnNull_WhenListIsNullOrEmpty() {
        assertNull(converter.convertToDatabaseColumn(null));

        assertNull(converter.convertToDatabaseColumn(Collections.emptyList()));
    }

    @Test
    void convertToEntityAttribute_ShouldReturnList_WhenJsonIsValid() {
        String json = "[\"valor1\",\"valor2\"]";
        List<String> result = converter.convertToEntityAttribute(json);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("valor1", result.get(0));
        assertEquals("valor2", result.get(1));
    }

    @Test
    void convertToEntityAttribute_ShouldReturnEmptyList_WhenDataIsNull() {
        List<String> result = converter.convertToEntityAttribute(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void convertToEntityAttribute_ShouldThrowException_WhenJsonIsInvalid() {
        String invalidJson = "{esta_faltando_aspas_e_colchetes}";

        assertThrows(IllegalArgumentException.class, () -> {
            converter.convertToEntityAttribute(invalidJson);
        });
    }

    @Test
    void convertToDatabaseColumn_ShouldHandleError_WhenSerializationFails() {
        ObjectMapper mockMapper = org.mockito.Mockito.mock(ObjectMapper.class);
        StringListConverter errorConverter = new StringListConverter(mockMapper);

        try {
            org.mockito.Mockito.when(mockMapper.writeValueAsString(org.mockito.Mockito.any()))
                    .thenThrow(new com.fasterxml.jackson.core.JsonGenerationException("Erro forçado", (com.fasterxml.jackson.core.JsonGenerator) null));
        } catch (java.io.IOException e) {
            // Silencia erro do mock
        }

        List<String> list = Collections.singletonList("erro");
        assertThrows(IllegalArgumentException.class, () -> errorConverter.convertToDatabaseColumn(list));
    }
}