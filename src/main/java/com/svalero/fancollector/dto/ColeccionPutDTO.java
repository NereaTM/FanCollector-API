package com.svalero.fancollector.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ColeccionPutDTO {

    @NotBlank(message = "El nombre no puede estar en blanco")
    private String nombre;

    private String descripcion;

    @NotBlank(message = "La categoria no puede estar en blanco")
    private String categoria;

    private String imagenPortada;
    private Boolean esPublica;
    private Boolean usableComoPlantilla;
    private MultipartFile archivo;
}