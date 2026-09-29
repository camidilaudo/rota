package com.rota.service.impl;

import com.rota.dto.request.CategoriaRequestDTO;
import com.rota.dto.response.CategoriaResponseDTO;
import com.rota.entity.Categoria;
import com.rota.exception.ResourceNotFoundException;

import com.rota.repository.CategoriaRepository;
import com.rota.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional
    public CategoriaResponseDTO crear(CategoriaRequestDTO dto) {
        categoriaRepository.findByNombreIgnoreCase(dto.getNombre()).ifPresent(c -> {
            throw new IllegalArgumentException("Ya existe una categoría registrada con el nombre: " + dto.getNombre());
        });

        Categoria categoria = Categoria.builder()
                .nombre(dto.getNombre().trim())
                .diasUmbralCritico(dto.getDiasUmbralCritico())
                .modoNotificacion(dto.getModoNotificacion())
                .esPiloto(dto.getEsPiloto())
                .activa(true)
                .build();

        return mapToDTO(categoriaRepository.save(categoria));
    }

    @Override
    @Transactional
    public CategoriaResponseDTO actualizarUmbralYNotificacion(Long id, Integer diasUmbral, String modoNotificacion) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));

        if (diasUmbral != null) {
            categoria.setDiasUmbralCritico(diasUmbral);
        }
        if (modoNotificacion != null) {
            categoria.setModoNotificacion(com.rota.entity.ModoNotificacion.valueOf(modoNotificacion.toUpperCase()));
        }

        return mapToDTO(categoriaRepository.save(categoria));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> obtenerTodas() {
        return categoriaRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    private CategoriaResponseDTO mapToDTO(Categoria c) {
        return CategoriaResponseDTO.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .diasUmbralCritico(c.getDiasUmbralCritico())
                .modoNotificacion(c.getModoNotificacion())
                .esPiloto(c.getEsPiloto())
                .activa(c.getActiva())
                .build();
    }
}