package br.com.bg.ludopedia.web;

import br.com.bg.ludopedia.service.LudopediaGameService;
import br.com.bg.ludopedia.web.dto.LudopediaGameDetailsResponse;
import br.com.bg.ludopedia.web.dto.LudopediaGameSummaryResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/ludopedia/games", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class LudopediaGameController {

    private final LudopediaGameService gameService;

    public LudopediaGameController(LudopediaGameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/search")
    public List<LudopediaGameSummaryResponse> search(@RequestParam @NotBlank String name) {
        return gameService.search(name);
    }

    @GetMapping("/{id}")
    public LudopediaGameDetailsResponse getById(@PathVariable @Positive long id) {
        return gameService.getGame(id);
    }
}
