package br.com.bg.ludopedia.web.dto;

public record LudopediaGameSummaryResponse(
        long id,
        String name,
        String originalName,
        String imageUrl,
        Integer yearPublished,
        String link) {
}
