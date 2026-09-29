package com.rota.service.impl;

import com.rota.dto.request.AplicarDescuentoRequestDTO;
import com.rota.dto.request.RegistrarMermaRequestDTO;
import com.rota.dto.response.DescuentoAplicadoResponseDTO;
import com.rota.dto.response.MermaResponseDTO;
import com.rota.entity.Lote;
import com.rota.entity.Merma;
import com.rota.entity.Producto;
import com.rota.exception.ResourceNotFoundException;
import com.rota.repository.LoteRepository;
import com.rota.repository.MermaRepository;
import com.rota.service.OperacionStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperacionStockServiceImpl implements OperacionStockService {

    private final LoteRepository loteRepository;
    private final MermaRepository mermaRepository;

    @Override
    @Transactional
    public DescuentoAplicadoResponseDTO aplicarDescuento(AplicarDescuentoRequestDTO dto) {
        Lote lote = loteRepository.findById(dto.getLoteId())
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado con ID: " + dto.getLoteId()));

        lote.setPorcentajeDescuento(dto.getPorcentajeDescuento());
        loteRepository.save(lote);

        BigDecimal precioOriginal = lote.getProducto().getPrecioVenta();
        BigDecimal factorDescuento = BigDecimal.ONE.subtract(
                dto.getPorcentajeDescuento().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
        );
        BigDecimal precioConDescuento = precioOriginal.multiply(factorDescuento).setScale(2, RoundingMode.HALF_UP);

        return DescuentoAplicadoResponseDTO.builder()
                .loteId(lote.getId())
                .productoNombre(lote.getProducto().getNombre())
                .precioOriginal(precioOriginal)
                .porcentajeDescuento(dto.getPorcentajeDescuento())
                .precioConDescuento(precioConDescuento)
                .mensaje("Descuento del " + dto.getPorcentajeDescuento() + "% aplicado correctamente al lote")
                .build();
    }

    @Override
    @Transactional
    public MermaResponseDTO registrarMerma(RegistrarMermaRequestDTO dto) {
        Lote lote = loteRepository.findById(dto.getLoteId())
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado con ID: " + dto.getLoteId()));

        if (dto.getCantidad() > lote.getCantidad()) {
            throw new IllegalArgumentException("La cantidad a mermar (" + dto.getCantidad() +
                    ") supera el stock disponible en el lote (" + lote.getCantidad() + ")");
        }

        Producto producto = lote.getProducto();
        BigDecimal costoUnitario = producto.getCosto();
        BigDecimal perdidaTotal = costoUnitario.multiply(BigDecimal.valueOf(dto.getCantidad()));

        // Descontamos las unidades del lote
        lote.setCantidad(lote.getCantidad() - dto.getCantidad());
        loteRepository.save(lote);

        // Registramos la merma monetaria
        Merma merma = Merma.builder()
                .producto(producto)
                .cantidad(dto.getCantidad())
                .motivo(dto.getMotivo().trim())
                .costoUnitario(costoUnitario)
                .perdidaTotal(perdidaTotal)
                .build();

        Merma mermaGuardada = mermaRepository.save(merma);

        return mapToDTO(mermaGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MermaResponseDTO> obtenerHistorialMermas() {
        return mermaRepository.findAllByOrderByFechaRegistroDesc()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private MermaResponseDTO mapToDTO(Merma m) {
        return MermaResponseDTO.builder()
                .id(m.getId())
                .productoId(m.getProducto().getId())
                .productoNombre(m.getProducto().getNombre())
                .productoCodigoBarra(m.getProducto().getCodigoBarra())
                .cantidadMermada(m.getCantidad())
                .motivo(m.getMotivo())
                .costoUnitario(m.getCostoUnitario())
                .perdidaTotal(m.getPerdidaTotal())
                .fechaRegistro(m.getFechaRegistro())
                .build();
    }
}