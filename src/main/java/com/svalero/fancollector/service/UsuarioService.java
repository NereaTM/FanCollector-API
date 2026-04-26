package com.svalero.fancollector.service;

import com.svalero.fancollector.domain.enums.RolUsuario;
import com.svalero.fancollector.dto.UsuarioInDTO;
import com.svalero.fancollector.dto.UsuarioOutDTO;
import com.svalero.fancollector.dto.UsuarioPutDTO;

import java.util.List;

public interface UsuarioService {

    UsuarioOutDTO crearUsuario(UsuarioInDTO usuarioInDTO);

    UsuarioOutDTO crearUsuarioComoAdmin(UsuarioInDTO dto, String emailUsuario, boolean esAdmin);

    UsuarioOutDTO buscarUsuarioPorId(long id);

    List<UsuarioOutDTO> listarUsuarios(String nombre, String email, RolUsuario rol);

    UsuarioOutDTO modificarUsuario(long id, UsuarioPutDTO dto, String emailUsuario, boolean esAdmin, boolean esMods);

    UsuarioOutDTO actualizarContrasena(long id, String nuevaContrasena, String emailUsuario, boolean esAdmin, boolean esMods);

    UsuarioOutDTO actualizarRol (long id, RolUsuario nuevoRol, String emailUsuario);

    void borrarUsuario (long id, String emailUsuario, boolean esAdmin, boolean esMods);

}