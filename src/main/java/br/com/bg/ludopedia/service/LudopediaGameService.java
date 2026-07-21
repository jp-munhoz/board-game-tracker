package br.com.bg.ludopedia.service;

import br.com.bg.game.domain.Game;
import br.com.bg.game.repository.GameRepository;
import br.com.bg.ludopedia.client.LudopediaClient;
import br.com.bg.ludopedia.client.dto.LudopediaCategory;
import br.com.bg.ludopedia.client.dto.LudopediaGameDetails;
import br.com.bg.ludopedia.client.dto.LudopediaGameListResponse;
import br.com.bg.ludopedia.client.dto.LudopediaGameSummary;
import br.com.bg.ludopedia.client.dto.LudopediaMechanic;
import br.com.bg.ludopedia.client.dto.LudopediaProfessional;
import br.com.bg.ludopedia.client.dto.LudopediaTheme;
import br.com.bg.ludopedia.client.scraper.LudopediaGamePageScraper;
import br.com.bg.ludopedia.client.scraper.LudopediaRankingScraper;
import br.com.bg.ludopedia.client.scraper.RankingGame;
import br.com.bg.ludopedia.exception.LudopediaGameNotFoundException;
import br.com.bg.ludopedia.web.dto.LudopediaGameDetailsResponse;
import br.com.bg.ludopedia.web.dto.LudopediaGameSummaryResponse;
import br.com.bg.ludopedia.web.dto.RankingGameResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LudopediaGameService {

    private final LudopediaClient client;
    private final LudopediaGamePageScraper pageScraper;
    private final LudopediaRankingScraper rankingScraper;
    private final GameRepository gameRepository;

    public LudopediaGameService(LudopediaClient client, LudopediaGamePageScraper pageScraper,
                                 LudopediaRankingScraper rankingScraper, GameRepository gameRepository) {
        this.client = client;
        this.pageScraper = pageScraper;
        this.rankingScraper = rankingScraper;
        this.gameRepository = gameRepository;
    }

    public List<LudopediaGameSummaryResponse> search(String name) {
        LudopediaGameListResponse response = client.search(name);
        if (response == null || response.jogos() == null) {
            return List.of();
        }
        return response.jogos().stream().map(this::toSummary).toList();
    }

    public LudopediaGameDetailsResponse getGame(long id) {
        Game game = getOrFetchGame(id);
        return toDetails(game);
    }

    /**
     * Retorna o jogo do cache local (Postgres); se ainda nao foi cacheado, busca na
     * Ludopedia (API + scraper de descricao) e grava o cache antes de retornar.
     */
    @Transactional
    public Game getOrFetchGame(long id) {
        return gameRepository.findById(id).orElseGet(() -> fetchAndCacheFromLudopedia(id));
    }

    private Game fetchAndCacheFromLudopedia(long id) {
        LudopediaGameDetails details = client.getGame(id);
        if (details == null || details.idJogo() == null) {
            throw new LudopediaGameNotFoundException("Jogo com id " + id + " nao encontrado na Ludopedia");
        }
        String description = pageScraper.fetchDescription(details.link()).orElse(null);

        Game game = new Game(details.idJogo());
        game.update(
                details.nmJogo(),
                null,
                details.thumb(),
                details.anoPublicacao(),
                details.anoNacional(),
                details.qtJogadoresMin(),
                details.qtJogadoresMax(),
                details.vlTempoJogo(),
                details.idadeMinima(),
                details.link(),
                description,
                mapMechanics(details.mecanicas()),
                mapCategories(details.categorias()),
                mapThemes(details.temas()),
                mapProfessionals(details.designers()),
                mapProfessionals(details.artistas()));
        return gameRepository.save(game);
    }

    public List<RankingGameResponse> getRankingByCategory(int idCategoria) {
        return rankingScraper.fetchByCategory(idCategoria).stream().map(this::toRanking).toList();
    }

    private RankingGameResponse toRanking(RankingGame game) {
        return new RankingGameResponse(
                game.idJogo(),
                game.nome(),
                game.thumb(),
                game.ano(),
                game.link(),
                game.rank());
    }

    private LudopediaGameSummaryResponse toSummary(LudopediaGameSummary summary) {
        return new LudopediaGameSummaryResponse(
                summary.idJogo() != null ? summary.idJogo() : 0L,
                summary.nmJogo(),
                summary.nmOriginal(),
                summary.thumb(),
                summary.anoPublicacao(),
                summary.link());
    }

    private LudopediaGameDetailsResponse toDetails(Game game) {
        return new LudopediaGameDetailsResponse(
                game.getId(),
                game.getName(),
                game.getImageUrl(),
                game.getYearPublished(),
                game.getNationalYear(),
                game.getMinPlayers(),
                game.getMaxPlayers(),
                game.getPlayingTimeMinutes(),
                game.getMinAge(),
                game.getMechanics(),
                game.getCategories(),
                game.getThemes(),
                game.getDesigners(),
                game.getArtists(),
                game.getLink(),
                game.getDescription());
    }

    private static List<String> mapMechanics(List<LudopediaMechanic> items) {
        return items == null ? List.of() : items.stream().map(LudopediaMechanic::nome).toList();
    }

    private static List<String> mapCategories(List<LudopediaCategory> items) {
        return items == null ? List.of() : items.stream().map(LudopediaCategory::nome).toList();
    }

    private static List<String> mapThemes(List<LudopediaTheme> items) {
        return items == null ? List.of() : items.stream().map(LudopediaTheme::nome).toList();
    }

    private static List<String> mapProfessionals(List<LudopediaProfessional> items) {
        return items == null ? List.of() : items.stream().map(LudopediaProfessional::nome).toList();
    }
}
