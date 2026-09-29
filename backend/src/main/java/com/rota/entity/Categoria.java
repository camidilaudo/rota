package com.rota.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "categorias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @NotNull(message = "El umbral de días críticos es obligatorio")
    @Min(value = 1, message = "El umbral de días críticos debe ser al menos 1 día")
    @Column(name = "dias_umbral_critico", nullable = false)
    private Integer diasUmbralCritico;

    @Enumerated(EnumType.STRING)
    @Column(name = "modo_notificacion", nullable = false)
    @Builder.Default
    private ModoNotificacion modoNotificacion = ModoNotificacion.PANTALLA;

    @Builder.Default
    @Column(name = "es_piloto", nullable = false)
    private Boolean esPiloto = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activa = true;
}