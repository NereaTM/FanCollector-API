package com.svalero.fancollector.controller;

import com.svalero.fancollector.dto.ItemInDTO;
import com.svalero.fancollector.dto.ItemOutDTO;
import com.svalero.fancollector.dto.ItemPutDTO;
import com.svalero.fancollector.dto.patches.ItemRarezaDTO;
import com.svalero.fancollector.exception.domain.ColeccionNoEncontradaException;
import com.svalero.fancollector.exception.domain.ItemNoEncontradoException;
import com.svalero.fancollector.security.auth.SecurityUtils;
import com.svalero.fancollector.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemOutDTO> crearItem(
            @Valid @RequestBody ItemInDTO itemInDTO,
            Authentication authentication)
            throws ColeccionNoEncontradaException {
        String email = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods = SecurityUtils.isMods(authentication);

        ItemOutDTO nuevo = itemService.crearItem(itemInDTO, email, esAdmin, esMods);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ItemOutDTO>> listarItems(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String rareza,
            @RequestParam(required = false) Long idColeccion,
            Authentication authentication) {
        String email = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods  = SecurityUtils.isMods(authentication);

        List<ItemOutDTO> items = itemService.listarItems(nombre, tipo, rareza, idColeccion, email, esAdmin, esMods);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemOutDTO> buscarItem(
            @PathVariable Long id,
            Authentication authentication)
            throws ItemNoEncontradoException {
        String email = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods  = SecurityUtils.isMods(authentication);

        ItemOutDTO item = itemService.buscarItemPorId(id, email, esAdmin, esMods);
        return ResponseEntity.ok(item);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemOutDTO> actualizarItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemPutDTO itemPutDTO,
            Authentication authentication)
            throws ItemNoEncontradoException, ColeccionNoEncontradaException {
        String email = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods  = SecurityUtils.isMods(authentication);

        ItemOutDTO actualizado = itemService.actualizarItem(id, itemPutDTO, email, esAdmin, esMods);
        return ResponseEntity.ok(actualizado);
    }

    @PatchMapping("/{id}/rareza")
    public ResponseEntity<ItemOutDTO> actualizarRareza(
            @PathVariable Long id,
            @Valid @RequestBody ItemRarezaDTO rarezaDTO,
            Authentication authentication)
            throws ItemNoEncontradoException {
        String email = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods  = SecurityUtils.isMods(authentication);

        ItemOutDTO itemActualizado = itemService.actualizarRareza(id, rarezaDTO.getRareza(), email, esAdmin, esMods);
        return ResponseEntity.ok(itemActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarItem(
            @PathVariable Long id,
            Authentication authentication)
            throws ItemNoEncontradoException {
        String email = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods  = SecurityUtils.isMods(authentication);

        itemService.eliminarItem(id, email, esAdmin, esMods);
    return ResponseEntity.noContent().build();
    }
}