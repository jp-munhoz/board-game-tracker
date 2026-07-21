package br.com.bg.collection.web.dto;

public record CollectionGameResponse(
        long id,
        String name,
        String imageUrl,
        Integer yearPublished,
        String link) {
}
