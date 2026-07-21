package br.com.bg.collection.web.dto;

import jakarta.validation.constraints.Positive;

public record CollectionGameRequest(@Positive long gameId) {
}
