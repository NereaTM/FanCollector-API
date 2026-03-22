package com.svalero.fancollector.service;

import com.svalero.fancollector.dto.UsuarioColeccionInDTO;
import com.svalero.fancollector.dto.UsuarioColeccionOutDTO;
import com.svalero.fancollector.dto.UsuarioColeccionPutDTO;
import com.svalero.fancollector.dto.patches.UsuarioColeccionFavoritaDTO;
import com.svalero.fancollector.dto.patches.UsuarioColeccionVisibleDTO;

import java.util.List;

public interface UsuarioColeccionService {

    UsuarioColeccionOutDTO crear(UsuarioColeccionInDTO dto, String emailUsuario, boolean esAdmin, boolean esMods);

    UsuarioColeccionOutDTO crearV2(UsuarioColeccionInDTO dto, String emailUsuario, boolean esAdmin, boolean esMods);

    UsuarioColeccionOutDTO buscarPorId(Long id, String emailUsuario, boolean esAdmin, boolean esMods);

    List<UsuarioColeccionOutDTO> listar(Long idUsuario, Long idColeccion, Boolean soloFavoritas, Boolean esVisible, String emailUsuario, boolean esAdmin, boolean esMods);

    UsuarioColeccionOutDTO actualizar(Long id, UsuarioColeccionPutDTO dto, String emailUsuario, boolean esAdmin, boolean esMods);

    void eliminar(Long id);

    void eliminarV2(Long id, String emailUsuario, boolean esAdmin, boolean esMods);

    UsuarioColeccionOutDTO actualizarFavorita(Long id, UsuarioColeccionFavoritaDTO dto, String emailUsuario, boolean esAdmin, boolean esMods);

    UsuarioColeccionOutDTO actualizarVisible(Long id, UsuarioColeccionVisibleDTO dto, String emailUsuario, boolean esAdmin, boolean esMods);
}
