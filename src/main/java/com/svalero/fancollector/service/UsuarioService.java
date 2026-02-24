package com.svalero.fancollector.service;

import com.svalero.fancollector.domain.enums.RolUsuario;
import com.svalero.fancollector.dto.UsuarioAdminOutDTO;
import com.svalero.fancollector.dto.UsuarioInDTO;
import com.svalero.fancollector.dto.UsuarioOutDTO;
import com.svalero.fancollector.dto.UsuarioPutDTO;
import com.svalero.fancollector.exception.domain.UsuarioNoEncontradoException;

import java.util.List;

public interface UsuarioService {

    UsuarioOutDTO crearUsuario(UsuarioInDTO usuarioInDTO);

    UsuarioOutDTO crearUsuarioComoAdmin(UsuarioInDTO dto, String emailUsuario, boolean esAdmin);

    UsuarioOutDTO buscarUsuarioPorId(long id) throws UsuarioNoEncontradoException;

    List<UsuarioOutDTO> listarUsuarios(String nombre, String email, RolUsuario rol);

    UsuarioOutDTO modificarUsuario(long id, UsuarioPutDTO dto, String emailUsuario, boolean esAdmin, boolean esMods)
            throws UsuarioNoEncontradoException;

    UsuarioOutDTO actualizarContrasena(long id, String nuevaContrasena, String emailUsuario, boolean esAdmin, boolean esMods)
            throws UsuarioNoEncontradoException;

    UsuarioOutDTO actualizarRol (long id, RolUsuario nuevoRol, String emailUsuario)
            throws UsuarioNoEncontradoException;

    void borrarUsuario (long id, String emailUsuario, boolean esAdmin, boolean esMods)
            throws UsuarioNoEncontradoException;

    UsuarioAdminOutDTO buscarUsuarioPorIdAdmin(long id)
            throws UsuarioNoEncontradoException;
}