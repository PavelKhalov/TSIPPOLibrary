package ru.khalov.tsippolibrary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.khalov.tsippolibrary.dto.request.LoginRequest;
import ru.khalov.tsippolibrary.dto.request.RegisterRequest;
import ru.khalov.tsippolibrary.dto.response.AuthResponse;
import ru.khalov.tsippolibrary.entity.Role;
import ru.khalov.tsippolibrary.entity.User;
import ru.khalov.tsippolibrary.repository.UserRepository;
import ru.khalov.tsippolibrary.util.expeption.UsernameTakenException;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuthResponse register(RegisterRequest request){
        if(userRepository.existsByUsername(request.username())){
            throw new UsernameTakenException("This username already taken");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(Role.USER));

        userRepository.save(user);

        return new AuthResponse(jwtService.generateToken(user));
    }


    public AuthResponse login(LoginRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        User user = userRepository.findByUsername(request.username()).orElseThrow(()->
                new UsernameNotFoundException("This username not found"));

        return new AuthResponse(jwtService.generateToken(user));
    }
}
