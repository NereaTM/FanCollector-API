package com.svalero.fancollector.service;

import com.svalero.fancollector.domain.enums.RarezaItem;
import com.svalero.fancollector.dto.ItemInDTO;
import com.svalero.fancollector.dto.ItemOutDTO;
import com.svalero.fancollector.dto.ItemPutDTO;

import java.util.List;

public interface ItemService {

    ItemOutDTO crearItem(ItemInDTO dto, String emailUsuario, boolean esAdmin, boolean esMods);

    ItemOutDTO buscarItemPorId(Long idItem, String emailUsuario, boolean esAdmin, boolean esMods);

    List<ItemOutDTO> listarItems(String nombre, String tipo, String rarezaStr, Long idColeccion, String emailUsuario, boolean esAdmin, boolean esMods);

    ItemOutDTO actualizarItem(Long idItem, ItemPutDTO datosItem, String emailUsuario, boolean esAdmin, boolean esMods);

    void eliminarItem(Long idItem, String emailUsuario, boolean esAdmin, boolean esMods);

    ItemOutDTO actualizarRareza(Long id, RarezaItem rareza, String emailUsuario, boolean esAdmin, boolean esModsa);
}
