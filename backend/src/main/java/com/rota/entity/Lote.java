package com.rota.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lotes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    // HU-20/HU-22: un lote puede agotarse por completo (venta o baja total), por eso se permite 0.
    // La creación de lotes exige mínimo 1 unidad vía LoteRequestDTO.
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 0, message = "La cantidad del lote no puede ser negativa")
    @Column(nullable = false)
    private Integer cantidad;

    // Sin @FutureOrPresent: un lote existente puede vencer y aun así tiene que poder actualizarse
    // (revisión, descuento, traslado). La creación exige fecha futura vía LoteRequestDTO.
    @NotNull(message = "La fecha de vencimiento es obligatoria")
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_recepcion", nullable = false, updatable = false)
    private LocalDate fechaRecepcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Ubicacion ubicacion;

    @Column(name = "porcentaje_descuento", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal porcentajeDescuento = BigDecimal.ZERO;

    // HU-16: constancia de revisión (cuándo y quién). Un lote revisado hoy sale de la ruta del día.
    @Column(name = "revisado_en")
    private LocalDateTime revisadoEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revisado_por_id")
    private Usuario revisadoPor;

    @PrePersist
    public void prePersist() {
        if (this.fechaRecepcion == null) {
            this.fechaRecepcion = LocalDate.now();
        }
    }
}