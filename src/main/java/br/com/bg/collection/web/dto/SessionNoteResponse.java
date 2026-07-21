package br.com.bg.collection.web.dto;

import java.time.Instant;

public record SessionNoteResponse(String authorUsername, String authorDisplayName, String text, Instant updatedAt) {
}
