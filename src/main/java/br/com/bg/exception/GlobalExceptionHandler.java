package br.com.bg.exception;

import br.com.bg.collection.exception.CollectionGameAlreadyAddedException;
import br.com.bg.collection.exception.CollectionGameNotFoundException;
import br.com.bg.collection.exception.CollectionUserNotFoundException;
import br.com.bg.collection.exception.GameSessionNotFoundException;
import br.com.bg.collection.exception.SessionAccessDeniedException;
import br.com.bg.collection.exception.SessionParticipantAlreadyAddedException;
import br.com.bg.collection.exception.SessionParticipantNotFoundException;
import br.com.bg.ludopedia.exception.LudopediaGameNotFoundException;
import br.com.bg.ludopedia.exception.LudopediaIntegrationException;
import br.com.bg.user.exception.UsernameAlreadyExistsException;
import br.com.bg.wishlist.exception.WishlistGameAlreadyAddedException;
import br.com.bg.wishlist.exception.WishlistGameNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LudopediaGameNotFoundException.class)
    public ProblemDetail handleLudopediaNotFound(LudopediaGameNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(LudopediaIntegrationException.class)
    public ProblemDetail handleLudopediaIntegration(LudopediaIntegrationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    @ExceptionHandler(CollectionGameNotFoundException.class)
    public ProblemDetail handleCollectionGameNotFound(CollectionGameNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CollectionUserNotFoundException.class)
    public ProblemDetail handleCollectionUserNotFound(CollectionUserNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CollectionGameAlreadyAddedException.class)
    public ProblemDetail handleCollectionGameAlreadyAdded(CollectionGameAlreadyAddedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(GameSessionNotFoundException.class)
    public ProblemDetail handleGameSessionNotFound(GameSessionNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(SessionParticipantNotFoundException.class)
    public ProblemDetail handleSessionParticipantNotFound(SessionParticipantNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(SessionParticipantAlreadyAddedException.class)
    public ProblemDetail handleSessionParticipantAlreadyAdded(SessionParticipantAlreadyAddedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(SessionAccessDeniedException.class)
    public ProblemDetail handleSessionAccessDenied(SessionAccessDeniedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ProblemDetail handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(WishlistGameNotFoundException.class)
    public ProblemDetail handleWishlistGameNotFound(WishlistGameNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(WishlistGameAlreadyAddedException.class)
    public ProblemDetail handleWishlistGameAlreadyAdded(WishlistGameAlreadyAddedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Usuario ou senha invalidos");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleValidation(ConstraintViolationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
