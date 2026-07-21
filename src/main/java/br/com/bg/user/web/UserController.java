package br.com.bg.user.web;

import br.com.bg.user.domain.AppUser;
import br.com.bg.user.repository.AppUserRepository;
import br.com.bg.user.web.dto.UserSummaryResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private final AppUserRepository appUserRepository;

    public UserController(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @GetMapping
    public List<UserSummaryResponse> list() {
        return appUserRepository.findAll().stream()
                .map(this::toSummary)
                .toList();
    }

    private UserSummaryResponse toSummary(AppUser user) {
        return new UserSummaryResponse(user.getUsername(), user.getDisplayName());
    }
}
