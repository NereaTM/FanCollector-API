package com.svalero.fancollector.service;

import com.svalero.fancollector.dto.ColeccionInDTO;
import com.svalero.fancollector.dto.ColeccionOutDTO;
import com.svalero.fancollector.dto.ColeccionPutDTO;

import java.util.List;

public interface ColeccionService {

    ColeccionOutDTO crearColeccion(ColeccionInDTO datosColeccion, String emailUsuario);

    ColeccionOutDTO buscarColeccionPorId(Long id, String emailUsuario, boolean esAdmin, boolean esMods);

    List<ColeccionOutDTO> listarColecciones(String nombre, String categoria, Long idCreador, String nombreCreador,String emailUsuario, boolean esAdmin, boolean esMods, Boolean usableComoPlantilla);

    ColeccionOutDTO actualizarColeccion(Long id, ColeccionPutDTO dto, String emailUsuario, boolean esAdmin, boolean esMods);

    void eliminarColeccion(Long idColeccion, String emailUsuario, boolean esAdmin, boolean esMods);

    ColeccionOutDTO actualizarEsPublica(Long id, Boolean esPublica, String emailUsuario, boolean esAdmin, boolean esMods);

    ColeccionOutDTO actualizarUsableComoPlantilla(Long id, Boolean usableComoPlantilla,String emailUsuario, boolean esAdmin);
}
