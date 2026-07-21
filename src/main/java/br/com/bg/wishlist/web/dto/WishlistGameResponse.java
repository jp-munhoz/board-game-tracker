package br.com.bg.wishlist.web.dto;

public record WishlistGameResponse(
        long id,
        String name,
        String imageUrl,
        Integer yearPublished,
        String link,
        String note) {
}
