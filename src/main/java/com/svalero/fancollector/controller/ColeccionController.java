package com.svalero.fancollector.controller;

import com.svalero.fancollector.dto.ColeccionInDTO;
import com.svalero.fancollector.dto.ColeccionOutDTO;
import com.svalero.fancollector.dto.ColeccionPutDTO;
import com.svalero.fancollector.dto.patches.ColeccionPlantillaDTO;
import com.svalero.fancollector.dto.patches.ColeccionPublicoDTO;
import com.svalero.fancollector.exception.domain.ColeccionNoEncontradaException;
import com.svalero.fancollector.exception.domain.UsuarioNoEncontradoException;
import com.svalero.fancollector.security.auth.SecurityUtils;
import com.svalero.fancollector.service.ColeccionService;
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
@RequestMapping("/colecciones")
public class ColeccionController {

    @Autowired
    private ColeccionService coleccionService;

    @PostMapping
    public ResponseEntity<ColeccionOutDTO> crearColeccion(
            @Valid @RequestBody ColeccionInDTO dto,
            Authentication authentication)
            throws UsuarioNoEncontradoException {
        String email = SecurityUtils.email(authentication);

        return new ResponseEntity<>(coleccionService.crearColeccion(dto,  email), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ColeccionOutDTO>> listarColecciones(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Long idCreador,
            @RequestParam(required = false) String nombreCreador,
            @RequestParam(required = false) Boolean usableComoPlantilla,
            Authentication authentication)
            throws UsuarioNoEncontradoException {
        String email = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods = SecurityUtils.isMods(authentication);

        return ResponseEntity.ok(coleccionService.listarColecciones(nombre, categoria, idCreador, nombreCreador, email, esAdmin, esMods, usableComoPlantilla));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ColeccionOutDTO> buscarColeccion(
            @PathVariable Long id,
            Authentication authentication)
            throws ColeccionNoEncontradaException, UsuarioNoEncontradoException {
        String email = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods = SecurityUtils.isMods(authentication);

        return ResponseEntity.ok(coleccionService.buscarColeccionPorId(id, email, esAdmin, esMods));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ColeccionOutDTO> actualizarColeccion(
            @PathVariable Long id,
            @Valid @RequestBody ColeccionPutDTO dto,
            Authentication authentication)
            throws ColeccionNoEncontradaException, UsuarioNoEncontradoException {
        String email = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods = SecurityUtils.isMods(authentication);

        return ResponseEntity.ok(coleccionService.actualizarColeccion(id, dto, email, esAdmin, esMods));
    }

    @PatchMapping("/{id}/publico")
    public ResponseEntity<ColeccionOutDTO> actualizarEsPublica(
            @PathVariable Long id,
            @Valid @RequestBody ColeccionPublicoDTO publicoDTO,
            Authentication authentication)
            throws ColeccionNoEncontradaException, UsuarioNoEncontradoException {
        String email = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods = SecurityUtils.isMods(authentication);

        return ResponseEntity.ok(coleccionService.actualizarEsPublica(id, publicoDTO.getEsPublica(), email, esAdmin, esMods));
    }

    @PatchMapping("/{id}/plantilla")
    public ResponseEntity<ColeccionOutDTO> actualizarUsableComoPlantilla(
            @PathVariable Long id,
            @Valid @RequestBody ColeccionPlantillaDTO plantillaDTO,
            Authentication authentication)
            throws ColeccionNoEncontradaException {
        String email = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);

        return ResponseEntity.ok(coleccionService.actualizarUsableComoPlantilla(id, plantillaDTO.getUsableComoPlantilla(), email, esAdmin));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarColeccion(
            @PathVariable Long id,
            Authentication authentication)
            throws ColeccionNoEncontradaException, UsuarioNoEncontradoException  {
        String email = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods = SecurityUtils.isMods(authentication);
        coleccionService.eliminarColeccion(id, email, esAdmin, esMods);

        return ResponseEntity.noContent().build();
    }
}
