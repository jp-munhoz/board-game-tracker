package br.com.bg.collection.exception;

public class SessionParticipantAlreadyAddedException extends RuntimeException {

    public SessionParticipantAlreadyAddedException(String message) {
        super(message);
    }
}
