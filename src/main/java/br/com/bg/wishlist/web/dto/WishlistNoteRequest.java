package br.com.bg.wishlist.web.dto;

import jakarta.validation.constraints.Size;

public record WishlistNoteRequest(@Size(max = 2000) String note) {
}
