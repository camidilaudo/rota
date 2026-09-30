package com.rota.service.impl;

import com.rota.dto.response.BusquedaProductoLotesResponseDTO;
import com.rota.dto.response.LoteResponseDTO;
import com.rota.entity.Lote;
import com.rota.entity.Producto;
import com.rota.entity.Ubicacion;
import com.rota.repository.LoteRepository;
import com.rota.repository.ProductoRepository;
import com.rota.service.BusquedaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class BusquedaServiceImpl implements BusquedaService {

    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<BusquedaProductoLotesResponseDTO> buscarProductosConLotes(String query, Ubicacion ubicacion, Pageable pageable) {
        Page<Producto> productosPage = productoRepository.buscarPorNombreOCodigo(query, pageable);

        List<BusquedaProductoLotesResponseDTO> contenido = productosPage.getContent().stream()
                .map(this::construirProductoDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(contenido, pageable, productosPage.getTotalElements());
    }

    private BusquedaProductoLotesResponseDTO construirProductoDTO(Producto producto) {
        List<Lote> todosLosLotes = loteRepository.findByProductoIdOrderByFechaVencimientoAsc(producto.getId());

        List<LoteResponseDTO> lotesDTO = todosLosLotes.stream()
                .filter(l -> l.getCantidad() > 0)
                .map(this::mapToLoteDTO)
                .collect(Collectors.toList());

        // Se usa lambda explícita para evitar el aviso de conversiones nulas en mapToInt
        int stockTotal = lotesDTO.stream()
                .mapToInt(dto -> dto.getCantidad())
                .sum();

        return BusquedaProductoLotesResponseDTO.builder()
                .productoId(producto.getId())
                .codigoBarra(producto.getCodigoBarra())
                .nombreProducto(producto.getNombre())
                .categoriaNombre(producto.getCategoria().getNombre())
                .stockTotal(stockTotal)
                .lotes(lotesDTO)
                .build();
    }

    private LoteResponseDTO mapToLoteDTO(Lote lote) {
        int dias = (int) ChronoUnit.DAYS.between(LocalDate.now(), lote.getFechaVencimiento());
        
        return LoteResponseDTO.builder()
                .id(lote.getId())
                .productoId(lote.getProducto().getId())
                .productoCodigoBarra(lote.getProducto().getCodigoBarra())
                .productoNombre(lote.getProducto().getNombre())
                .cantidad(lote.getCantidad())
                .fechaVencimiento(lote.getFechaVencimiento())
                .fechaRecepcion(lote.getFechaRecepcion())
                .ubicacion(lote.getUbicacion())
                .diasHastaVencimiento(dias)
                .build();
    }
}