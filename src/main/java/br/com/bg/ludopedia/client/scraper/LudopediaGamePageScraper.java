package br.com.bg.ludopedia.client.scraper;

import br.com.bg.ludopedia.config.LudopediaProperties;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

/**
 * A LudoAPI nao expoe a descricao do jogo em nenhum endpoint (verificado na doc oficial),
 * entao ela e obtida via scrape da propria pagina publica do jogo (campo "link" da ficha).
 */
@Component
public class LudopediaGamePageScraper {

    private static final Logger log = LoggerFactory.getLogger(LudopediaGamePageScraper.class);
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; board-game-tracker/1.0)";
    private static final String DESCRIPTION_SELECTOR = "#bloco-descricao-sm .col-sm-9 p";

    private final int timeoutMillis;

    public LudopediaGamePageScraper(LudopediaProperties properties) {
        this.timeoutMillis = (int) properties.scrapeTimeout().toMillis();
    }

    public Optional<String> fetchDescription(String gamePageUrl) {
        if (gamePageUrl == null || gamePageUrl.isBlank()) {
            return Optional.empty();
        }
        try {
            Document doc = Jsoup.connect(gamePageUrl)
                    .userAgent(USER_AGENT)
                    .timeout(timeoutMillis)
                    .get();
            Element description = doc.selectFirst(DESCRIPTION_SELECTOR);
            return description == null ? Optional.empty() : Optional.of(extractText(description));
        } catch (IOException e) {
            log.warn("Falha ao obter descricao da pagina do jogo ({}): {}", gamePageUrl, e.getMessage());
            return Optional.empty();
        }
    }

    private static String extractText(Element description) {
        String withLineBreaks = description.html().replaceAll("(?i)<br\\s*/?>", "\n");
        String plainText = Parser.unescapeEntities(withLineBreaks.replaceAll("<[^>]+>", ""), false);
        return plainText.replaceAll("\n{3,}", "\n\n").trim();
    }
}
