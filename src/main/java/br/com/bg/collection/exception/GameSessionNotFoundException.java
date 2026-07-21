package br.com.bg.collection.exception;

public class GameSessionNotFoundException extends RuntimeException {

    public GameSessionNotFoundException(String message) {
        super(message);
    }
}
