package br.com.bg.collection.web.dto;

import br.com.bg.collection.domain.SessionStatus;

import java.time.LocalDate;
import java.util.List;

public record SharedSessionResponse(
        long sessionId,
        long gameId,
        String gameName,
        String gameImageUrl,
        String ownerUsername,
        String ownerDisplayName,
        LocalDate playedAt,
        SessionStatus status,
        List<SessionNoteResponse> notes) {
}
