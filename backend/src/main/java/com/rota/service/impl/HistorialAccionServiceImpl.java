package com.rota.service.impl;

import com.rota.dto.response.HistorialAccionResponseDTO;
import com.rota.entity.HistorialAccion;
import com.rota.entity.Lote;
import com.rota.entity.TipoAccion;
import com.rota.entity.Usuario;
import com.rota.repository.HistorialAccionRepository;
import com.rota.service.HistorialAccionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistorialAccionServiceImpl implements HistorialAccionService {

    private final HistorialAccionRepository historialAccionRepository;

    @Override
    @Transactional
    public void registrarAccion(Lote lote, TipoAccion tipoAccion, String motivo, Usuario usuario) {
        historialAccionRepository.save(HistorialAccion.builder()
                .lote(lote)
                .tipoAccion(tipoAccion)
                .motivo(motivo)
                .usuario(usuario)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialAccionResponseDTO> obtenerHistorialPorLote(Long loteId) {
        return historialAccionRepository.findByLoteIdOrderByFechaHoraDesc(loteId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private HistorialAccionResponseDTO mapToDTO(HistorialAccion h) {
        return HistorialAccionResponseDTO.builder()
                .id(h.getId())
                .loteId(h.getLote().getId())
                .tipoAccion(h.getTipoAccion())
                .motivo(h.getMotivo())
                .usuarioNombre(h.getUsuario().getNombre())
                .usuarioEmail(h.getUsuario().getEmail())
                .fechaHora(h.getFechaHora())
                .build();
    }
}
