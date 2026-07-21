package br.com.bg.collection.service;

import br.com.bg.collection.domain.GameSession;
import br.com.bg.collection.domain.SessionNote;
import br.com.bg.collection.exception.GameSessionNotFoundException;
import br.com.bg.collection.exception.SessionAccessDeniedException;
import br.com.bg.collection.repository.GameSessionRepository;
import br.com.bg.collection.repository.SessionNoteRepository;
import br.com.bg.collection.web.dto.SessionNoteResponse;
import br.com.bg.collection.web.dto.SharedSessionResponse;
import br.com.bg.game.domain.Game;
import br.com.bg.user.domain.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SessionParticipationService {

    private final GameSessionRepository gameSessionRepository;
    private final SessionNoteRepository sessionNoteRepository;

    public SessionParticipationService(GameSessionRepository gameSessionRepository,
                                        SessionNoteRepository sessionNoteRepository) {
        this.gameSessionRepository = gameSessionRepository;
        this.sessionNoteRepository = sessionNoteRepository;
    }

    @Transactional(readOnly = true)
    public List<SharedSessionResponse> listParticipating(Long userId) {
        return sessionNoteRepository.findParticipatingNotes(userId).stream()
                .map(note -> toResponse(note.getGameSession()))
                .toList();
    }

    @Transactional(readOnly = true)
    public SharedSessionResponse getDetail(Long userId, long sessionId) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new GameSessionNotFoundException("Sessao com id " + sessionId + " nao encontrada"));
        requireAccess(session, userId);
        return toResponse(session);
    }

    @Transactional
    public SharedSessionResponse updateMyNote(Long userId, long sessionId, String text) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new GameSessionNotFoundException("Sessao com id " + sessionId + " nao encontrada"));
        AppUser me = requireAccess(session, userId);
        SessionNote note = sessionNoteRepository.findByGameSession_IdAndAuthor_Id(session.getId(), userId)
                .orElseGet(() -> new SessionNote(session, me, null));
        note.updateText(text);
        sessionNoteRepository.save(note);
        return toResponse(session);
    }

    private AppUser requireAccess(GameSession session, Long userId) {
        boolean isOwner = session.getCollectionEntry().getUser().getId().equals(userId);
        if (isOwner) {
            return session.getCollectionEntry().getUser();
        }
        return sessionNoteRepository.findByGameSession_IdAndAuthor_Id(session.getId(), userId)
                .map(SessionNote::getAuthor)
                .orElseThrow(() -> new SessionAccessDeniedException("Voce nao tem acesso a essa sessao"));
    }

    private SharedSessionResponse toResponse(GameSession session) {
        Game game = session.getCollectionEntry().getGame();
        AppUser owner = session.getCollectionEntry().getUser();
        List<SessionNoteResponse> notes = sessionNoteRepository.findByGameSession_IdOrderByCreatedAtAsc(session.getId()).stream()
                .map(this::toNoteResponse)
                .toList();
        return new SharedSessionResponse(
                session.getId(),
                game.getId(),
                game.getName(),
                game.getImageUrl(),
                owner.getUsername(),
                owner.getDisplayName(),
                session.getPlayedAt(),
                session.getStatus(),
                notes);
    }

    private SessionNoteResponse toNoteResponse(SessionNote note) {
        AppUser author = note.getAuthor();
        return new SessionNoteResponse(author.getUsername(), author.getDisplayName(), note.getText(), note.getUpdatedAt());
    }
}
