package com.rota.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductoResponseDTO {
    private Long id;
    private String codigoBarra;
    private String nombre;
    private BigDecimal precioVenta;
    private BigDecimal costo;
    private BigDecimal margenGanancia;
    private BigDecimal porcentajeMargen;
    private Long categoriaId;
    private String categoriaNombre;
    private Integer diasUmbralCriticoCategoria;
}