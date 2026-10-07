package com.rota.service.impl;

import com.rota.dto.response.LoteRiesgoResponseDTO;
import com.rota.dto.response.RutaDiariaResponseDTO;
import com.rota.entity.Categoria;
import com.rota.entity.EstadoRiesgo;
import com.rota.entity.Lote;
import com.rota.entity.Producto;
import com.rota.repository.LoteRepository;
import com.rota.security.SeguridadUtils;
import com.rota.service.MotorRiesgoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class MotorRiesgoServiceImpl implements MotorRiesgoService {

    private final LoteRepository loteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LoteRiesgoResponseDTO> evaluarTodosLosLotes() {
        return loteRepository.findAllLotesConProductoYCategoria()
                .stream()
                .map(this::evaluarLote)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RutaDiariaResponseDTO obtenerRutaDiariaPriorizada() {
        List<LoteRiesgoResponseDTO> todos = evaluarTodosLosLotes();

        // Filtramos solo los lotes que requieren acción inmediata (Rojo o Crítico/Vencido)
        List<LoteRiesgoResponseDTO> priorizados = todos.stream()
                .filter(l -> l.getEstadoRiesgo() == EstadoRiesgo.ROJO || l.getEstadoRiesgo() == EstadoRiesgo.CRITICO_VENCIDO)
                .sorted(Comparator.comparing(LoteRiesgoResponseDTO::getUbicacion, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(LoteRiesgoResponseDTO::getFechaVencimiento, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        long vencidos = priorizados.stream()
                .filter(l -> l.getEstadoRiesgo() == EstadoRiesgo.CRITICO_VENCIDO)
                .count();

        BigDecimal valorTotal = priorizados.stream()
                .map(LoteRiesgoResponseDTO::getValorEnRiesgo)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return RutaDiariaResponseDTO.builder()
                .totalLotesAtencion(priorizados.size())
                .totalLotesVencidos((int) vencidos)
                // HU-05: el usuario operativo no visualiza el valor en riesgo
                .valorTotalEnRiesgo(SeguridadUtils.esRepositor() ? null : valorTotal)
                .lotesPriorizados(priorizados)
                .build();
    }

    private LoteRiesgoResponseDTO evaluarLote(Lote lote) {
        Producto producto = lote.getProducto();
        Categoria categoria = (producto != null) ? producto.getCategoria() : null;

        // 1. Manejo seguro de fecha de vencimiento y cálculo de días
        LocalDate fechaVencimiento = Optional.ofNullable(lote.getFechaVencimiento()).orElse(LocalDate.now());
        int diasRestantes = (int) ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);

        // 2. Manejo seguro del umbral (si es null o la categoría es null, se usa 7 por defecto)
        int umbral = (categoria != null && categoria.getDiasUmbralCritico() != null)
                ? categoria.getDiasUmbralCritico()
                : 7;

        // 3. Evaluación del estado de riesgo
        EstadoRiesgo estado;
        if (diasRestantes <= 0) {
            estado = EstadoRiesgo.CRITICO_VENCIDO;
        } else if (diasRestantes <= umbral) {
            estado = EstadoRiesgo.ROJO;
        } else if (diasRestantes <= (int) Math.ceil(umbral * 1.5)) {
            estado = EstadoRiesgo.AMARILLO;
        } else {
            estado = EstadoRiesgo.VERDE;
        }

        // 4. Manejo seguro del costo y valor en riesgo
        BigDecimal costoUnitario = (producto != null && producto.getCosto() != null) 
                ? producto.getCosto() 
                : BigDecimal.ZERO;
        
        int cantidad = Optional.ofNullable(lote.getCantidad()).orElse(0);
        BigDecimal valorRiesgo = costoUnitario.multiply(BigDecimal.valueOf(cantidad));

        return LoteRiesgoResponseDTO.builder()
                .loteId(lote.getId())
                .productoId(producto != null ? producto.getId() : null)
                .productoCodigoBarra(producto != null ? producto.getCodigoBarra() : null)
                .productoNombre(producto != null ? producto.getNombre() : null)
                .categoriaNombre(categoria != null ? categoria.getNombre() : null)
                .cantidad(cantidad)
                .fechaVencimiento(lote.getFechaVencimiento())
                .ubicacion(lote.getUbicacion())
                .diasHastaVencimiento(diasRestantes)
                .diasUmbralCritico(umbral)
                .estadoRiesgo(estado)
                .modoNotificacion(categoria != null ? categoria.getModoNotificacion() : null)
                .valorEnRiesgo(SeguridadUtils.esRepositor() ? null : valorRiesgo)
                .build();
    }
}