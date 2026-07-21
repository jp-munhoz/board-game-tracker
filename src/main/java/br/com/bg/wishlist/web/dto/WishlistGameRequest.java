package br.com.bg.wishlist.web.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record WishlistGameRequest(@Positive long gameId, @Size(max = 2000) String note) {
}
