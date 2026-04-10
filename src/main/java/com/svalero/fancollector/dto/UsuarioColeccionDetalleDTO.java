package com.svalero.fancollector.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UsuarioColeccionDetalleDTO {

    private Long id;
    private Long idUsuario;
    private Long idColeccion;
    private Boolean esFavorita;
    private Boolean esCreador;
    private Boolean esVisible;
    private LocalDateTime fechaAgregada;

    private ColeccionOutDTO coleccion;
    private String nombreUsuario;
}
