package com.svalero.fancollector.controller;

import com.svalero.fancollector.domain.Usuario;
import com.svalero.fancollector.dto.LoginDTO;
import com.svalero.fancollector.dto.UsuarioInDTO;
import com.svalero.fancollector.dto.UsuarioOutDTO;
import com.svalero.fancollector.exception.domain.UsuarioNoEncontradoException;
import com.svalero.fancollector.repository.UsuarioRepository;
import com.svalero.fancollector.security.jwt.JwtService;
import com.svalero.fancollector.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/registro")
    public ResponseEntity<UsuarioOutDTO> register(@Valid @RequestBody UsuarioInDTO dto) {
        UsuarioOutDTO creado = usuarioService.crearUsuario(dto);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> token(@Valid @RequestBody LoginDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getContrasena()));
        String token = jwtService.generateToken(authentication.getName());

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> UsuarioNoEncontradoException.porEmail(authentication.getName()));

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("id", usuario.getId());
        response.put("email", usuario.getEmail());
        response.put("nombre", usuario.getNombre());
        response.put("rol", usuario.getRol().name());

        return ResponseEntity.ok(response);
    }
}
