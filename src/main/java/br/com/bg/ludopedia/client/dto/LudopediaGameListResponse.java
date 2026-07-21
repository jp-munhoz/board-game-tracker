package br.com.bg.ludopedia.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LudopediaGameListResponse(
        List<LudopediaGameSummary> jogos,
        Integer total) {
}
