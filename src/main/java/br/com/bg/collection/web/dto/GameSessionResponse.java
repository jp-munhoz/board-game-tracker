package br.com.bg.collection.web.dto;

import br.com.bg.collection.domain.SessionStatus;

import java.time.LocalDate;
import java.util.List;

public record GameSessionResponse(long id, LocalDate playedAt, SessionStatus status, List<SessionNoteResponse> notes) {
}
