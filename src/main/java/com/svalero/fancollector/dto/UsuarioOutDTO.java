package com.svalero.fancollector.dto;

import com.svalero.fancollector.domain.enums.RolUsuario;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UsuarioOutDTO {

    private Long id;
    private String nombre;
    private String email;
    private RolUsuario rol;
    private String urlAvatar;
    private String descripcion;
    private String contactoPublico;
    private LocalDateTime fechaRegistro;
}
