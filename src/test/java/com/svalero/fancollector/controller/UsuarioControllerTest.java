package com.svalero.fancollector.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.fancollector.domain.enums.RolUsuario;
import com.svalero.fancollector.dto.UsuarioAdminOutDTO;
import com.svalero.fancollector.dto.UsuarioInDTO;
import com.svalero.fancollector.dto.UsuarioOutDTO;
import com.svalero.fancollector.dto.UsuarioPutDTO;
import com.svalero.fancollector.dto.patches.UsuarioPasswordDTO;
import com.svalero.fancollector.dto.patches.UsuarioRolDTO;
import com.svalero.fancollector.exception.domain.UsuarioNoEncontradoException;
import com.svalero.fancollector.exception.validation.EmailDuplicadoException;
import com.svalero.fancollector.security.jwt.JwtService;
import com.svalero.fancollector.service.UsuarioService;
import com.svalero.fancollector.util.ImagenUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@WithMockUser(username = "nerea@test.com", roles = {"ADMIN"})
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private
    JwtService jwtService;

    @MockitoBean
    private ImagenUtil imagenUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private UsuarioService usuarioService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void listarUsuarios_sinFiltros_devuelve200() throws Exception {
        UsuarioOutDTO usuario1 = new UsuarioOutDTO();
        usuario1.setId(1L);
        usuario1.setNombre("Nerea");
        usuario1.setRol(RolUsuario.USER);
        usuario1.setFechaRegistro(LocalDateTime.now());

        UsuarioOutDTO usuario2 = new UsuarioOutDTO();
        usuario2.setId(2L);
        usuario2.setNombre("Andrea");
        usuario2.setRol(RolUsuario.USER);
        usuario2.setFechaRegistro(LocalDateTime.now());

        List<UsuarioOutDTO> usuarios = List.of(usuario1, usuario2);

        when(usuarioService.listarUsuarios(eq(null), eq(null), eq(null))).thenReturn(usuarios);

        mockMvc.perform(get("/usuarios")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Nerea"))
                .andExpect(jsonPath("$[1].nombre").value("Andrea"));
    }

    @Test
    public void buscarUsuarioPorId_existente_devuelve200() throws Exception {
        UsuarioOutDTO usuarioOutDTO = new UsuarioOutDTO();
        usuarioOutDTO.setId(1L);
        usuarioOutDTO.setNombre("Nerea");
        usuarioOutDTO.setRol(RolUsuario.USER);

        when(usuarioService.buscarUsuarioPorId(1L)).thenReturn(usuarioOutDTO);

        mockMvc.perform(get("/usuarios/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Nerea"));
    }

    @Test
    public void buscarUsuarioPorId_noExiste_devuelve404() throws Exception {
        when(usuarioService.buscarUsuarioPorId(999L))
                .thenThrow(new UsuarioNoEncontradoException(999L));

        mockMvc.perform(get("/usuarios/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "nerea@test.com", roles = {"ADMIN"})
    public void obtenerUsuarioAdmin_existente_devuelve200() throws Exception {
        UsuarioAdminOutDTO adminDTO = new UsuarioAdminOutDTO();
        adminDTO.setId(1L);
        adminDTO.setNombre("Nerea");
        adminDTO.setEmail("nerea@test.com");
        adminDTO.setRol(RolUsuario.ADMIN);

        when(usuarioService.buscarUsuarioPorIdAdmin(1L)).thenReturn(adminDTO);

        mockMvc.perform(get("/usuarios/1/admin")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("nerea@test.com"));
    }

    @Test
    @WithMockUser(username = "nerea@test.com", roles = {"ADMIN"})
    public void obtenerUsuarioAdmin_noExiste_devuelve404() throws Exception {
        when(usuarioService.buscarUsuarioPorIdAdmin(999L))
                .thenThrow(new UsuarioNoEncontradoException(999L));

        mockMvc.perform(get("/usuarios/999/admin")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    public void crearUsuario_datosValidos_devuelve201() throws Exception {
        UsuarioInDTO usuarioInDTO = new UsuarioInDTO();
        usuarioInDTO.setNombre("Nerea");
        usuarioInDTO.setEmail("nerea@gmail.com");
        usuarioInDTO.setContrasena("password123");

        UsuarioOutDTO savedDto = new UsuarioOutDTO();
        savedDto.setId(1L);
        savedDto.setNombre("Nerea");
        savedDto.setRol(RolUsuario.USER);

        when(usuarioService.crearUsuarioComoAdmin(any(UsuarioInDTO.class), anyString(), anyBoolean()))
                .thenReturn(savedDto);

        mockMvc.perform(post("/usuarios")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioInDTO))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Nerea"));
    }

    @Test
    public void crearUsuario_bodyInvalido_devuelve400() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void crearUsuario_emailDuplicado_devuelve400() throws Exception {
        UsuarioInDTO usuarioInDTO = new UsuarioInDTO();
        usuarioInDTO.setNombre("Nerea");
        usuarioInDTO.setEmail("nerea@gmail.com");
        usuarioInDTO.setContrasena("password123");

        when(usuarioService.crearUsuarioComoAdmin(any(UsuarioInDTO.class), anyString(), anyBoolean()))
                .thenThrow(new EmailDuplicadoException("nerea@gmail.com"));

        mockMvc.perform(post("/usuarios")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioInDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void modificarUsuario_existente_devuelve200() throws Exception {
        UsuarioPutDTO usuarioPutDTO = new UsuarioPutDTO();
        usuarioPutDTO.setNombre("Nerea Modificada");
        usuarioPutDTO.setEmail("nerea@gmail.com");

        UsuarioOutDTO usuarioActual = new UsuarioOutDTO();
        usuarioActual.setId(1L);
        when(usuarioService.buscarUsuarioPorId(1L)).thenReturn(usuarioActual);


        UsuarioOutDTO response = new UsuarioOutDTO();
        response.setId(1L);
        response.setNombre("Nerea Modificada");

        when(usuarioService.modificarUsuario(
                eq(1L), any(UsuarioPutDTO.class), anyString(), anyBoolean(), anyBoolean()
        )).thenReturn(response);

        mockMvc.perform(multipart(HttpMethod.PUT, "/usuarios/1")
                        .param("nombre", "Nerea Modificada")
                        .param("email", "nerea@gmail.com")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nerea Modificada"));
    }

    @Test
    public void modificarUsuario_noExiste_devuelve404() throws Exception {
        UsuarioPutDTO usuarioPutDTO = new UsuarioPutDTO();
        usuarioPutDTO.setNombre("Nerea");
        usuarioPutDTO.setEmail("nerea@gmail.com");

        UsuarioOutDTO usuarioActual = new UsuarioOutDTO();
        usuarioActual.setId(1L);
        when(usuarioService.buscarUsuarioPorId(1L)).thenReturn(usuarioActual);


        when(usuarioService.modificarUsuario(eq(1L), any(UsuarioPutDTO.class), anyString(), anyBoolean(), anyBoolean()
        )).thenThrow(new UsuarioNoEncontradoException(1L));

        mockMvc.perform(multipart(HttpMethod.PUT, "/usuarios/1")
                        .param("nombre", "Nerea")
                        .param("email", "nerea@gmail.com")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void modificarUsuario_bodyInvalido_devuelve400() throws Exception {
        mockMvc.perform(put("/usuarios/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void actualizarContrasena_datosValidos_devuelve200() throws Exception {
        UsuarioPasswordDTO passwordDTO = new UsuarioPasswordDTO();
        passwordDTO.setContrasena("newpassword123");

        UsuarioOutDTO response = new UsuarioOutDTO();
        response.setId(1L);
        response.setNombre("Nerea");

        when(usuarioService.actualizarContrasena(eq(1L), eq("newpassword123"), anyString(), anyBoolean(), anyBoolean()
        )).thenReturn(response);

        mockMvc.perform(patch("/usuarios/1/contrasena")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void actualizarContrasena_noExiste_devuelve404() throws Exception {
        UsuarioPasswordDTO dto = new UsuarioPasswordDTO();
        dto.setContrasena("newpassword");

        when(usuarioService.actualizarContrasena(eq(999L), anyString(), anyString(), anyBoolean(), anyBoolean()))
                .thenThrow(new UsuarioNoEncontradoException(999L));

        mockMvc.perform(patch("/usuarios/999/contrasena")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void actualizarRol_datosValidos_devuelve200() throws Exception {
        UsuarioRolDTO rolDTO = new UsuarioRolDTO();
        rolDTO.setRol(RolUsuario.MODS);

        UsuarioOutDTO response = new UsuarioOutDTO();
        response.setId(1L);
        response.setNombre("Nerea");
        response.setRol(RolUsuario.MODS);

        when(usuarioService.actualizarRol(eq(1L), eq(RolUsuario.MODS), anyString()))
                .thenReturn(response);

        mockMvc.perform(patch("/usuarios/1/rol")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rolDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("MODS"));
    }

    @Test
    public void actualizarRol_noExiste_devuelve404() throws Exception {
        UsuarioRolDTO dto = new UsuarioRolDTO();
        dto.setRol(RolUsuario.MODS);

        when(usuarioService.actualizarRol(eq(999L), any(RolUsuario.class), anyString()))
                .thenThrow(new UsuarioNoEncontradoException(999L));

        mockMvc.perform(patch("/usuarios/999/rol")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void eliminarUsuario_existente_devuelve204() throws Exception {
        mockMvc.perform(delete("/usuarios/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void eliminarUsuario_noExiste_devuelve404() throws Exception {
        doThrow(new UsuarioNoEncontradoException(1L))
                .when(usuarioService).borrarUsuario(eq(1L), anyString(), anyBoolean(), anyBoolean());

        mockMvc.perform(delete("/usuarios/1")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}