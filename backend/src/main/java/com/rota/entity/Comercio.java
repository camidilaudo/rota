package com.rota.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fechaAlta;
}