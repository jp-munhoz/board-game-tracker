package br.com.bg.ludopedia.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LudopediaTheme(
        @JsonProperty("id_tema") Integer idTema,
        @JsonProperty("nm_tema") String nome) {
}
