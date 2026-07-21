package br.com.bg.wishlist.service;

import br.com.bg.game.domain.Game;
import br.com.bg.ludopedia.service.LudopediaGameService;
import br.com.bg.user.domain.AppUser;
import br.com.bg.user.repository.AppUserRepository;
import br.com.bg.wishlist.domain.WishlistEntry;
import br.com.bg.wishlist.exception.WishlistGameAlreadyAddedException;
import br.com.bg.wishlist.exception.WishlistGameNotFoundException;
import br.com.bg.wishlist.repository.WishlistEntryRepository;
import br.com.bg.wishlist.web.dto.WishlistGameResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishlistService {

    private final WishlistEntryRepository wishlistEntryRepository;
    private final AppUserRepository appUserRepository;
    private final LudopediaGameService ludopediaGameService;

    public WishlistService(WishlistEntryRepository wishlistEntryRepository,
                            AppUserRepository appUserRepository,
                            LudopediaGameService ludopediaGameService) {
        this.wishlistEntryRepository = wishlistEntryRepository;
        this.appUserRepository = appUserRepository;
        this.ludopediaGameService = ludopediaGameService;
    }

    @Transactional(readOnly = true)
    public List<WishlistGameResponse> list(Long userId) {
        return wishlistEntryRepository.findByUser_IdOrderByAddedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public WishlistGameResponse add(Long userId, long gameId, String note) {
        if (wishlistEntryRepository.existsByUser_IdAndGame_Id(userId, gameId)) {
            throw new WishlistGameAlreadyAddedException("Jogo com id " + gameId + " ja esta na lista de desejos");
        }
        AppUser user = appUserRepository.getReferenceById(userId);
        Game game = ludopediaGameService.getOrFetchGame(gameId);
        WishlistEntry entry = wishlistEntryRepository.save(new WishlistEntry(user, game, note));
        return toResponse(entry);
    }

    @Transactional
    public WishlistGameResponse updateNote(Long userId, long gameId, String note) {
        WishlistEntry entry = wishlistEntryRepository.findByUser_IdAndGame_Id(userId, gameId)
                .orElseThrow(() -> new WishlistGameNotFoundException("Jogo com id " + gameId + " nao esta na lista de desejos"));
        entry.updateNote(note);
        return toResponse(entry);
    }

    @Transactional
    public void remove(Long userId, long gameId) {
        WishlistEntry entry = wishlistEntryRepository.findByUser_IdAndGame_Id(userId, gameId)
                .orElseThrow(() -> new WishlistGameNotFoundException("Jogo com id " + gameId + " nao esta na lista de desejos"));
        wishlistEntryRepository.delete(entry);
    }

    private WishlistGameResponse toResponse(WishlistEntry entry) {
        Game game = entry.getGame();
        return new WishlistGameResponse(game.getId(), game.getName(), game.getImageUrl(), game.getYearPublished(), game.getLink(), entry.getNote());
    }
}
