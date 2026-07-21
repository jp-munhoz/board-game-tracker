package br.com.bg.ludopedia.web.dto;

public record RankingGameResponse(
        long id,
        String name,
        String imageUrl,
        Integer yearPublished,
        String link,
        Integer rank) {
}
