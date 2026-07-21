package br.com.bg.collection.service;

import br.com.bg.collection.domain.CollectionEntry;
import br.com.bg.collection.exception.CollectionGameAlreadyAddedException;
import br.com.bg.collection.exception.CollectionGameNotFoundException;
import br.com.bg.collection.exception.CollectionUserNotFoundException;
import br.com.bg.collection.repository.CollectionEntryRepository;
import br.com.bg.collection.repository.GameSessionRepository;
import br.com.bg.collection.repository.SessionNoteRepository;
import br.com.bg.collection.web.dto.CollectionGameResponse;
import br.com.bg.game.domain.Game;
import br.com.bg.ludopedia.service.LudopediaGameService;
import br.com.bg.user.domain.AppUser;
import br.com.bg.user.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CollectionService {

    private final CollectionEntryRepository collectionEntryRepository;
    private final GameSessionRepository gameSessionRepository;
    private final SessionNoteRepository sessionNoteRepository;
    private final AppUserRepository appUserRepository;
    private final LudopediaGameService ludopediaGameService;

    public CollectionService(CollectionEntryRepository collectionEntryRepository,
                              GameSessionRepository gameSessionRepository,
                              SessionNoteRepository sessionNoteRepository,
                              AppUserRepository appUserRepository,
                              LudopediaGameService ludopediaGameService) {
        this.collectionEntryRepository = collectionEntryRepository;
        this.gameSessionRepository = gameSessionRepository;
        this.sessionNoteRepository = sessionNoteRepository;
        this.appUserRepository = appUserRepository;
        this.ludopediaGameService = ludopediaGameService;
    }

    @Transactional(readOnly = true)
    public List<CollectionGameResponse> list(Long userId) {
        return collectionEntryRepository.findByUser_IdOrderByAddedAtDesc(userId).stream()
                .map(entry -> toResponse(entry.getGame()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CollectionGameResponse> listByUsername(String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new CollectionUserNotFoundException("Usuario " + username + " nao encontrado"));
        return list(user.getId());
    }

    @Transactional
    public CollectionGameResponse add(Long userId, long gameId) {
        if (collectionEntryRepository.existsByUser_IdAndGame_Id(userId, gameId)) {
            throw new CollectionGameAlreadyAddedException("Jogo com id " + gameId + " ja esta na colecao");
        }
        AppUser user = appUserRepository.getReferenceById(userId);
        Game game = ludopediaGameService.getOrFetchGame(gameId);
        CollectionEntry entry = collectionEntryRepository.save(new CollectionEntry(user, game));
        return toResponse(entry.getGame());
    }

    @Transactional
    public void remove(Long userId, long gameId) {
        CollectionEntry entry = collectionEntryRepository.findByUser_IdAndGame_Id(userId, gameId)
                .orElseThrow(() -> new CollectionGameNotFoundException("Jogo com id " + gameId + " nao esta na colecao"));
        sessionNoteRepository.deleteByGameSession_CollectionEntry_Id(entry.getId());
        gameSessionRepository.deleteByCollectionEntry_Id(entry.getId());
        collectionEntryRepository.delete(entry);
    }

    private CollectionGameResponse toResponse(Game game) {
        return new CollectionGameResponse(game.getId(), game.getName(), game.getImageUrl(), game.getYearPublished(), game.getLink());
    }
}
