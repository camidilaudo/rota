package com.rota.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lote desde el que se realizó el movimiento
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipo;

    @NotNull(message = "La cantidad del movimiento es obligatoria")
    @Min(value = 1, message = "La cantidad del movimiento debe ser de al menos 1 unidad")
    @Column(nullable = false)
    private Integer cantidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "ubicacion_origen", nullable = false, length = 30)
    private Ubicacion ubicacionOrigen;

    @Enumerated(EnumType.STRING)
    @Column(name = "ubicacion_destino", nullable = false, length = 30)
    private Ubicacion ubicacionDestino;

    // HU-12: usuario autenticado que registró el movimiento
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @CreationTimestamp
    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;
}
