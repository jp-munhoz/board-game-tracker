package br.com.bg.collection.service;

import br.com.bg.collection.domain.CollectionEntry;
import br.com.bg.collection.domain.GameSession;
import br.com.bg.collection.domain.SessionNote;
import br.com.bg.collection.domain.SessionStatus;
import br.com.bg.collection.exception.CollectionGameNotFoundException;
import br.com.bg.collection.exception.GameSessionNotFoundException;
import br.com.bg.collection.exception.SessionParticipantAlreadyAddedException;
import br.com.bg.collection.exception.SessionParticipantNotFoundException;
import br.com.bg.collection.repository.CollectionEntryRepository;
import br.com.bg.collection.repository.GameSessionRepository;
import br.com.bg.collection.repository.SessionNoteRepository;
import br.com.bg.collection.web.dto.GameSessionResponse;
import br.com.bg.collection.web.dto.SessionNoteResponse;
import br.com.bg.user.domain.AppUser;
import br.com.bg.user.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class GameSessionService {

    private final GameSessionRepository gameSessionRepository;
    private final CollectionEntryRepository collectionEntryRepository;
    private final SessionNoteRepository sessionNoteRepository;
    private final AppUserRepository appUserRepository;

    public GameSessionService(GameSessionRepository gameSessionRepository,
                               CollectionEntryRepository collectionEntryRepository,
                               SessionNoteRepository sessionNoteRepository,
                               AppUserRepository appUserRepository) {
        this.gameSessionRepository = gameSessionRepository;
        this.collectionEntryRepository = collectionEntryRepository;
        this.sessionNoteRepository = sessionNoteRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public List<GameSessionResponse> list(Long userId, long gameId) {
        CollectionEntry entry = getEntry(userId, gameId);
        return gameSessionRepository.findByCollectionEntry_IdOrderByPlayedAtDescCreatedAtDesc(entry.getId()).stream()
                .sorted(Comparator.comparing((GameSession s) -> s.getStatus() == SessionStatus.ONGOING ? 0 : 1)
                        .thenComparing(GameSession::getPlayedAt, Comparator.reverseOrder()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public GameSessionResponse add(Long userId, long gameId, LocalDate playedAt, String notes) {
        CollectionEntry entry = getEntry(userId, gameId);
        GameSession session = gameSessionRepository.save(new GameSession(entry, playedAt));
        sessionNoteRepository.save(new SessionNote(session, entry.getUser(), notes));
        return toResponse(session);
    }

    @Transactional
    public GameSessionResponse update(Long userId, long gameId, long sessionId, LocalDate playedAt, String notes) {
        CollectionEntry entry = getEntry(userId, gameId);
        GameSession session = gameSessionRepository.findByIdAndCollectionEntry_Id(sessionId, entry.getId())
                .orElseThrow(() -> new GameSessionNotFoundException("Sessao com id " + sessionId + " nao encontrada"));
        session.updatePlayedAt(playedAt);
        if (notes != null) {
            upsertNote(session, entry.getUser(), notes);
        }
        return toResponse(session);
    }

    @Transactional
    public GameSessionResponse complete(Long userId, long gameId, long sessionId) {
        GameSession session = getSession(userId, gameId, sessionId);
        session.complete();
        return toResponse(session);
    }

    @Transactional
    public void remove(Long userId, long gameId, long sessionId) {
        GameSession session = getSession(userId, gameId, sessionId);
        sessionNoteRepository.deleteByGameSession_Id(session.getId());
        gameSessionRepository.delete(session);
    }

    @Transactional
    public GameSessionResponse addParticipant(Long userId, long gameId, long sessionId, String username) {
        GameSession session = getSession(userId, gameId, sessionId);
        AppUser participant = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new SessionParticipantNotFoundException("Usuario " + username + " nao encontrado"));
        if (sessionNoteRepository.existsByGameSession_IdAndAuthor_Id(session.getId(), participant.getId())) {
            throw new SessionParticipantAlreadyAddedException("Usuario " + username + " ja participa dessa sessao");
        }
        sessionNoteRepository.save(new SessionNote(session, participant, null));
        return toResponse(session);
    }

    @Transactional
    public GameSessionResponse removeParticipant(Long userId, long gameId, long sessionId, String username) {
        GameSession session = getSession(userId, gameId, sessionId);
        CollectionEntry entry = getEntry(userId, gameId);
        if (entry.getUser().getUsername().equals(username)) {
            throw new SessionParticipantNotFoundException("O dono da sessao nao pode ser removido");
        }
        AppUser participant = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new SessionParticipantNotFoundException("Usuario " + username + " nao encontrado"));
        sessionNoteRepository.deleteByGameSession_IdAndAuthor_Id(session.getId(), participant.getId());
        return toResponse(session);
    }

    private void upsertNote(GameSession session, AppUser author, String text) {
        SessionNote note = sessionNoteRepository.findByGameSession_IdAndAuthor_Id(session.getId(), author.getId())
                .orElseGet(() -> new SessionNote(session, author, null));
        note.updateText(text);
        sessionNoteRepository.save(note);
    }

    private GameSession getSession(Long userId, long gameId, long sessionId) {
        CollectionEntry entry = getEntry(userId, gameId);
        return gameSessionRepository.findByIdAndCollectionEntry_Id(sessionId, entry.getId())
                .orElseThrow(() -> new GameSessionNotFoundException("Sessao com id " + sessionId + " nao encontrada"));
    }

    private CollectionEntry getEntry(Long userId, long gameId) {
        return collectionEntryRepository.findByUser_IdAndGame_Id(userId, gameId)
                .orElseThrow(() -> new CollectionGameNotFoundException("Jogo com id " + gameId + " nao esta na colecao"));
    }

    private GameSessionResponse toResponse(GameSession session) {
        List<SessionNoteResponse> notes = sessionNoteRepository.findByGameSession_IdOrderByCreatedAtAsc(session.getId()).stream()
                .map(this::toNoteResponse)
                .toList();
        return new GameSessionResponse(session.getId(), session.getPlayedAt(), session.getStatus(), notes);
    }

    private SessionNoteResponse toNoteResponse(SessionNote note) {
        AppUser author = note.getAuthor();
        return new SessionNoteResponse(author.getUsername(), author.getDisplayName(), note.getText(), note.getUpdatedAt());
    }
}
