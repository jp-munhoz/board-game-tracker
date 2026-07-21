package br.com.bg.ludopedia.client.scraper;

import br.com.bg.ludopedia.config.LudopediaProperties;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A LudoAPI nao permite filtrar jogos por categoria (verificado na doc oficial),
 * entao o ranking por categoria e obtido via scrape da pagina publica de ranking da Ludopedia.
 */
@Component
public class LudopediaRankingScraper {

    private static final Logger log = LoggerFactory.getLogger(LudopediaRankingScraper.class);
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; board-game-tracker/1.0)";
    private static final String RANKING_URL = "https://ludopedia.com.br/ranking";
    private static final String ITEM_SELECTOR = "div.main-panel-body div.pad-top > div.media";
    private static final Pattern PAGE_PATTERN = Pattern.compile("pagina=(\\d+)");
    private static final int MAX_PAGES = 10;

    private final int timeoutMillis;

    public LudopediaRankingScraper(LudopediaProperties properties) {
        this.timeoutMillis = (int) properties.scrapeTimeout().toMillis();
    }

    public List<RankingGame> fetchByCategory(int idCategoria) {
        Map<Long, RankingGame> games = new LinkedHashMap<>();
        int page = 1;
        int lastPage = 1;
        do {
            Document doc = fetchPage(idCategoria, page);
            if (doc == null) {
                break;
            }
            for (RankingGame game : parseGames(doc)) {
                games.putIfAbsent(game.idJogo(), game);
            }
            lastPage = Math.max(lastPage, extractLastPage(doc));
            page++;
        } while (page <= lastPage && page <= MAX_PAGES);
        return List.copyOf(games.values());
    }

    private Document fetchPage(int idCategoria, int page) {
        String url = RANKING_URL + "?tipo=bg&id_categoria=" + idCategoria + (page > 1 ? "&pagina=" + page : "");
        try {
            return Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(timeoutMillis)
                    .get();
        } catch (IOException e) {
            log.warn("Falha ao obter ranking da categoria {} (pagina {}): {}", idCategoria, page, e.getMessage());
            return null;
        }
    }

    private static List<RankingGame> parseGames(Document doc) {
        List<RankingGame> games = new ArrayList<>();
        for (Element item : doc.select(ITEM_SELECTOR)) {
            Long idJogo = parseId(item);
            if (idJogo == null) {
                continue;
            }
            Element nameLink = item.selectFirst("h4.media-heading > a");
            Element thumbImg = item.selectFirst("div.media-left img.img-capa");
            games.add(new RankingGame(
                    idJogo,
                    nameLink != null ? nameLink.text() : null,
                    parseYear(item),
                    thumbImg != null ? thumbImg.attr("src") : null,
                    nameLink != null ? nameLink.attr("href") : null,
                    parseRank(item)));
        }
        return games;
    }

    private static Long parseId(Element item) {
        Element rankBtn = item.selectFirst("a.btn-rank-jogo[data-id_jogo]");
        if (rankBtn == null) {
            return null;
        }
        try {
            return Long.parseLong(rankBtn.attr("data-id_jogo"));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer parseYear(Element item) {
        Element yearEl = item.selectFirst("h4.media-heading small i");
        if (yearEl == null) {
            return null;
        }
        String digits = yearEl.text().replaceAll("[^0-9]", "");
        return digits.isBlank() ? null : Integer.parseInt(digits);
    }

    private static Integer parseRank(Element item) {
        Element rankEl = item.selectFirst("h4.media-heading span.rank");
        if (rankEl == null) {
            return null;
        }
        String digits = rankEl.text().replaceAll("[^0-9]", "");
        return digits.isBlank() ? null : Integer.parseInt(digits);
    }

    private static int extractLastPage(Document doc) {
        int last = 1;
        for (Element link : doc.select("ul.pagination a[href*=pagina=]")) {
            Matcher matcher = PAGE_PATTERN.matcher(link.attr("href"));
            if (matcher.find()) {
                last = Math.max(last, Integer.parseInt(matcher.group(1)));
            }
        }
        return last;
    }
}
