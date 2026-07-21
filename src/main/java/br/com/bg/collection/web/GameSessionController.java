package br.com.bg.collection.web;

import br.com.bg.collection.service.GameSessionService;
import br.com.bg.collection.web.dto.GameSessionRequest;
import br.com.bg.collection.web.dto.GameSessionResponse;
import br.com.bg.collection.web.dto.SessionParticipantRequest;
import br.com.bg.security.AppUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/collection/{gameId}/sessions", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class GameSessionController {

    private final GameSessionService gameSessionService;

    public GameSessionController(GameSessionService gameSessionService) {
        this.gameSessionService = gameSessionService;
    }

    @GetMapping
    public List<GameSessionResponse> list(@AuthenticationPrincipal AppUserDetails principal,
                                           @PathVariable @Positive long gameId) {
        return gameSessionService.list(principal.getId(), gameId);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public GameSessionResponse add(@AuthenticationPrincipal AppUserDetails principal,
                                    @PathVariable @Positive long gameId,
                                    @Valid @RequestBody GameSessionRequest request) {
        return gameSessionService.add(principal.getId(), gameId, request.playedAt(), request.notes());
    }

    @PutMapping(value = "/{sessionId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public GameSessionResponse update(@AuthenticationPrincipal AppUserDetails principal,
                                       @PathVariable @Positive long gameId,
                                       @PathVariable @Positive long sessionId,
                                       @Valid @RequestBody GameSessionRequest request) {
        return gameSessionService.update(principal.getId(), gameId, sessionId, request.playedAt(), request.notes());
    }

    @PostMapping("/{sessionId}/complete")
    public GameSessionResponse complete(@AuthenticationPrincipal AppUserDetails principal,
                                         @PathVariable @Positive long gameId,
                                         @PathVariable @Positive long sessionId) {
        return gameSessionService.complete(principal.getId(), gameId, sessionId);
    }

    @DeleteMapping("/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@AuthenticationPrincipal AppUserDetails principal,
                        @PathVariable @Positive long gameId,
                        @PathVariable @Positive long sessionId) {
        gameSessionService.remove(principal.getId(), gameId, sessionId);
    }

    @PostMapping(value = "/{sessionId}/participants", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public GameSessionResponse addParticipant(@AuthenticationPrincipal AppUserDetails principal,
                                               @PathVariable @Positive long gameId,
                                               @PathVariable @Positive long sessionId,
                                               @Valid @RequestBody SessionParticipantRequest request) {
        return gameSessionService.addParticipant(principal.getId(), gameId, sessionId, request.username());
    }

    @DeleteMapping("/{sessionId}/participants/{username}")
    public GameSessionResponse removeParticipant(@AuthenticationPrincipal AppUserDetails principal,
                                                  @PathVariable @Positive long gameId,
                                                  @PathVariable @Positive long sessionId,
                                                  @PathVariable String username) {
        return gameSessionService.removeParticipant(principal.getId(), gameId, sessionId, username);
    }
}
