package com.svalero.fancollector.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.fancollector.dto.UsuarioColeccionInDTO;
import com.svalero.fancollector.dto.UsuarioColeccionOutDTO;
import com.svalero.fancollector.dto.UsuarioColeccionPutDTO;
import com.svalero.fancollector.dto.patches.UsuarioColeccionFavoritaDTO;
import com.svalero.fancollector.dto.patches.UsuarioColeccionVisibleDTO;
import com.svalero.fancollector.exception.domain.UsuarioColeccionNoEncontradoException;
import com.svalero.fancollector.exception.domain.UsuarioNoEncontradoException;
import com.svalero.fancollector.exception.validation.RelacionYaExisteException;
import com.svalero.fancollector.security.jwt.JwtService;
import com.svalero.fancollector.service.UsuarioColeccionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioColeccionController.class)
@WithMockUser(username = "nerea@test.com", roles = {"USER"})
public class UsuarioColeccionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioColeccionService usuarioColeccionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void listarUsuarioColecciones_sinFiltros_devuelve200() throws Exception {
        UsuarioColeccionOutDTO uc1 = new UsuarioColeccionOutDTO();
        uc1.setId(1L);
        uc1.setIdUsuario(1L);
        uc1.setIdColeccion(1L);
        uc1.setEsFavorita(true);
        uc1.setEsCreador(false);
        uc1.setEsVisible(true);
        uc1.setFechaAgregada(LocalDateTime.now());

        UsuarioColeccionOutDTO uc2 = new UsuarioColeccionOutDTO();
        uc2.setId(2L);
        uc2.setIdUsuario(1L);
        uc2.setIdColeccion(2L);
        uc2.setEsFavorita(false);
        uc2.setEsCreador(true);
        uc2.setEsVisible(true);
        uc2.setFechaAgregada(LocalDateTime.now());

        List<UsuarioColeccionOutDTO> lista = List.of(uc1, uc2);

