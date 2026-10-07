package com.rota.service.impl;

import com.rota.dto.request.ComercioRequestDTO;
import com.rota.dto.response.CategoriaResponseDTO;
import com.rota.dto.response.ComercioResponseDTO;
import com.rota.entity.Categoria;
import com.rota.entity.Comercio;
import com.rota.entity.Rol;
import com.rota.entity.Usuario;
import com.rota.exception.BadRequestException;
import com.rota.exception.ResourceNotFoundException;
import com.rota.repository.CategoriaRepository;
import com.rota.repository.ComercioRepository;
import com.rota.repository.UsuarioRepository;
import com.rota.security.SeguridadUtils;
import com.rota.service.ComercioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ComercioServiceImpl implements ComercioService {

    private final ComercioRepository comercioRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public ComercioResponseDTO crear(ComercioRequestDTO dto) {
        Usuario dueno = usuarioRepository.findByEmail(SeguridadUtils.obtenerEmailAutenticado())
                .orElseThrow(() -> new BadRequestException("No se pudo identificar al usuario autenticado"));

        if (dueno.getComercio() != null) {
            throw new BadRequestException("El usuario ya está asociado al comercio: " + dueno.getComercio().getNombre());
        }

        // Se eliminan IDs repetidos manteniendo el orden enviado
        List<Long> idsPiloto = List.copyOf(new LinkedHashSet<>(dto.getCategoriasPilotoIds()));
        List<Categoria> categoriasPiloto = categoriaRepository.findAllById(idsPiloto);
        if (categoriasPiloto.size() != idsPiloto.size()) {
            throw new ResourceNotFoundException("Una o más categorías piloto no existen: " + idsPiloto);
        }
        categoriasPiloto.stream()
                .filter(c -> c.getComercio() != null)
                .findFirst()
                .ifPresent(c -> {
                    throw new BadRequestException("La categoría '" + c.getNombre() + "' ya pertenece a otro comercio");
                });

        Comercio comercio = comercioRepository.save(Comercio.builder()
                .nombre(dto.getNombre().trim())
                .cuit(dto.getCuit() != null ? dto.getCuit().trim() : null)
                .direccion(dto.getDireccion() != null ? dto.getDireccion().trim() : null)
                .activo(true)
                .build());

        // HU-01: las categorías seleccionadas quedan como piloto y activas dentro del comercio
        categoriasPiloto.forEach(c -> {
            c.setComercio(comercio);
            c.setEsPiloto(true);
            c.setActiva(true);
        });
        categoriaRepository.saveAll(categoriasPiloto);

        // HU-01: el dueño queda asociado a su comercio para operar al iniciar sesión
        dueno.setComercio(comercio);
        usuarioRepository.save(dueno);

        return mapToDTO(comercio, dueno.getEmail(), categoriasPiloto);
    }

    @Override
    @Transactional(readOnly = true)
    public ComercioResponseDTO obtenerPorId(Long id) {
        Comercio comercio = comercioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comercio no encontrado con ID: " + id));

        String duenoEmail = usuarioRepository.findFirstByComercioIdAndRol(id, Rol.ROLE_DUENO)
                .map(Usuario::getEmail)
                .orElse(null);

        return mapToDTO(comercio, duenoEmail, categoriaRepository.findByComercioIdAndEsPilotoTrue(id));
    }

    private ComercioResponseDTO mapToDTO(Comercio comercio, String duenoEmail, List<Categoria> categoriasPiloto) {
        return ComercioResponseDTO.builder()
                .id(comercio.getId())
                .nombre(comercio.getNombre())
                .cuit(comercio.getCuit())
                .direccion(comercio.getDireccion())
                .activo(comercio.getActivo())
                .fechaAlta(comercio.getFechaAlta())
                .duenoEmail(duenoEmail)
                .categoriasPiloto(categoriasPiloto.stream().map(this::mapCategoriaToDTO).toList())
                .build();
    }

    private CategoriaResponseDTO mapCategoriaToDTO(Categoria c) {
        return CategoriaResponseDTO.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .diasUmbralAlerta(c.getDiasUmbralAlerta())
                .diasUmbralCritico(c.getDiasUmbralCritico())
                .modoNotificacion(c.getModoNotificacion())
                .esPiloto(c.getEsPiloto())
                .activa(c.getActiva())
                .build();
    }
}
