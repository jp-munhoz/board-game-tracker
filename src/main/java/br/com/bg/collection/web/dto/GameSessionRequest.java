package br.com.bg.collection.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record GameSessionRequest(@NotNull LocalDate playedAt, @Size(max = 2000) String notes) {
}
