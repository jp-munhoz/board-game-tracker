package br.com.bg.security.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(min = 2, max = 80) String displayName,
        @NotBlank @Size(min = 8, max = 100)
        @Pattern(regexp = ".*[A-Z].*", message = "A senha deve ter ao menos uma letra maiuscula")
        @Pattern(regexp = ".*[^A-Za-z0-9].*", message = "A senha deve ter ao menos um caractere especial")
        String password) {
}
