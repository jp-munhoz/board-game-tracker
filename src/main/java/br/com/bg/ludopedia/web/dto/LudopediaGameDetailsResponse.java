package br.com.bg.ludopedia.web.dto;

import java.util.List;

public record LudopediaGameDetailsResponse(
        long id,
        String name,
        String imageUrl,
        Integer yearPublished,
        Integer nationalYear,
        Integer minPlayers,
        Integer maxPlayers,
        Integer playingTimeMinutes,
        Integer minAge,
        List<String> mechanics,
        List<String> categories,
        List<String> themes,
        List<String> designers,
        List<String> artists,
        String link,
        String description) {
}