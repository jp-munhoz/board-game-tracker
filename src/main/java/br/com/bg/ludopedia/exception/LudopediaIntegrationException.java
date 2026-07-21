package br.com.bg.ludopedia.exception;

public class LudopediaIntegrationException extends RuntimeException {

    public LudopediaIntegrationException(String message) {
        super(message);
    }

    public LudopediaIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
