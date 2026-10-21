package com.rota.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "comercios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comercio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del comercio es obligatorio")
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 20)
    private String cuit;

    @Column(length = 150)
    private String direccion;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    // HU-19: piso de margen (%) que ningún descuento sugerido o aplicado puede perforar
    // @ColumnDefault asegura que el ALTER TABLE no falle sobre comercios ya existentes
    @Builder.Default
    @ColumnDefault("20.00")
    @Column(name = "margen_minimo_porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal margenMinimoPorcentaje = new BigDecimal("20.00");

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fechaAlta;
}