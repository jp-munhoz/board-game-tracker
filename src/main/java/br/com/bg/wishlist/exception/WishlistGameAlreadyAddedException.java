package br.com.bg.wishlist.exception;

public class WishlistGameAlreadyAddedException extends RuntimeException {

    public WishlistGameAlreadyAddedException(String message) {
        super(message);
    }
}
