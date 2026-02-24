package com.svalero.fancollector.controller;

import com.svalero.fancollector.domain.enums.RolUsuario;
import com.svalero.fancollector.dto.UsuarioAdminOutDTO;
import com.svalero.fancollector.dto.UsuarioInDTO;
import com.svalero.fancollector.dto.UsuarioOutDTO;
import com.svalero.fancollector.dto.UsuarioPutDTO;
import com.svalero.fancollector.dto.patches.UsuarioPasswordDTO;
import com.svalero.fancollector.dto.patches.UsuarioRolDTO;
import com.svalero.fancollector.exception.domain.UsuarioNoEncontradoException;
import com.svalero.fancollector.security.auth.SecurityUtils;
import com.svalero.fancollector.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioOutDTO> crearUsuarioComoAdmin(
            @Valid @RequestBody UsuarioInDTO dto, Authentication authentication) {
        String emailUsuario = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);

        UsuarioOutDTO nuevoUsuario = usuarioService.crearUsuarioComoAdmin(dto, emailUsuario, esAdmin);
        return ResponseEntity.status(201).body(nuevoUsuario);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioOutDTO>> listarUsuarios(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) RolUsuario rol
    ) {
        List<UsuarioOutDTO> listaUsuarios = usuarioService.listarUsuarios(nombre, email, rol);
        return ResponseEntity.ok(listaUsuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioOutDTO> obtenerUsuario(
            @PathVariable long id)
            throws UsuarioNoEncontradoException {
        UsuarioOutDTO usuarioEncontrado  = usuarioService.buscarUsuarioPorId(id);
        return ResponseEntity.ok(usuarioEncontrado );
    }

    @GetMapping("/{id}/admin")
    @PreAuthorize("hasAnyRole('ADMIN','MODS')")
    public ResponseEntity<UsuarioAdminOutDTO> obtenerUsuarioAdmin(
            @PathVariable long id)
            throws UsuarioNoEncontradoException {
        UsuarioAdminOutDTO dto = usuarioService.buscarUsuarioPorIdAdmin(id);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioOutDTO> modificarUsuario(
            @PathVariable long id,
            @Valid @RequestBody UsuarioPutDTO dto,
            Authentication authentication) throws UsuarioNoEncontradoException {

            String emailUsuario = SecurityUtils.email(authentication);
            boolean esAdmin = SecurityUtils.isAdmin(authentication);
            boolean esMods = SecurityUtils.isMods(authentication);

        UsuarioOutDTO usuarioModificado = usuarioService.modificarUsuario(id, dto, emailUsuario, esAdmin, esMods);
        return ResponseEntity.ok(usuarioModificado);
    }

    @PatchMapping("/{id}/contrasena")
    public ResponseEntity<UsuarioOutDTO> actualizarContrasena(
            @PathVariable long id,
            @Valid @RequestBody UsuarioPasswordDTO passwordDTO,
            Authentication authentication)
            throws UsuarioNoEncontradoException {

        String emailUsuario = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods = SecurityUtils.isMods(authentication);

        UsuarioOutDTO usuarioActualizado = usuarioService.actualizarContrasena(id, passwordDTO.getContrasena(), emailUsuario, esAdmin, esMods);
        return ResponseEntity.ok(usuarioActualizado);
    }

    @PatchMapping("/{id}/rol")
    public ResponseEntity<UsuarioOutDTO> actualizarRol(
            @PathVariable long id,
            @Valid @RequestBody UsuarioRolDTO dto,
            Authentication authentication
    ) throws UsuarioNoEncontradoException {

        String emailUsuario = SecurityUtils.email(authentication);

        UsuarioOutDTO actualizado = usuarioService.actualizarRol(id, dto.getRol(), emailUsuario);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrarUsuario(
            @PathVariable long id,
            Authentication authentication)
            throws UsuarioNoEncontradoException {

        String emailUsuario = SecurityUtils.email(authentication);
        boolean esAdmin = SecurityUtils.isAdmin(authentication);
        boolean esMods  = SecurityUtils.isMods(authentication);

        usuarioService.borrarUsuario(id, emailUsuario, esAdmin, esMods);
        return ResponseEntity.noContent().build();
    }
}
