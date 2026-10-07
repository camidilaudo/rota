package com.rota.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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

    // HU-05: datos financieros, se omiten (null) para el usuario operativo
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal costo;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal margenGanancia;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal porcentajeMargen;

    private Long categoriaId;
    private String categoriaNombre;
    private Integer diasUmbralCriticoCategoria;
}