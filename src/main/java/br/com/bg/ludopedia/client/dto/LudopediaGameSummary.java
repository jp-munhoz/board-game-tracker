package br.com.bg.ludopedia.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LudopediaGameSummary(
        @JsonProperty("id_jogo") Long idJogo,
        @JsonProperty("nm_jogo") String nmJogo,
        @JsonProperty("nm_original") String nmOriginal,
        @JsonProperty("ano_publicacao") Integer anoPublicacao,
        String thumb,
        String link) {
}
