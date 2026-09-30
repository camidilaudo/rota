package com.rota.service.impl;

import com.rota.dto.request.TrasladoLoteRequestDTO;
import com.rota.dto.response.AlertaRotacionResponseDTO;
import com.rota.dto.response.LoteResponseDTO;
import com.rota.entity.Lote;
import com.rota.entity.Ubicacion;
import com.rota.repository.LoteRepository;
import com.rota.service.RotacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class RotacionServiceImpl implements RotacionService {

    private final LoteRepository loteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LoteResponseDTO> obtenerLotesPorFefo(Long productoId) {
        return loteRepository.findByProductoIdAndCantidadGreaterThanOrderByFechaVencimientoAsc(productoId, 0)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LoteResponseDTO obtenerProximoLoteAReponer(Long productoId) {
        Lote lote = loteRepository.findFirstByProductoIdAndUbicacionAndCantidadGreaterThanOrderByFechaVencimientoAsc(
                productoId, Ubicacion.DEPOSITO, 0)
                .orElseThrow(() -> new RuntimeException("No existen lotes disponibles en depósito para este producto."));
        return mapToDTO(lote);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertaRotacionResponseDTO> evaluarAlertasRotacionIncorrecta() {
        LocalDate hoy = LocalDate.now();
        List<AlertaRotacionResponseDTO> alertas = new ArrayList<>();

        List<Lote> lotesGondola = loteRepository.findLotesActivosPorUbicacion(Ubicacion.GONDOLA);
        List<Lote> lotesDeposito = loteRepository.findLotesActivosPorUbicacion(Ubicacion.DEPOSITO);

        // Agrupar lotes en Depósito por ID de Producto
        Map<Long, List<Lote>> depositoMap = lotesDeposito.stream()
                .collect(Collectors.groupingBy(l -> l.getProducto().getId()));

        for (Lote loteGondola : lotesGondola) {
            Long productoId = loteGondola.getProducto().getId();

            if (depositoMap.containsKey(productoId)) {
                // El primer lote de la lista es el de vencimiento más próximo en depósito
                Lote loteDepositoCandidato = depositoMap.get(productoId).get(0);

                // HU-11: Si Depósito vence ANTES que Góndola -> Inconsistencia de rotación
                if (loteDepositoCandidato.getFechaVencimiento().isBefore(loteGondola.getFechaVencimiento())) {
                    long diasDif = ChronoUnit.DAYS.between(
                            loteDepositoCandidato.getFechaVencimiento(),
                            loteGondola.getFechaVencimiento()
                    );

                    int diasGondola = (int) ChronoUnit.DAYS.between(hoy, loteGondola.getFechaVencimiento());
                    int diasDeposito = (int) ChronoUnit.DAYS.between(hoy, loteDepositoCandidato.getFechaVencimiento());

                    alertas.add(AlertaRotacionResponseDTO.builder()
                            .productoId(productoId)
                            .productoNombre(loteGondola.getProducto().getNombre())
                            .productoCodigoBarra(loteGondola.getProducto().getCodigoBarra())
                            .loteGondolaId(loteGondola.getId())
                            .fechaVencimientoGondola(loteGondola.getFechaVencimiento())
                            .diasHastaVencimientoGondola(diasGondola)
                            .loteDepositoId(loteDepositoCandidato.getId())
                            .fechaVencimientoDeposito(loteDepositoCandidato.getFechaVencimiento())
                            .diasHastaVencimientoDeposito(diasDeposito)
                            .diasDiferencia((int) diasDif)
                            .recomendacionAccion(String.format(
                                    "Error de rotación: El lote #%d en DEPÓSITO vence %d día(s) antes que el lote #%d expuesto en GÓNDOLA. Trasladar a frente de góndola.",
                                    loteDepositoCandidato.getId(), diasDif, loteGondola.getId()))
                            .build());
                }
            }
        }

        return alertas;
    }

    @Override
    @Transactional
    public LoteResponseDTO trasladarLoteAGondola(TrasladoLoteRequestDTO request) {
        Lote loteOrigen = loteRepository.findById(request.getLoteOrigenId())
                .orElseThrow(() -> new RuntimeException("No se encontró el lote especificado."));

        if (loteOrigen.getUbicacion() != Ubicacion.DEPOSITO) {
            throw new IllegalStateException("Solo se pueden trasladar lotes que se encuentren actualmente en DEPÓSITO.");
        }

        if (request.getCantidadATrasladar() > loteOrigen.getCantidad()) {
            throw new IllegalArgumentException(String.format(
                    "Cantidad insuficiente. El lote tiene %d unidades y se intentaron trasladar %d.",
                    loteOrigen.getCantidad(), request.getCantidadATrasladar()));
        }

        Lote loteDestino;

        // Caso 1: Traslado TOTAL de la mercadería
        if (request.getCantidadATrasladar().equals(loteOrigen.getCantidad())) {
            loteOrigen.setUbicacion(Ubicacion.GONDOLA);
            loteDestino = loteRepository.save(loteOrigen);
        } 
        // Caso 2: Traslado PARCIAL (se fracciona el lote)
        else {
            // Reducimos la cantidad en el lote de depósito
            loteOrigen.setCantidad(loteOrigen.getCantidad() - request.getCantidadATrasladar());
            loteRepository.save(loteOrigen);

            // Creamos la nueva entrada en góndola respetando la misma fecha de vencimiento y recepción
            Lote nuevoLoteGondola = Lote.builder()
                    .producto(loteOrigen.getProducto())
                    .cantidad(request.getCantidadATrasladar())
                    .fechaVencimiento(loteOrigen.getFechaVencimiento())
                    .fechaRecepcion(loteOrigen.getFechaRecepcion())
                    .ubicacion(Ubicacion.GONDOLA)
                    .build();

            loteDestino = loteRepository.save(nuevoLoteGondola);
        }

        return mapToDTO(loteDestino);
    }

    private LoteResponseDTO mapToDTO(Lote lote) {
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