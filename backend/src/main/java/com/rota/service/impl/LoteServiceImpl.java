package com.rota.service.impl;

import com.rota.dto.request.LoteRequestDTO;
import com.rota.dto.response.LoteResponseDTO;
import com.rota.entity.Lote;
import com.rota.entity.Producto;
import com.rota.entity.Ubicacion;
import com.rota.exception.ResourceNotFoundException;
import com.rota.repository.LoteRepository;
import com.rota.repository.ProductoRepository;
import com.rota.service.LoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class LoteServiceImpl implements LoteService {

    private final LoteRepository loteRepository;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional
    public LoteResponseDTO registrarLote(LoteRequestDTO dto) {
        Producto producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + dto.getProductoId()));

        Lote lote = Lote.builder()
                .producto(producto)
                .cantidad(dto.getCantidad())
                .fechaVencimiento(dto.getFechaVencimiento())
                .ubicacion(dto.getUbicacion())
                .build();

        return mapToDTO(loteRepository.save(lote));
    }

    @Override
    @Transactional
    public LoteResponseDTO actualizarUbicacion(Long loteId, Ubicacion nuevaUbicacion) {
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado con ID: " + loteId));

        lote.setUbicacion(nuevaUbicacion);
        return mapToDTO(loteRepository.save(lote));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoteResponseDTO> obtenerLotesPorFEFO(Long productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new ResourceNotFoundException("Producto no encontrado con ID: " + productoId);
        }
        return loteRepository.findByProductoIdOrderByFechaVencimientoAsc(productoId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private LoteResponseDTO mapToDTO(Lote lote) {
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), lote.getFechaVencimiento());
        return LoteResponseDTO.builder()
                .id(lote.getId())
                .productoId(lote.getProducto().getId())
                .productoCodigoBarra(lote.getProducto().getCodigoBarra())
                .productoNombre(lote.getProducto().getNombre())
                .cantidad(lote.getCantidad())
                .fechaVencimiento(lote.getFechaVencimiento())
                .fechaRecepcion(lote.getFechaRecepcion())
                .ubicacion(lote.getUbicacion())
                .diasHastaVencimiento((int) dias)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoteResponseDTO> buscarLotesPorFiltro(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            return List.of(); // O puedes retornar una lista vacía
        }
        List<Lote> lotes = loteRepository.buscarLotesActivosPorProductoFiltro(filtro.trim());
        return lotes.stream().map(this::mapToDTO).toList();
    }
}