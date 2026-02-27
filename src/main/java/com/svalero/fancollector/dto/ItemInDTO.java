package com.svalero.fancollector.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ItemInDTO {

    @NotNull(message = "La colección es obligatoria")
    private Long idColeccion;

    @NotBlank(message = "El nombre no puede estar en blanco")
    private String nombre;

    private String descripcion;
    private String imagenUrl;

    @NotBlank(message = "El tipo es obligatorio")
    private String tipo;

    @NotNull(message = "La rareza tiene que ser : COMUN, RARO, EPICO, LEGENDARIO")
    private String  rareza;

    private Integer anioLanzamiento;
    private MultipartFile archivo;
}
