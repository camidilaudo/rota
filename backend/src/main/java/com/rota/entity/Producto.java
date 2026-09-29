package com.rota.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código de barras es obligatorio")
    @Column(name = "codigo_barra", nullable = false, unique = true, length = 50)
    private String codigoBarra;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Column(nullable = false, length = 120)
    private String nombre;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio de venta debe ser mayor a 0")
    @Column(name = "precio_venta", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioVenta;

    @NotNull(message = "El costo es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El costo no puede ser negativo")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    /**
     * Calcula el margen unitario ($)
     */
    public BigDecimal getMargenGanancia() {
        if (precioVenta == null || costo == null) return BigDecimal.ZERO;
        return precioVenta.subtract(costo);
    }

    /**
     * Calcula el porcentaje de margen sobre el costo (%)
     */
    public BigDecimal getPorcentajeMargen() {
        if (costo == null || costo.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return getMargenGanancia()
                .multiply(BigDecimal.valueOf(100))
                .divide(costo, 2, RoundingMode.HALF_UP);
    }
}