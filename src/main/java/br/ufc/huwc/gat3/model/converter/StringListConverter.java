package br.ufc.huwc.gat3.model.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;


/**
 * Conversor JPA para mapeamento de listas de Strings em formato JSON.
 * Esta classe permite que o Spring Data/Hibernate converta uma lista Java (List{@literal <}String{@literal >})
 * em um texto JSON único para armazenamento em colunas do tipo TEXT no banco de dados.
 */
@Converter
@RequiredArgsConstructor
public class StringListConverter implements AttributeConverter<List<String>, String> {

    private final ObjectMapper objectMapper;


    /**
     * Transforma uma lista de Strings em uma representação JSON (Banco de Dados).
     * Este método é chamado automaticamente pelo JPA no momento de salvar (INSERT/UPDATE).
     * @param attribute A lista de Strings contida na entidade.
     * @return Uma String formatada em JSON contendo os elementos da lista.
     * Retorna null caso a lista esteja vazia ou seja nula.
     * @throws IllegalArgumentException Caso ocorra um erro técnico na serialização do JSON.
     */
    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (IOException e) {
            throw new IllegalArgumentException("Erro ao serializar List<String> para JSON", e);
        }
    }

    /**
     * Transforma uma String JSON de volta para uma lista de Strings (Java).
     * Este método é chamado automaticamente pelo JPA no momento da leitura (SELECT).
     * @param dbData O conteúdo da coluna de texto recuperado do banco de dados.
     * @return Uma {@link List} de Strings reconstruída.
     * Retorna uma lista vazia se o dado no banco for nulo.
     * @throws IllegalArgumentException Caso o conteúdo do banco não seja um JSON válido.
     */
    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<String>>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException("Erro ao desserializar JSON para List<String>", e);
        }
    }
}