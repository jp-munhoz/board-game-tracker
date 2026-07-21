package br.com.bg.ludopedia.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LudopediaMechanic(
        @JsonProperty("id_mecanica") Integer idMecanica,
        @JsonProperty("nm_mecanica") String nome) {
}
