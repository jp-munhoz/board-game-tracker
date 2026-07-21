package br.com.bg.collection.web;

import br.com.bg.collection.service.SessionParticipationService;
import br.com.bg.collection.web.dto.SessionNoteRequest;
import br.com.bg.collection.web.dto.SharedSessionResponse;
import br.com.bg.security.AppUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/sessions", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class SessionController {

    private final SessionParticipationService sessionParticipationService;

    public SessionController(SessionParticipationService sessionParticipationService) {
        this.sessionParticipationService = sessionParticipationService;
    }

    @GetMapping("/shared")
    public List<SharedSessionResponse> listParticipating(@AuthenticationPrincipal AppUserDetails principal) {
        return sessionParticipationService.listParticipating(principal.getId());
    }

    @GetMapping("/{sessionId}")
    public SharedSessionResponse getDetail(@AuthenticationPrincipal AppUserDetails principal,
                                            @PathVariable @Positive long sessionId) {
        return sessionParticipationService.getDetail(principal.getId(), sessionId);
    }

    @PutMapping(value = "/{sessionId}/notes/me", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SharedSessionResponse updateMyNote(@AuthenticationPrincipal AppUserDetails principal,
                                               @PathVariable @Positive long sessionId,
                                               @Valid @RequestBody SessionNoteRequest request) {
        return sessionParticipationService.updateMyNote(principal.getId(), sessionId, request.text());
    }
}
