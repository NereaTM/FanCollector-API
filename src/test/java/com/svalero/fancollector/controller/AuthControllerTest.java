package com.svalero.fancollector.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.fancollector.domain.Usuario;
import com.svalero.fancollector.domain.enums.RolUsuario;
import com.svalero.fancollector.dto.LoginDTO;
import com.svalero.fancollector.dto.UsuarioInDTO;
import com.svalero.fancollector.dto.UsuarioOutDTO;
import com.svalero.fancollector.exception.validation.EmailDuplicadoException;
import com.svalero.fancollector.repository.UsuarioRepository;
import com.svalero.fancollector.security.jwt.JwtService;
import com.svalero.fancollector.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@WithMockUser
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void registro_datosValidos_devuelve201() throws Exception {
        UsuarioInDTO inDTO = new UsuarioInDTO();
        inDTO.setNombre("Nerea");
        inDTO.setEmail("nerea@gmail.com");
        inDTO.setContrasena("password123");

        UsuarioOutDTO outDTO = new UsuarioOutDTO();
        outDTO.setId(1L);
        outDTO.setNombre("Nerea");
        outDTO.setRol(RolUsuario.USER);

        when(usuarioService.crearUsuario(any(UsuarioInDTO.class))).thenReturn(outDTO);

        mockMvc.perform(post("/auth/registro")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Nerea"));
    }

    @Test
    public void registro_bodyInvalido_devuelve400() throws Exception {
        mockMvc.perform(post("/auth/registro")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registro_emailDuplicado_devuelve400() throws Exception {
        UsuarioInDTO inDTO = new UsuarioInDTO();
        inDTO.setNombre("Nerea");
        inDTO.setEmail("nerea@gmail.com");
        inDTO.setContrasena("password123");

        when(usuarioService.crearUsuario(any(UsuarioInDTO.class)))
                .thenThrow(new EmailDuplicadoException("nerea@gmail.com"));

        mockMvc.perform(post("/auth/registro")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void login_credencialesValidas_devuelve200() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("nerea@gmail.com");
        loginDTO.setContrasena("password123");

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("nerea@gmail.com");
        usuario.setNombre("Nerea");
        usuario.setRol(RolUsuario.USER);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken("nerea@gmail.com", null);

        when(authenticationManager.authenticate(any())).thenReturn(authToken);
        when(usuarioRepository.findByEmail("nerea@gmail.com")).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(eq("nerea@gmail.com"), eq("USER"), eq (1L))).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.email").value("nerea@gmail.com"))
                .andExpect(jsonPath("$.nombre").value("Nerea"))
                .andExpect(jsonPath("$.rol").value("USER"));
    }

    @Test
    public void login_credencialesInvalidas_devuelve401() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("nerea@gmail.com");
        loginDTO.setContrasena("wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciales incorrectas"));

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void login_bodyInvalido_devuelve400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}