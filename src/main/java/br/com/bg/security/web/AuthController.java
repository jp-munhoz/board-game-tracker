package br.com.bg.security.web;

import br.com.bg.security.AppUserDetails;
import br.com.bg.security.web.dto.CurrentUserResponse;
import br.com.bg.security.web.dto.LoginRequest;
import br.com.bg.security.web.dto.RegisterRequest;
import br.com.bg.user.domain.AppUser;
import br.com.bg.user.service.UserRegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRegistrationService userRegistrationService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AuthController(AuthenticationManager authenticationManager, UserRegistrationService userRegistrationService) {
        this.authenticationManager = authenticationManager;
        this.userRegistrationService = userRegistrationService;
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CurrentUserResponse register(@Valid @RequestBody RegisterRequest request,
                                         HttpServletRequest httpRequest,
                                         HttpServletResponse httpResponse) {
        AppUser user = userRegistrationService.register(request);
        AppUserDetails principal = authenticate(user.getUsername(), request.password(), httpRequest, httpResponse);
        return toResponse(principal);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CurrentUserResponse login(@Valid @RequestBody LoginRequest request,
                                      HttpServletRequest httpRequest,
                                      HttpServletResponse httpResponse) {
        AppUserDetails principal = authenticate(request.username(), request.password(), httpRequest, httpResponse);
        return toResponse(principal);
    }

    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal AppUserDetails principal) {
        return toResponse(principal);
    }

    private AppUserDetails authenticate(String username, String password,
                                         HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return (AppUserDetails) authentication.getPrincipal();
    }

    private CurrentUserResponse toResponse(AppUserDetails principal) {
        return new CurrentUserResponse(principal.getUsername(), principal.getDisplayName());
    }
}
