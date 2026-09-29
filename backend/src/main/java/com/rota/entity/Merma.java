package com.rota.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mermas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Merma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @NotNull(message = "La cantidad mermada es obligatoria")
    @Min(value = 1, message = "La cantidad a mermar debe ser de al menos 1 unidad")
    @Column(nullable = false)
    private Integer cantidad;

    @NotBlank(message = "El motivo de la merma es obligatorio")
    @Column(nullable = false, length = 150)
    private String motivo;

    @NotNull(message = "El costo unitario es obligatorio")
    @Column(name = "costo_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoUnitario;

    @NotNull(message = "La pérdida total es obligatoria")
    @Column(name = "perdida_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal perdidaTotal;

    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;
}