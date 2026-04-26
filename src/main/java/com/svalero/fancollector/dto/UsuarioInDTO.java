package com.svalero.fancollector.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioInDTO {

    @NotBlank(message = "El nombre no puede estar en blanco")
    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    private String nombre;

    @Email(message = "Formato valido del email <ejemplo@gmail.com>")
    @NotBlank(message = "El email no puede estar en blanco")
    @Size(max = 100, message = "El email no puede superar los 100 caracteres")
    private String email;

    @NotBlank(message = "La contraseña no puede estar en blanco")
    @Size(min = 6, max = 30, message = "La contraseña debe tener entre 6 y 30 caracteres")
    private String contrasena;

    @Size(max = 500, message = "La URL del avatar no puede superar los 500 caracteres")
    private String urlAvatar;

    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    private String descripcion;

    @Size(max = 100, message = "El contacto público no puede superar los 100 caracteres")
    private String contactoPublico;
}
