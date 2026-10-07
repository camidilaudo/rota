package com.rota.service.impl;

import com.rota.dto.request.UsuarioRequestDTO;
import com.rota.dto.response.UsuarioResponseDTO;
import com.rota.entity.Comercio;
import com.rota.entity.Usuario;
import com.rota.exception.BadRequestException;
import com.rota.repository.UsuarioRepository;
import com.rota.security.SeguridadUtils;
import com.rota.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UsuarioResponseDTO crear(UsuarioRequestDTO dto) {
        String email = dto.getEmail().trim();
        if (usuarioRepository.existsByEmail(email)) {
            throw new BadRequestException("Ya existe un usuario registrado con el email: " + email);
        }

        // El nuevo usuario queda asociado al comercio del dueño que lo crea
        Comercio comercio = usuarioRepository.findByEmail(SeguridadUtils.obtenerEmailAutenticado())
                .map(Usuario::getComercio)
                .orElse(null);

        Usuario usuario = Usuario.builder()
                .nombre(dto.getNombre().trim())
                .email(email)
                .password(passwordEncoder.encode(dto.getPassword()))
                .rol(dto.getRol())
                .comercio(comercio)
                .build();

        return mapToDTO(usuarioRepository.save(usuario));
    }

    private UsuarioResponseDTO mapToDTO(Usuario u) {
        return UsuarioResponseDTO.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .email(u.getEmail())
                .rol(u.getRol())
                .comercioId(u.getComercio() != null ? u.getComercio().getId() : null)
                .build();
    }
}
