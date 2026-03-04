package com.svalero.fancollector.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UsuarioPutDTO {

    @NotBlank(message = "El nombre no puede estar en blanco")
    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    private String nombre;

    @Email(message = "Ejemplo email <ejemplo@gmail.com>")
    @NotBlank(message = "El email no puede estar en blanco")
    @Size(max = 100, message = "El email no puede superar los 100 caracteres")
    private String email;

    @Size(max = 500, message = "La URL del avatar no puede superar los 500 caracteres")
    private String urlAvatar;

    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    private String descripcion;

    @Size(max = 100, message = "El contacto público no puede superar los 100 caracteres")
    private String contactoPublico;
    private MultipartFile archivo;
}
