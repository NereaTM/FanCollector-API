package com.svalero.fancollector.service;

import com.svalero.fancollector.domain.*;
import com.svalero.fancollector.domain.enums.EstadoItem;
import com.svalero.fancollector.domain.enums.RarezaItem;
import com.svalero.fancollector.dto.ItemInDTO;
import com.svalero.fancollector.dto.ItemOutDTO;
import com.svalero.fancollector.dto.ItemPutDTO;
import com.svalero.fancollector.exception.domain.ColeccionNoEncontradaException;
import com.svalero.fancollector.exception.domain.ItemNoEncontradoException;
import com.svalero.fancollector.exception.domain.UsuarioNoEncontradoException;
import com.svalero.fancollector.repository.ColeccionRepository;
import com.svalero.fancollector.repository.ItemRepository;
import com.svalero.fancollector.repository.UsuarioColeccionRepository;
import com.svalero.fancollector.repository.UsuarioItemRepository;
import com.svalero.fancollector.security.auth.CurrentUserResolver;
import com.svalero.fancollector.security.auth.Permisos;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ColeccionRepository coleccionRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CurrentUserResolver currentUserResolver;

    @Autowired
    private UsuarioItemRepository usuarioItemRepository;

    @Autowired
    private UsuarioColeccionRepository usuarioColeccionRepository;

    @Override
    @Transactional
    public ItemOutDTO crearItem(ItemInDTO datosItem, String emailUsuario, boolean esAdmin, boolean esMods) {

        Coleccion coleccion = coleccionRepository.findById(datosItem.getIdColeccion())
                .orElseThrow(() -> new ColeccionNoEncontradaException(datosItem.getIdColeccion()));

        Usuario actual = currentUserResolver.usuarioActual(emailUsuario);
        Permisos.checkPuedeEditarOBorrarColeccion(coleccion, actual, esAdmin, esMods);

        Item item = modelMapper.map(datosItem, Item.class);

        if (datosItem.getRareza() != null) {
            try {RarezaItem rareza = RarezaItem.valueOf(datosItem.getRareza().toUpperCase());
                item.setRareza(rareza);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Rareza no válida: " + datosItem.getRareza());
            }
        }

        item.setColeccion(coleccion);

        Item guardado = itemRepository.save(item);

        if (coleccion.isEsPublica() && coleccion.isUsableComoPlantilla()) {

            List<UsuarioColeccion> unidos = usuarioColeccionRepository.findByColeccion_Id(coleccion.getId());

            for (UsuarioColeccion uc : unidos) {
                Usuario usuarioDueno = uc.getUsuario();

                // Evitar duplicados por si acaso
                boolean existe = usuarioItemRepository.existsByUsuarioIdAndColeccionIdAndItemId(
                        usuarioDueno.getId(), coleccion.getId(), guardado.getId());
                if (existe) continue;

                UsuarioItem usuarioItem = new UsuarioItem();
                usuarioItem.setUsuario(usuarioDueno);
                usuarioItem.setColeccion(coleccion);
                usuarioItem.setItem(guardado);
                usuarioItem.setEstado(EstadoItem.BUSCO);
                usuarioItem.setCantidad(0);
                usuarioItem.setEsVisible(true);
                usuarioItem.setFechaRegistro(LocalDateTime.now());

                usuarioItemRepository.save(usuarioItem);
            }
        }
        return modelMapper.map(guardado, ItemOutDTO.class);
    }

    @Override
    public ItemOutDTO buscarItemPorId(Long idItem, String emailUsuario, boolean esAdmin, boolean esMods) {
        Item item = itemRepository.findById(idItem)
                .orElseThrow(() -> new ItemNoEncontradoException(idItem));
        Usuario actual = currentUserResolver.usuarioActual(emailUsuario);
        Permisos.checkPuedeVerColeccion(item.getColeccion(), actual, esAdmin);
        return modelMapper.map(item, ItemOutDTO.class);
    }

    @Override
    public List<ItemOutDTO> listarItems(String nombre, String tipo, String rarezaStr, Long idColeccion, String emailUsuario, boolean esAdmin, boolean esMods) {

        List<Item> items;
        RarezaItem rareza = null;

        if (rarezaStr != null && !rarezaStr.isBlank()) {
            try { rareza = RarezaItem.valueOf(rarezaStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Valor de rareza no válido: " + rarezaStr);
            }
        }

        if ((nombre == null || nombre.isBlank()) &&
                (tipo == null || tipo.isBlank()) &&
                (rarezaStr == null || rarezaStr.isBlank()) &&
                idColeccion == null) {
            items = itemRepository.findAll();
        } else {
            items = itemRepository.buscarPorFiltros(nombre, tipo, rareza, idColeccion);
        }
        //sin logeo
        if (emailUsuario == null || emailUsuario.isBlank()) {
            return items.stream()
                    .filter(item -> item.getColeccion().isEsPublica())
                    .map(item -> modelMapper.map(item, ItemOutDTO.class))
                    .toList();
        }
        //con logeo
        Usuario actual = currentUserResolver.usuarioActual(emailUsuario);

        return items.stream()
                .filter(item -> Permisos.puedeVerColeccion(item.getColeccion(), actual, esAdmin))
                .map(item -> modelMapper.map(item, ItemOutDTO.class))
                .toList();
    }

    @Override
    public ItemOutDTO actualizarItem(Long idItem, ItemPutDTO datosItem, String emailUsuario, boolean esAdmin, boolean esMods) {

        Item existente = itemRepository.findById(idItem)
                .orElseThrow(() -> new ItemNoEncontradoException(idItem));

        Usuario actual = currentUserResolver.usuarioActual(emailUsuario);
        Permisos.checkPuedeEditarOBorrarColeccion(existente.getColeccion(), actual, esAdmin, esMods);

        existente.setNombre(datosItem.getNombre());
        existente.setDescripcion(datosItem.getDescripcion());
        existente.setTipo(datosItem.getTipo());
        existente.setImagenUrl(datosItem.getImagenUrl());
        existente.setAnioLanzamiento(datosItem.getAnioLanzamiento());

        if (datosItem.getRareza() != null && !datosItem.getRareza().isBlank()) {
            try {
                RarezaItem rareza = RarezaItem.valueOf(datosItem.getRareza().toUpperCase());
                existente.setRareza(rareza);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Rareza no válida: " + datosItem.getRareza());
            }
        }

        Item actualizado = itemRepository.save(existente);
        return modelMapper.map(actualizado, ItemOutDTO.class);
    }

    @Override
    public ItemOutDTO actualizarRareza(Long id, RarezaItem rareza, String emailUsuario, boolean esAdmin, boolean esMods) {

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNoEncontradoException(id));

        Usuario actual = currentUserResolver.usuarioActual(emailUsuario);
        Permisos.checkPuedeEditarOBorrarColeccion(item.getColeccion(), actual, esAdmin, esMods);

        item.setRareza(rareza);

        Item actualizado = itemRepository.save(item);
        return modelMapper.map(actualizado, ItemOutDTO.class);
    }

    @Override
    @Transactional
    public void eliminarItem(Long idItem, String emailUsuario, boolean esAdmin, boolean esMods) {

        Item item = itemRepository.findById(idItem)
                .orElseThrow(() -> new ItemNoEncontradoException(idItem));

        Usuario actual = currentUserResolver.usuarioActual(emailUsuario);
        Permisos.checkPuedeEditarOBorrarColeccion(item.getColeccion(), actual, esAdmin, esMods);

        itemRepository.delete(item);
    }
}
