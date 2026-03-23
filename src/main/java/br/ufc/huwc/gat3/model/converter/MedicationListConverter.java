package br.ufc.huwc.gat3.model.converter;

import br.ufc.huwc.gat3.model.Discharge.MedicationEntry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Conversor de Atributo JPA para transformar listas de medicamentos em formato JSON.
 * Esta classe permite que uma lista de objetos {@link MedicationEntry} seja persistida
 * em uma única coluna do tipo TEXT ou JSON no banco de dados, realizando a serialização
 * (Java para JSON) e desserialização (JSON para Java) de forma transparente para a aplicação.
 */
@Converter
public class MedicationListConverter implements AttributeConverter<List<MedicationEntry>, String> {

    private final ObjectMapper objectMapper;

    /**
     * Construtor padrão que inicializa um novo {@link ObjectMapper}.
     */
    public MedicationListConverter() {
        this(new ObjectMapper());
    }

    /**
     * Construtor que permite a injeção de um {@link ObjectMapper} customizado.
     * @param objectMapper O mapeador de objetos Jackson a ser utilizado.
     */
    public MedicationListConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Converte a lista de medicamentos (objeto Java) para uma String JSON para armazenamento no banco.
     * Se a lista for nula ou vazia, o método retorna nulo, evitando o armazenamento de arrays vazios "[]"
     * no banco de dados.
     * @param attribute A lista de {@link MedicationEntry} presente na entidade.
     * @return Uma String contendo o JSON correspondente ou {@code null}.
     * @throws IllegalArgumentException Se ocorrer um erro durante a serialização do JSON.
     */
    @Override
    public String convertToDatabaseColumn(List<MedicationEntry> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Erro ao serializar MedicationList para JSON", e);
        }
    }

    /**
     * Converte o JSON armazenado no banco de dados de volta para uma lista de objetos Java.
     * <p>
     * Caso a coluna no banco esteja nula, o método retorna uma lista vazia imutável para evitar
     * {@code NullPointerException} no código de negócios.
     * </p>
     * @param dbData A String JSON recuperada da coluna do banco de dados.
     * @return Uma {@link List} de {@link MedicationEntry}.
     * @throws IllegalArgumentException Se o JSON estiver malformado ou for incompatível com a classe de destino.
     */
    @Override
    public List<MedicationEntry> convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(dbData,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, MedicationEntry.class));
        } catch (IOException e) {
            throw new IllegalArgumentException("Erro ao desserializar JSON para MedicationList", e);
        }
    }
}