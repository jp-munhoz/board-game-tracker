package br.com.bg.collection.web.dto;

import jakarta.validation.constraints.Size;

public record SessionNoteRequest(@Size(max = 2000) String text) {
}
