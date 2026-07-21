package br.com.bg.ludopedia.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LudopediaCategory(
        @JsonProperty("id_categoria") Integer idCategoria,
        @JsonProperty("nm_categoria") String nome) {
}
