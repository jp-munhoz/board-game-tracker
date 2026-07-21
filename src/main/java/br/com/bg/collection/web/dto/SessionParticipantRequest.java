package br.com.bg.collection.web.dto;

import jakarta.validation.constraints.NotBlank;

public record SessionParticipantRequest(@NotBlank String username) {
}
