package br.com.bg.collection.exception;

public class SessionAccessDeniedException extends RuntimeException {

    public SessionAccessDeniedException(String message) {
        super(message);
    }
}
