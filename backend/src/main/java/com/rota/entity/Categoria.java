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
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotNull(message = "El umbral de días de alerta es obligatorio")
    @Min(value = 1, message = "El umbral de alerta debe ser de al menos 1 día")
    @Column(name = "dias_umbral_alerta", nullable = false)
    private Integer diasUmbralAlerta;

    @Column(name = "dias_umbral_critico")
    private Integer diasUmbralCritico;

    @Enumerated(EnumType.STRING)
    @Column(name = "modo_notificacion", nullable = false, length = 30)
    private ModoNotificacion modoNotificacion;

    @Column(name = "es_piloto")
    @Builder.Default
    private Boolean esPiloto = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activa = true;
}