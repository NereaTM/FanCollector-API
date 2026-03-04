package com.svalero.fancollector.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.fancollector.dto.ColeccionInDTO;
import com.svalero.fancollector.dto.ColeccionOutDTO;
import com.svalero.fancollector.dto.ColeccionPutDTO;
import com.svalero.fancollector.dto.patches.ColeccionPlantillaDTO;
import com.svalero.fancollector.dto.patches.ColeccionPublicoDTO;
import com.svalero.fancollector.exception.domain.ColeccionNoEncontradaException;
import com.svalero.fancollector.security.jwt.JwtService;
import com.svalero.fancollector.service.ColeccionService;
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

@WebMvcTest(ColeccionController.class)
@WithMockUser(username = "nerea@test.com", roles = {"ADMIN"})
public class ColeccionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private ImagenUtil imagenUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private ColeccionService coleccionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void listarColecciones_sinFiltros_devuelve200() throws Exception {
        ColeccionOutDTO coleccion1 = new ColeccionOutDTO();
        coleccion1.setId(1L);
        coleccion1.setNombre("Figuras Anime");
        coleccion1.setCategoria("Anime");
        coleccion1.setIdCreador(1L);
        coleccion1.setNombreCreador("Nerea");
        coleccion1.setFechaCreacion(LocalDateTime.now());

        ColeccionOutDTO coleccion2 = new ColeccionOutDTO();
        coleccion2.setId(2L);
        coleccion2.setNombre("Trading Cards");
        coleccion2.setCategoria("Cartas");
        coleccion2.setIdCreador(1L);
        coleccion2.setNombreCreador("Nerea");
        coleccion2.setFechaCreacion(LocalDateTime.now());

        List<ColeccionOutDTO> colecciones = List.of(coleccion1, coleccion2);

        when(coleccionService.listarColecciones(eq(null), eq(null), eq(null), eq(null), anyString(), anyBoolean(), anyBoolean(), eq(null)))
                .thenReturn(colecciones);

        mockMvc.perform(get("/colecciones")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Figuras Anime"))
                .andExpect(jsonPath("$[1].nombre").value("Trading Cards"));
    }