        when(usuarioColeccionService.listar(isNull(), isNull(), isNull(), isNull(), anyString(), anyBoolean(), anyBoolean())).
                thenReturn(lista);
        mockMvc.perform(get("/usuario-colecciones")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    public void buscarUsuarioColeccionPorId_existente_devuelve200() throws Exception {
        UsuarioColeccionOutDTO dto = new UsuarioColeccionOutDTO();
        dto.setId(1L);
        dto.setIdUsuario(1L);
        dto.setIdColeccion(1L);
        dto.setEsFavorita(true);

        when(usuarioColeccionService.buscarPorId(eq(1L), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(dto);

        mockMvc.perform(get("/usuario-colecciones/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.esFavorita").value(true));
    }

    @Test
    public void buscarUsuarioColeccionPorId_noExiste_devuelve404() throws Exception {
        when(usuarioColeccionService.buscarPorId(eq(999L), anyString(), anyBoolean(), anyBoolean()))
                .thenThrow(new UsuarioColeccionNoEncontradoException(999L));

        mockMvc.perform(get("/usuario-colecciones/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void crearUsuarioColeccion_datosValidos_devuelve201() throws Exception {
        UsuarioColeccionInDTO inDTO = new UsuarioColeccionInDTO();
        inDTO.setIdUsuario(1L);
        inDTO.setIdColeccion(1L);
        inDTO.setEsFavorita(true);

        UsuarioColeccionOutDTO outDTO = new UsuarioColeccionOutDTO();
        outDTO.setId(1L);
        outDTO.setIdUsuario(1L);
        outDTO.setIdColeccion(1L);
        outDTO.setEsFavorita(true);

        when(usuarioColeccionService.crear(any(UsuarioColeccionInDTO.class), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(outDTO);

        mockMvc.perform(post("/usuario-colecciones")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inDTO))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.esFavorita").value(true));
    }

    @Test
    public void crearUsuarioColeccion_bodyInvalido_devuelve400() throws Exception {
        mockMvc.perform(post("/usuario-colecciones")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void crearUsuarioColeccion_usuarioNoExiste_devuelve404() throws Exception {
        UsuarioColeccionInDTO inDTO = new UsuarioColeccionInDTO();
        inDTO.setIdUsuario(999L);
        inDTO.setIdColeccion(1L);

        when(usuarioColeccionService.crear(any(UsuarioColeccionInDTO.class), anyString(), anyBoolean(), anyBoolean()))
                .thenThrow(new UsuarioNoEncontradoException(999L));

        mockMvc.perform(post("/usuario-colecciones")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void crearUsuarioColeccion_coleccionNoExiste_devuelve404() throws Exception {
        UsuarioColeccionInDTO inDTO = new UsuarioColeccionInDTO();
        inDTO.setIdUsuario(1L);
        inDTO.setIdColeccion(999L);

        when(usuarioColeccionService.crear(any(UsuarioColeccionInDTO.class), anyString(), anyBoolean(), anyBoolean()))
                .thenThrow(new UsuarioNoEncontradoException(999L));

        mockMvc.perform(post("/usuario-colecciones")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void crearUsuarioColeccion_relacionDuplicada_devuelve400() throws Exception {
        when(usuarioColeccionService.crear(any(UsuarioColeccionInDTO.class), anyString(), anyBoolean(), anyBoolean()))
                .thenThrow(new RelacionYaExisteException("La relación ya existe"));

        mockMvc.perform(post("/usuario-colecciones")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UsuarioColeccionInDTO())))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void modificarUsuarioColeccion_existente_devuelve200() throws Exception {
        UsuarioColeccionPutDTO putDTO = new UsuarioColeccionPutDTO();
        putDTO.setEsFavorita(true);

        UsuarioColeccionOutDTO outDTO = new UsuarioColeccionOutDTO();
        outDTO.setId(1L);
        outDTO.setEsFavorita(true);

        when(usuarioColeccionService.actualizar(eq(1L), any(UsuarioColeccionPutDTO.class), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(outDTO);

        mockMvc.perform(put("/usuario-colecciones/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.esFavorita").value(true));
    }

    @Test
    public void modificarUsuarioColeccion_noExiste_devuelve404() throws Exception {
        UsuarioColeccionPutDTO putDTO = new UsuarioColeccionPutDTO();
        putDTO.setEsFavorita(true);

        when(usuarioColeccionService.actualizar(eq(1L), any(UsuarioColeccionPutDTO.class), anyString(), anyBoolean(), anyBoolean()))
                .thenThrow(new UsuarioColeccionNoEncontradoException(1L));

        mockMvc.perform(put("/usuario-colecciones/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void modificarUsuarioColeccion_jsonInvalido_devuelve500() throws Exception {
        mockMvc.perform(put("/usuario-colecciones/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"esFavorita\": \"no es un booleano\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500));
    }

    @Test
    public void actualizarFavorita_relacionExistente_devuelve200() throws Exception {
        UsuarioColeccionFavoritaDTO favDTO = new UsuarioColeccionFavoritaDTO();
        favDTO.setEsFavorita(true);

        UsuarioColeccionOutDTO outDTO = new UsuarioColeccionOutDTO();
        outDTO.setId(1L);
        outDTO.setEsFavorita(true);

        when(usuarioColeccionService.actualizarFavorita(eq(1L), any(UsuarioColeccionFavoritaDTO.class), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(outDTO);

        mockMvc.perform(patch("/usuario-colecciones/1/favorita")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(favDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.esFavorita").value(true));
    }

    @Test
    public void actualizarFavorita_noExiste_devuelve404() throws Exception {
        UsuarioColeccionFavoritaDTO favDTO = new UsuarioColeccionFavoritaDTO();
        favDTO.setEsFavorita(true);

        when(usuarioColeccionService.actualizarFavorita(eq(999L),any(UsuarioColeccionFavoritaDTO.class),anyString(),anyBoolean(),anyBoolean()))
                .thenThrow(new UsuarioColeccionNoEncontradoException(999L));

        mockMvc.perform(patch("/usuario-colecciones/999/favorita")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(favDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void actualizarFavorita_bodyInvalido_devuelve400() throws Exception {
        mockMvc.perform(patch("/usuario-colecciones/1/favorita")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void actualizarVisible_relacionExistente_devuelve200() throws Exception {
        UsuarioColeccionVisibleDTO visibleDTO = new UsuarioColeccionVisibleDTO();
        visibleDTO.setEsVisible(false);

        UsuarioColeccionOutDTO outDTO = new UsuarioColeccionOutDTO();
        outDTO.setId(1L);
        outDTO.setEsVisible(false);

        when(usuarioColeccionService.actualizarVisible(eq(1L), any(UsuarioColeccionVisibleDTO.class), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(outDTO);

        mockMvc.perform(patch("/usuario-colecciones/1/visible")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(visibleDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.esVisible").value(false));
    }

    @Test
    public void actualizarVisible_noExiste_devuelve404() throws Exception {
        UsuarioColeccionVisibleDTO visibleDTO = new UsuarioColeccionVisibleDTO();
        visibleDTO.setEsVisible(true);

        when(usuarioColeccionService.actualizarVisible(eq(999L),any(UsuarioColeccionVisibleDTO.class),anyString(),anyBoolean(),anyBoolean()))
                .thenThrow(new UsuarioColeccionNoEncontradoException(999L));

        mockMvc.perform(patch("/usuario-colecciones/999/visible")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(visibleDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void actualizarVisible_bodyInvalido_devuelve400() throws Exception {
        mockMvc.perform(patch("/usuario-colecciones/1/visible")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // v1 eliminar
    @Test
    public void eliminarUsuarioColeccion_existente_devuelve204() throws Exception {
        mockMvc.perform(delete("/usuario-colecciones/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void eliminarUsuarioColeccion_noExiste_devuelve404() throws Exception {
        doThrow(new UsuarioColeccionNoEncontradoException(1L))
                .when(usuarioColeccionService).eliminar(eq(1L));

        mockMvc.perform(delete("/usuario-colecciones/1")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    //v2 eliminar
    @Test
    public void eliminarUsuarioColeccionV2_existente_devuelve204() throws Exception {
        mockMvc.perform(delete("/usuario-colecciones/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void eliminarUsuarioColeccionV2_noExiste_devuelve404() throws Exception {
        doThrow(new UsuarioColeccionNoEncontradoException(1L))
                .when(usuarioColeccionService).eliminarV2(eq(1L),anyString(),anyBoolean(),anyBoolean());

        mockMvc.perform(delete("/usuario-colecciones/1")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}