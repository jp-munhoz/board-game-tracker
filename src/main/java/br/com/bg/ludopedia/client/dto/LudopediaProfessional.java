package br.com.bg.ludopedia.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LudopediaProfessional(
        @JsonProperty("id_profissional") Integer idProfissional,
        @JsonProperty("nm_profissional") String nome) {
}
