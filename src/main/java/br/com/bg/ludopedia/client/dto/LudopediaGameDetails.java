package br.com.bg.ludopedia.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LudopediaGameDetails(
        @JsonProperty("id_jogo") Long idJogo,
        @JsonProperty("nm_jogo") String nmJogo,
        String thumb,
        @JsonProperty("tp_jogo") String tpJogo,
        String link,
        @JsonProperty("ano_publicacao") Integer anoPublicacao,
        @JsonProperty("ano_nacional") Integer anoNacional,
        @JsonProperty("qt_jogadores_min") Integer qtJogadoresMin,
        @JsonProperty("qt_jogadores_max") Integer qtJogadoresMax,
        @JsonProperty("vl_tempo_jogo") Integer vlTempoJogo,
        @JsonProperty("idade_minima") Integer idadeMinima,
        @JsonProperty("qt_tem") Integer qtTem,
        @JsonProperty("qt_teve") Integer qtTeve,
        @JsonProperty("qt_favorito") Integer qtFavorito,
        @JsonProperty("qt_quer") Integer qtQuer,
        @JsonProperty("qt_jogou") Integer qtJogou,
        List<LudopediaMechanic> mecanicas,
        List<LudopediaCategory> categorias,
        List<LudopediaTheme> temas,
        List<LudopediaProfessional> artistas,
        List<LudopediaProfessional> designers) {
}
