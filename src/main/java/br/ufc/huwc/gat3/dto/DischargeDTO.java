package br.ufc.huwc.gat3.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DischargeDTO {

    private Long numero;
    private OffsetDateTime dtConsulta;

    private PatientDTO paciente;
    private GradeDTO grade;

    private List<AttendanceDTO> atendimentos;

    private List<AnamneseDTO> anamneses;
    private List<PrescriptionDTO> receituarios;

    private List<CidProcessDTO> cidsPrimarios;
    private List<CidProcessDTO> cidsSecundarios;

    private List<EvolutionDTO> evolucoes;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AnamneseDTO {
        private List<AnamneseItemDTO> itens;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AnamneseItemDTO {
        private String descricao;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PrescriptionDTO {
        private List<PrescriptionItemDTO> itens;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PrescriptionItemDTO {
        private String descricao;
        private String formaUso;
        private String quantidade;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PatientDTO {
        private Long prontuario;
        private String nome;
        private OffsetDateTime dtNascimento;
        private String sexo;
        private String nomeMae;
        private List<AddressDTO> enderecos;
        private List<ContactDTO> contatos;
        private String cpf;
        private String cns;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddressDTO {
        private String logradouro;
        private Integer nroLogradouro;
        private String complLogradouro;
        private String bairro;
        private CityDTO cidade;
        private UfDTO uf;
        private Long bclCloCep;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CityDTO {
        private String nome;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UfDTO {
        private String sigla;
        private String nome;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContactDTO {
        private Integer ddd;
        private String nroFone;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GradeDTO {
        private TeamDTO equipe;
        private PreceptorDTO preceptor;
        private SpecialtyDTO especialidade;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TeamDTO {
        private String nome;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PreceptorDTO {
        private PersonDTO pessoa;
        private String creMec;
        private String rqe;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PersonDTO {
        private String nome;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SpecialtyDTO {
        private String nomeEspecialidade;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AttendanceDTO {
        private Long seq;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EvolutionDTO {
        private List<EvolutionItemDTO> itens;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EvolutionItemDTO {
        private String descricao;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CidProcessDTO {
        private CidDetailDTO cid;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CidDetailDTO {
        private String codigo;
        private String descricao;
    }
}
