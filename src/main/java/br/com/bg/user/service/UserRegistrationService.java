package br.com.bg.user.service;

import br.com.bg.security.web.dto.RegisterRequest;
import br.com.bg.user.domain.AppUser;
import br.com.bg.user.exception.UsernameAlreadyExistsException;
import br.com.bg.user.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRegistrationService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(RegisterRequest request) {
        String username = request.username().trim();
        if (appUserRepository.findByUsername(username).isPresent()) {
            throw new UsernameAlreadyExistsException("Usuario ja cadastrado: " + username);
        }

        AppUser user = new AppUser(username, request.displayName().trim(), passwordEncoder.encode(request.password()));
        return appUserRepository.save(user);
    }
}
