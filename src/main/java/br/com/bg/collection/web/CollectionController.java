package br.com.bg.collection.web;

import br.com.bg.collection.service.CollectionService;
import br.com.bg.collection.web.dto.CollectionGameRequest;
import br.com.bg.collection.web.dto.CollectionGameResponse;
import br.com.bg.security.AppUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/collection", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping
    public List<CollectionGameResponse> list(@AuthenticationPrincipal AppUserDetails principal) {
        return collectionService.list(principal.getId());
    }

    @GetMapping("/users/{username}")
    public List<CollectionGameResponse> listByUsername(@PathVariable @NotBlank String username) {
        return collectionService.listByUsername(username);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CollectionGameResponse add(@AuthenticationPrincipal AppUserDetails principal,
                                       @Valid @RequestBody CollectionGameRequest request) {
        return collectionService.add(principal.getId(), request.gameId());
    }

    @DeleteMapping("/{gameId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@AuthenticationPrincipal AppUserDetails principal, @PathVariable @Positive long gameId) {
        collectionService.remove(principal.getId(), gameId);
    }
}
