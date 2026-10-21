package com.rota.service.impl;

import com.rota.repository.MovimientoStockRepository;
import com.rota.service.VelocidadVentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VelocidadVentaServiceImpl implements VelocidadVentaService {

    // HU-15: ventana de observación para estimar la rotación diaria real de un producto
    private static final int VENTANA_DIAS = 14;

    private final MovimientoStockRepository movimientoStockRepository;

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularRotacionDiaria(Long productoId) {
        LocalDateTime desde = LocalDateTime.now().minusDays(VENTANA_DIAS);
        Integer totalVendido = movimientoStockRepository.sumarVentasPorProductoDesde(productoId, desde);
        if (totalVendido == null || totalVendido <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(totalVendido)
                .divide(BigDecimal.valueOf(VENTANA_DIAS), 4, RoundingMode.HALF_UP);
    }
}
