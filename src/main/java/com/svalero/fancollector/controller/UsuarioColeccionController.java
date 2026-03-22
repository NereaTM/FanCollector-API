package com.svalero.fancollector.controller;

import com.svalero.fancollector.dto.UsuarioColeccionInDTO;
import com.svalero.fancollector.dto.UsuarioColeccionOutDTO;
import com.svalero.fancollector.dto.UsuarioColeccionPutDTO;
import com.svalero.fancollector.dto.patches.UsuarioColeccionFavoritaDTO;
import com.svalero.fancollector.dto.patches.UsuarioColeccionVisibleDTO;
import com.svalero.fancollector.exception.domain.UsuarioColeccionNoEncontradoException;
import com.svalero.fancollector.security.auth.SecurityUtils;
import com.svalero.fancollector.service.UsuarioColeccionService;
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
@RequestMapping("/usuario-colecciones")
public class UsuarioColeccionController {

    @Autowired
    private UsuarioColeccionService usuarioColeccionService;

    @PostMapping
    public ResponseEntity<UsuarioColeccionOutDTO> crear(
            @Valid @RequestBody UsuarioColeccionInDTO dto,
            Authentication authentication) {
        String email = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods = SecurityUtils.isMods(authentication);

        return new ResponseEntity<>(usuarioColeccionService.crear(dto, email, esAdmin, esMods),HttpStatus.CREATED);
    }

    @PostMapping("/v2")
    public ResponseEntity<UsuarioColeccionOutDTO> crearV2(
            @Valid @RequestBody UsuarioColeccionInDTO dto,
            Authentication authentication) {
        String email = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods = SecurityUtils.isMods(authentication);

        return new ResponseEntity<>(usuarioColeccionService.crearV2(dto, email, esAdmin, esMods), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioColeccionOutDTO>> listar(
            @RequestParam(required = false) Long idUsuario,
            @RequestParam(required = false) Long idColeccion,
            @RequestParam(required = false) Boolean soloFavoritas,
            @RequestParam(required = false) Boolean esVisible,
            Authentication authentication) {
        String email = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods = SecurityUtils.isMods(authentication);

        return ResponseEntity.ok(usuarioColeccionService.listar(idUsuario, idColeccion, soloFavoritas, esVisible, email, esAdmin, esMods));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioColeccionOutDTO> buscar(
            @PathVariable Long id,
            Authentication authentication) {
        String email = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods = SecurityUtils.isMods(authentication);

        return ResponseEntity.ok(usuarioColeccionService.buscarPorId(id, email, esAdmin, esMods));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioColeccionOutDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioColeccionPutDTO dto,
            Authentication authentication) {
        String email = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods = SecurityUtils.isMods(authentication);

        return ResponseEntity.ok(usuarioColeccionService.actualizar(id, dto, email, esAdmin, esMods));
    }

    @PatchMapping("/{id}/favorita")
    public ResponseEntity<UsuarioColeccionOutDTO> actualizarFavorita(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioColeccionFavoritaDTO dto,
            Authentication authentication) {
        String email = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods = SecurityUtils.isMods(authentication);

        return ResponseEntity.ok( usuarioColeccionService.actualizarFavorita(id, dto, email, esAdmin, esMods));
    }

    @PatchMapping("/{id}/visible")
    public ResponseEntity<UsuarioColeccionOutDTO> actualizarVisible(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioColeccionVisibleDTO dto,
            Authentication authentication) {
        String email = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods = SecurityUtils.isMods(authentication);

        return ResponseEntity.ok(usuarioColeccionService.actualizarVisible(id, dto, email, esAdmin, esMods));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id)
            throws UsuarioColeccionNoEncontradoException {

        usuarioColeccionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("v2/{id}")
    public ResponseEntity<Void> eliminarV2(
            @PathVariable Long id,
            Authentication authentication) {
        String email = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods = SecurityUtils.isMods(authentication);

        usuarioColeccionService.eliminarV2(id, email, esAdmin, esMods);
        return ResponseEntity.noContent().build();
    }
}
