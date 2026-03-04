package com.svalero.fancollector.service;

import com.svalero.fancollector.domain.enums.EstadoItem;
import com.svalero.fancollector.dto.UsuarioItemInDTO;
import com.svalero.fancollector.dto.UsuarioItemOutDTO;
import com.svalero.fancollector.dto.UsuarioItemPutDTO;

import java.util.List;

public interface UsuarioItemService {

    UsuarioItemOutDTO crear(UsuarioItemInDTO dto, String emailUsuario, boolean esAdmin, boolean esMods);

    UsuarioItemOutDTO buscarPorId(Long id, String emailUsuario, boolean esAdmin, boolean esMods);

    List<UsuarioItemOutDTO> listar(Long idUsuario, Long idItem, Long idColeccion, EstadoItem estado, Boolean esVisible, String emailUsuario, boolean esAdmin, boolean esMods);

    UsuarioItemOutDTO actualizarCompleto(Long id, UsuarioItemPutDTO dto, String emailUsuario, boolean esAdmin, boolean esMods);

    UsuarioItemOutDTO actualizarVisibilidad(Long id, Boolean esVisible, String emailUsuario, boolean esAdmin, boolean esMods);

    void eliminar(Long id, String emailUsuario, boolean esAdmin, boolean esMods);
}
