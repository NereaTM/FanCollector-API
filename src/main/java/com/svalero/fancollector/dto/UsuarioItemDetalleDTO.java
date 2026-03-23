package com.svalero.fancollector.dto;

import com.svalero.fancollector.domain.enums.EstadoItem;
import com.svalero.fancollector.domain.enums.RarezaItem;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UsuarioItemDetalleDTO {

    private Long id;
    private Long idUsuario;
    private String nombreUsuario;
    private Long idColeccion;
    private String nombreColeccion;
    private Long idItem;
    private String nombreItem;
    private EstadoItem estado;
    private Integer cantidad;
    private String notas;
    private boolean esVisible;
    private LocalDateTime fechaRegistro;

    // datos de la v2
    private String descripcionItem;
    private String imagenUrl;
    private String tipo;
    private RarezaItem rareza;
    private Integer anioLanzamiento;
}