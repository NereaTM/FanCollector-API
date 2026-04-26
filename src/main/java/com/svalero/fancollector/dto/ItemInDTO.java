package com.svalero.fancollector.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ItemInDTO {

    @NotNull(message = "La colección es obligatoria")
    private Long idColeccion;

    @NotBlank(message = "El nombre no puede estar en blanco")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    private String nombre;

    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    private String descripcion;
    private String imagenUrl;

    @NotBlank(message = "El tipo es obligatorio")
    @Size(max = 50, message = "El tipo no puede superar los 50 caracteres")
    private String tipo;

    @NotBlank(message = "La rareza tiene que ser: COMUN, RARO, EPICO, LEGENDARIO")
    private String rareza;

    private Integer anioLanzamiento;
    private MultipartFile archivo;
}
