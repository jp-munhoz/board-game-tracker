package br.com.bg.wishlist.web;

import br.com.bg.security.AppUserDetails;
import br.com.bg.wishlist.service.WishlistService;
import br.com.bg.wishlist.web.dto.WishlistGameRequest;
import br.com.bg.wishlist.web.dto.WishlistGameResponse;
import br.com.bg.wishlist.web.dto.WishlistNoteRequest;
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
@RequestMapping(value = "/api/wishlist", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public List<WishlistGameResponse> list(@AuthenticationPrincipal AppUserDetails principal) {
        return wishlistService.list(principal.getId());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public WishlistGameResponse add(@AuthenticationPrincipal AppUserDetails principal,
                                     @Valid @RequestBody WishlistGameRequest request) {
        return wishlistService.add(principal.getId(), request.gameId(), request.note());
    }

    @PutMapping(value = "/{gameId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public WishlistGameResponse updateNote(@AuthenticationPrincipal AppUserDetails principal,
                                            @PathVariable @Positive long gameId,
                                            @Valid @RequestBody WishlistNoteRequest request) {
        return wishlistService.updateNote(principal.getId(), gameId, request.note());
    }

    @DeleteMapping("/{gameId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@AuthenticationPrincipal AppUserDetails principal, @PathVariable @Positive long gameId) {
        wishlistService.remove(principal.getId(), gameId);
    }
}
