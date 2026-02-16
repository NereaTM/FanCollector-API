package com.svalero.fancollector.service;

import com.svalero.fancollector.domain.enums.RarezaItem;
import com.svalero.fancollector.dto.ItemInDTO;
import com.svalero.fancollector.dto.ItemOutDTO;
import com.svalero.fancollector.dto.ItemPutDTO;
import com.svalero.fancollector.exception.domain.ColeccionNoEncontradaException;
import com.svalero.fancollector.exception.domain.ItemNoEncontradoException;

import java.util.List;

public interface ItemService {

    ItemOutDTO crearItem(ItemInDTO dto, String emailUsuario, boolean esAdmin, boolean esMods)
            throws ColeccionNoEncontradaException;

    ItemOutDTO buscarItemPorId(Long idItem, String emailUsuario, boolean esAdmin, boolean esMods)
            throws ItemNoEncontradoException;

    List<ItemOutDTO> listarItems(String nombre, String tipo, String rarezaStr, Long idColeccion, String emailUsuario, boolean esAdmin, boolean esMods);

    ItemOutDTO actualizarItem(Long idItem, ItemPutDTO datosItem, String emailUsuario, boolean esAdmin, boolean esMods)
            throws ItemNoEncontradoException;

    void eliminarItem(Long idItem, String emailUsuario, boolean esAdmin, boolean esMods)
            throws ItemNoEncontradoException;

    ItemOutDTO actualizarRareza(Long id, RarezaItem rareza, String emailUsuario, boolean esAdmin, boolean esModsa)
            throws ItemNoEncontradoException;
}
