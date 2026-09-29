package com.rota.service.impl;

import com.rota.dto.response.LoteRiesgoResponseDTO;
import com.rota.dto.response.RutaDiariaResponseDTO;
import com.rota.entity.EstadoRiesgo;
import com.rota.entity.Lote;
import com.rota.repository.LoteRepository;
import com.rota.service.MotorRiesgoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
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
                .sorted(Comparator.comparing(LoteRiesgoResponseDTO::getUbicacion)
                        .thenComparing(LoteRiesgoResponseDTO::getFechaVencimiento))
                .toList();

        long vencidos = priorizados.stream()
                .filter(l -> l.getEstadoRiesgo() == EstadoRiesgo.CRITICO_VENCIDO)
                .count();

        BigDecimal valorTotal = priorizados.stream()
                .map(LoteRiesgoResponseDTO::getValorEnRiesgo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return RutaDiariaResponseDTO.builder()
                .totalLotesAtencion(priorizados.size())
                .totalLotesVencidos((int) vencidos)
                .valorTotalEnRiesgo(valorTotal)
                .lotesPriorizados(priorizados)
                .build();
    }

    private LoteRiesgoResponseDTO evaluarLote(Lote lote) {
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), lote.getFechaVencimiento());
        int diasRestantes = (int) dias;
        int umbral = lote.getProducto().getCategoria().getDiasUmbralCritico();

        EstadoRiesgo estado;
        if (diasRestantes <= 0) {
            estado = EstadoRiesgo.CRITICO_VENCIDO;
        } else if (diasRestantes <= umbral) {
            estado = EstadoRiesgo.ROJO;
        } else if (diasRestantes <= Math.ceil(umbral * 1.5)) {
            estado = EstadoRiesgo.AMARILLO;
        } else {
            estado = EstadoRiesgo.VERDE;
        }

        BigDecimal costoUnitario = lote.getProducto().getCosto();
        BigDecimal valorRiesgo = costoUnitario.multiply(BigDecimal.valueOf(lote.getCantidad()));

        return LoteRiesgoResponseDTO.builder()
                .loteId(lote.getId())
                .productoId(lote.getProducto().getId())
                .productoCodigoBarra(lote.getProducto().getCodigoBarra())
                .productoNombre(lote.getProducto().getNombre())
                .categoriaNombre(lote.getProducto().getCategoria().getNombre())
                .cantidad(lote.getCantidad())
                .fechaVencimiento(lote.getFechaVencimiento())
                .ubicacion(lote.getUbicacion())
                .diasHastaVencimiento(diasRestantes)
                .diasUmbralCritico(umbral)
                .estadoRiesgo(estado)
                .modoNotificacion(lote.getProducto().getCategoria().getModoNotificacion())
                .valorEnRiesgo(valorRiesgo)
                .build();
    }
}