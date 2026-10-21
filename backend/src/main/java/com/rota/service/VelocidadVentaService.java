package com.rota.service;

import java.math.BigDecimal;

public interface VelocidadVentaService {
    /**
     * Rotación diaria promedio (unidades/día) de un producto, calculada sobre la ventana de ventas reciente.
     * Devuelve BigDecimal.ZERO si no hay ventas registradas en la ventana.
     */
    BigDecimal calcularRotacionDiaria(Long productoId);
}
