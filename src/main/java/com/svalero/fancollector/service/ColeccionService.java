package com.svalero.fancollector.service;

import com.svalero.fancollector.dto.ColeccionInDTO;
import com.svalero.fancollector.dto.ColeccionOutDTO;
import com.svalero.fancollector.dto.ColeccionPutDTO;
import com.svalero.fancollector.exception.domain.ColeccionNoEncontradaException;
import com.svalero.fancollector.exception.domain.UsuarioNoEncontradoException;

import java.util.List;

public interface ColeccionService {

    ColeccionOutDTO crearColeccion(ColeccionInDTO datosColeccion, String emailUsuario)
            throws UsuarioNoEncontradoException;

    ColeccionOutDTO buscarColeccionPorId(Long id, String emailUsuario, boolean esAdmin, boolean esMods)
            throws ColeccionNoEncontradaException;

    List<ColeccionOutDTO> listarColecciones(String nombre, String categoria, Long idCreador, String nombreCreador,String emailUsuario, boolean esAdmin, boolean esMods, Boolean usableComoPlantilla)
            throws UsuarioNoEncontradoException;

    ColeccionOutDTO actualizarColeccion(Long id, ColeccionPutDTO dto, String emailUsuario, boolean esAdmin, boolean esMods)
            throws ColeccionNoEncontradaException;

    void eliminarColeccion(Long idColeccion, String emailUsuario, boolean esAdmin, boolean esMods)
            throws ColeccionNoEncontradaException;

    ColeccionOutDTO actualizarEsPublica(Long id, Boolean esPublica, String emailUsuario, boolean esAdmin, boolean esMods)
            throws ColeccionNoEncontradaException;

    ColeccionOutDTO actualizarUsableComoPlantilla(Long id, Boolean usableComoPlantilla,String emailUsuario, boolean esAdmin)
            throws ColeccionNoEncontradaException;
}