    @Test
    public void buscarColeccionPorId_existente_devuelve200() throws Exception {
        ColeccionOutDTO coleccionOutDTO = new ColeccionOutDTO();
        coleccionOutDTO.setId(1L);
        coleccionOutDTO.setNombre("Figuras Anime");
        coleccionOutDTO.setCategoria("Anime");

        when(coleccionService.buscarColeccionPorId(eq(1L), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(coleccionOutDTO);

        mockMvc.perform(get("/colecciones/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Figuras Anime"));
    }

    @Test
    public void buscarColeccionPorId_noExiste_devuelve404() throws Exception {
        when(coleccionService.buscarColeccionPorId(eq(999L), anyString(), anyBoolean(), anyBoolean()))
                .thenThrow(new ColeccionNoEncontradaException(999L));
        mockMvc.perform(get("/colecciones/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void crearColeccion_datosValidos_devuelve201() throws Exception {
        ColeccionInDTO coleccionInDTO = new ColeccionInDTO();
        coleccionInDTO.setIdCreador(1L);
        coleccionInDTO.setNombre("Figuras Anime");
        coleccionInDTO.setCategoria("Anime");
        coleccionInDTO.setDescripcion("Colección de figuras");

        ColeccionOutDTO savedDto = new ColeccionOutDTO();
        savedDto.setId(1L);
        savedDto.setNombre("Figuras Anime");
        savedDto.setCategoria("Anime");

        when(coleccionService.crearColeccion(any(ColeccionInDTO.class), anyString()))
                .thenReturn(savedDto);

        mockMvc.perform(multipart("/colecciones")
                        .param("idCreador", "1")
                        .param("nombre", "Figuras Anime")
                        .param("categoria", "Anime")
                        .param("descripcion", "Colección de figuras")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Figuras Anime"));
    }

    @Test
    public void crearColeccion_bodyInvalido_devuelve400() throws Exception {
        mockMvc.perform(post("/colecciones")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void modificarColeccion_existente_devuelve200() throws Exception {

        ColeccionPutDTO coleccionPutDTO = new ColeccionPutDTO();
        coleccionPutDTO.setNombre("Figuras Anime Actualizado");
        coleccionPutDTO.setCategoria("Anime");

        ColeccionOutDTO response = new ColeccionOutDTO();
        response.setId(1L);
        response.setNombre("Figuras Anime Actualizado");

        ColeccionOutDTO coleccionActual = new ColeccionOutDTO();
        coleccionActual.setId(1L);
        when(coleccionService.buscarColeccionPorId(eq(1L), anyString(), anyBoolean(), anyBoolean())).thenReturn(coleccionActual);

        when(coleccionService.actualizarColeccion(
                eq(1L), any(ColeccionPutDTO.class), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(response);

        mockMvc.perform(multipart(HttpMethod.PUT, "/colecciones/1")
                        .param("nombre", "Figuras Anime Actualizado")
                        .param("categoria", "Anime")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Figuras Anime Actualizado"));
    }

    @Test
    public void modificarColeccion_noExiste_devuelve404() throws Exception {
        ColeccionPutDTO coleccionPutDTO = new ColeccionPutDTO();
        coleccionPutDTO.setNombre("Figuras Anime");
        coleccionPutDTO.setCategoria("Anime");

        ColeccionOutDTO coleccionActual = new ColeccionOutDTO();
        coleccionActual.setId(1L);
        when(coleccionService.buscarColeccionPorId(eq(1L), anyString(), anyBoolean(), anyBoolean())).thenReturn(coleccionActual);

        when(coleccionService.actualizarColeccion(
                eq(1L), any(ColeccionPutDTO.class), anyString(), anyBoolean(), anyBoolean()))
                .thenThrow(new ColeccionNoEncontradaException(1L));

        mockMvc.perform(multipart(HttpMethod.PUT, "/colecciones/1")
                        .param("nombre", "Figuras Anime")
                        .param("categoria", "Anime")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void modificarColeccion_bodyInvalido_devuelve400() throws Exception {
        mockMvc.perform(put("/colecciones/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void actualizarEsPublica_coleccionExistente_devuelve200() throws Exception {
        ColeccionPublicoDTO publicoDTO = new ColeccionPublicoDTO();
        publicoDTO.setEsPublica(true);

        ColeccionOutDTO response = new ColeccionOutDTO();
        response.setId(1L);
        response.setEsPublica(true);

        when(coleccionService.actualizarEsPublica(eq(1L), eq(true), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(response);
        mockMvc.perform(patch("/colecciones/1/publico")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(publicoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.esPublica").value(true));
    }

    @Test
    public void actualizarEsPublica_bodyInvalido_devuelve400() throws Exception {
        mockMvc.perform(patch("/colecciones/1/publico")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void actualizarEsPublica_coleccionNoExiste_devuelve404() throws Exception {
        ColeccionPublicoDTO publicoDTO = new ColeccionPublicoDTO();
        publicoDTO.setEsPublica(true);

        when(coleccionService.actualizarEsPublica(eq(999L), eq(true), anyString(), anyBoolean(), anyBoolean()))
                .thenThrow(new ColeccionNoEncontradaException(999L));

        mockMvc.perform(patch("/colecciones/999/publico")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(publicoDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void actualizarUsableComoPlantilla_coleccionExistente_devuelve200() throws Exception {
        ColeccionPlantillaDTO plantillaDTO = new ColeccionPlantillaDTO();
        plantillaDTO.setUsableComoPlantilla(true);

        ColeccionOutDTO response = new ColeccionOutDTO();
        response.setId(1L);
        response.setUsableComoPlantilla(true);

        when(coleccionService.actualizarUsableComoPlantilla(eq(1L), eq(true), anyString(), anyBoolean()))
                .thenReturn(response);
        mockMvc.perform(patch("/colecciones/1/plantilla")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(plantillaDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usableComoPlantilla").value(true));
    }

    @Test
    public void actualizarUsableComoPlantilla_coleccionNoExiste_devuelve404() throws Exception {
        ColeccionPlantillaDTO plantillaDTO = new ColeccionPlantillaDTO();
        plantillaDTO.setUsableComoPlantilla(true);

        when(coleccionService.actualizarUsableComoPlantilla(eq(999L), eq(true), anyString(), anyBoolean()))
                .thenThrow(new ColeccionNoEncontradaException(999L));

        mockMvc.perform(patch("/colecciones/999/plantilla")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(plantillaDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void actualizarUsableComoPlantilla_bodyInvalido_devuelve400() throws Exception {
        mockMvc.perform(patch("/colecciones/1/plantilla")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void eliminarColeccion_existente_devuelve204() throws Exception {
        mockMvc.perform(delete("/colecciones/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void eliminarColeccion_noExiste_devuelve404() throws Exception {
        doThrow(new ColeccionNoEncontradaException(1L))
                .when(coleccionService).eliminarColeccion(eq(1L), anyString(), anyBoolean(), anyBoolean());

        mockMvc.perform(delete("/colecciones/1")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}