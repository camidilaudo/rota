package com.rota.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequestDTO {

    @Email(message = "El email no es válido")
    @NotBlank(message = "El email es obligatorio")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}