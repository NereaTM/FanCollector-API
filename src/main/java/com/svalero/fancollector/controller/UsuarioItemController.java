package com.svalero.fancollector.controller;

import com.svalero.fancollector.domain.enums.EstadoItem;
import com.svalero.fancollector.dto.UsuarioItemInDTO;
import com.svalero.fancollector.dto.UsuarioItemOutDTO;
import com.svalero.fancollector.dto.UsuarioItemPutDTO;
import com.svalero.fancollector.dto.patches.UsuarioItemVisibleDTO;
import com.svalero.fancollector.security.auth.SecurityUtils;
import com.svalero.fancollector.service.UsuarioItemService;
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
@RequestMapping("/usuario-items")
public class UsuarioItemController {

    @Autowired
    private UsuarioItemService usuarioItemService;

    @PostMapping
    public ResponseEntity<UsuarioItemOutDTO> crear(
            @Valid @RequestBody UsuarioItemInDTO dto,
            Authentication authentication) {
        String email = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods = SecurityUtils.isMods(authentication);

        return new ResponseEntity<>(usuarioItemService.crear(dto, email, esAdmin, esMods), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioItemOutDTO>> listar(
            @RequestParam(required = false) Long idUsuario,
            @RequestParam(required = false) Long idItem,
            @RequestParam(required = false) Long idColeccion,
            @RequestParam(required = false) EstadoItem estado,
            @RequestParam(required = false) Boolean esVisible,
            Authentication authentication) {
        String email = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods = SecurityUtils.isMods(authentication);

        return ResponseEntity.ok(usuarioItemService.listar(idUsuario, idItem, idColeccion, estado, esVisible, email, esAdmin, esMods));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioItemOutDTO> buscar(
            @PathVariable Long id,
            Authentication authentication) {
        String email = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods = SecurityUtils.isMods(authentication);

        return ResponseEntity.ok(usuarioItemService.buscarPorId(id, email, esAdmin,esMods));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioItemOutDTO> actualizarCompleto(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioItemPutDTO dto,
            Authentication authentication) {
        String email = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods = SecurityUtils.isMods(authentication);

        return ResponseEntity.ok(usuarioItemService.actualizarCompleto(id, dto, email, esAdmin, esMods));
    }

    @PatchMapping("/{id}/visible")
    public ResponseEntity<UsuarioItemOutDTO> actualizarVisibilidad(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioItemVisibleDTO visibleDTO,
            Authentication authentication) {
        String email = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods = SecurityUtils.isMods(authentication);

        return ResponseEntity.ok(usuarioItemService.actualizarVisibilidad(id, visibleDTO.getEsVisible(), email, esAdmin, esMods));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            Authentication authentication) {
        String email = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods = SecurityUtils.isMods(authentication);

        usuarioItemService.eliminar(id, email, esAdmin, esMods);
        return ResponseEntity.noContent().build();
    }
}
