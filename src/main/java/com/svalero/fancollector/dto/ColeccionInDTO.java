package com.svalero.fancollector.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ColeccionInDTO {

    @NotNull(message = "El ID del creador es obligatorio")
    private Long idCreador;

    @NotBlank(message = "El nombre no puede estar en blanco")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    private String descripcion;

    @NotBlank(message = "La categoría no puede estar en blanco")
    @Size(max = 50, message = "La categoría no puede superar los 50 caracteres")
    private String categoria;

    private String imagenPortada;
    private Boolean esPublica;
    private Boolean usableComoPlantilla;
    private MultipartFile archivo;
}
