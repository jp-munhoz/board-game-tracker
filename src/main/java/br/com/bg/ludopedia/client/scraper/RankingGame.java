package br.com.bg.ludopedia.client.scraper;

public record RankingGame(long idJogo, String nome, Integer ano, String thumb, String link, Integer rank) {
}
