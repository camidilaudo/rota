package com.rota.service.impl;

import com.rota.dto.request.AuthRequestDTO;
import com.rota.dto.response.AuthResponseDTO;
import com.rota.entity.Usuario;
import com.rota.repository.UsuarioRepository;
import com.rota.security.JwtService;
import com.rota.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponseDTO login(AuthRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        String jwtToken = jwtService.generateToken(usuario);

        return AuthResponseDTO.builder()
                .token(jwtToken)
                .email(usuario.getEmail())
                .rol(usuario.getRol().name())
                .build();
    }
}