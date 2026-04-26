package com.svalero.fancollector.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.fancollector.domain.enums.RarezaItem;
import com.svalero.fancollector.dto.ItemInDTO;
import com.svalero.fancollector.dto.ItemOutDTO;
import com.svalero.fancollector.dto.ItemPutDTO;
import com.svalero.fancollector.dto.patches.ItemRarezaDTO;
import com.svalero.fancollector.exception.domain.ColeccionNoEncontradaException;
import com.svalero.fancollector.exception.domain.ItemNoEncontradoException;
import com.svalero.fancollector.exception.validation.RelacionYaExisteException;
import com.svalero.fancollector.security.jwt.JwtService;
import com.svalero.fancollector.service.ItemService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@WithMockUser(username = "nerea@test.com", roles = {"USER"})
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private ImagenUtil imagenUtil;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void listarItems_sinFiltros_devuelve200() throws Exception {
        ItemOutDTO item1 = new ItemOutDTO();
        item1.setId(1L);
        item1.setNombre("Darkrai");
        item1.setTipo("Figura");
        item1.setRareza(RarezaItem.LEGENDARIO);

        ItemOutDTO item2 = new ItemOutDTO();
        item2.setId(2L);
        item2.setNombre("Pikachu");
        item2.setTipo("Carta");
        item2.setRareza(RarezaItem.RARO);

        List<ItemOutDTO> items = List.of(item1, item2);

        when(itemService.listarItems(eq(null), eq(null), eq(null), eq(null), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(items);
        mockMvc.perform(get("/items")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Darkrai"))
                .andExpect(jsonPath("$[1].nombre").value("Pikachu"));
    }

    @Test
    public void buscarItemPorId_existente_devuelve200() throws Exception {
        ItemOutDTO itemOutDTO = new ItemOutDTO();
        itemOutDTO.setId(1L);
        itemOutDTO.setNombre("Darkrai");
        itemOutDTO.setRareza(RarezaItem.LEGENDARIO);

        when(itemService.buscarItemPorId(eq(1L), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(itemOutDTO);
        mockMvc.perform(get("/items/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Darkrai"));
    }

    @Test
    public void buscarItemPorId_noExiste_devuelve404() throws Exception {
        when(itemService.buscarItemPorId(eq(999L), anyString(), anyBoolean(), anyBoolean()))
                .thenThrow(new ItemNoEncontradoException(999L));

        mockMvc.perform(get("/items/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void crearItem_datosValidos_devuelve201() throws Exception {
        ItemInDTO itemInDTO = new ItemInDTO();
        itemInDTO.setIdColeccion(1L);
        itemInDTO.setNombre("Darkrai");
        itemInDTO.setTipo("Figura");
        itemInDTO.setRareza("LEGENDARIO");

        ItemOutDTO savedDto = new ItemOutDTO();
        savedDto.setId(1L);
        savedDto.setNombre("Darkrai");
        savedDto.setRareza(RarezaItem.LEGENDARIO);

        when(itemService.crearItem(any(ItemInDTO.class), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(savedDto);

        mockMvc.perform(multipart("/items")
                        .param("idColeccion", "999")
                        .param("nombre", "Darkrai")
                        .param("tipo", "Figura")
                        .param("rareza", "LEGENDARIO")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Darkrai"));
    }

    @Test
    public void crearItem_bodyInvalido_devuelve400() throws Exception {
        mockMvc.perform(post("/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void crearItem_coleccionNoExiste_devuelve404() throws Exception {
        ItemInDTO itemInDTO = new ItemInDTO();
        itemInDTO.setIdColeccion(999L);
        itemInDTO.setNombre("Darkrai");
        itemInDTO.setTipo("Figura");
        itemInDTO.setRareza("LEGENDARIO");

        when(itemService.crearItem(any(ItemInDTO.class), anyString(), anyBoolean(), anyBoolean()))
                .thenThrow(new ColeccionNoEncontradaException(999L));

        mockMvc.perform(multipart("/items")
                        .param("idColeccion", "999")
                        .param("nombre", "Darkrai")
                        .param("tipo", "Figura")
                        .param("rareza", "LEGENDARIO")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void crearItem_relacionDuplicada_devuelve400() throws Exception {
        when(itemService.crearItem(any(ItemInDTO.class), anyString(), anyBoolean(), anyBoolean()))
                .thenThrow(new RelacionYaExisteException("El item ya existe en esta colección"));

        mockMvc.perform(multipart("/items")
                        .param("idColeccion", "1")
                        .param("nombre", "Darkrai")
                        .param("tipo", "Figura")
                        .param("rareza", "LEGENDARIO")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void modificarItem_existente_devuelve200() throws Exception {
        ItemPutDTO itemPutDTO = new ItemPutDTO();
        itemPutDTO.setNombre("Darkrai");
        itemPutDTO.setTipo("Figura Premium");
        itemPutDTO.setRareza("EPICO");

        ItemOutDTO response = new ItemOutDTO();
        response.setId(1L);
        response.setNombre("Darkrai");
        response.setRareza(RarezaItem.EPICO);

        ItemOutDTO itemActual = new ItemOutDTO();
        itemActual.setId(1L);
        when(itemService.buscarItemPorId(eq(1L), anyString(), anyBoolean(), anyBoolean())).thenReturn(itemActual);


        when(itemService.actualizarItem(eq(1L), any(ItemPutDTO.class), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(response);

        mockMvc.perform(multipart(HttpMethod.PUT, "/items/1")
                        .param("nombre", "Darkrai")
                        .param("tipo", "Figura Premium")
                        .param("rareza", "EPICO")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Darkrai"));
    }

    @Test
    public void modificarItem_noExiste_devuelve404() throws Exception {
        ItemOutDTO itemActual = new ItemOutDTO();
        itemActual.setId(1L);
        when(itemService.buscarItemPorId(eq(1L), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(itemActual);

        when(itemService.actualizarItem(eq(1L), any(ItemPutDTO.class), anyString(), anyBoolean(), anyBoolean()))
                .thenThrow(new ItemNoEncontradoException(1L));

        mockMvc.perform(multipart(HttpMethod.PUT, "/items/1")
                        .param("nombre", "Darkrai")
                        .param("tipo", "Figura")
                        .param("rareza", "COMUN")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void modificarItem_bodyInvalido_devuelve400() throws Exception {
        mockMvc.perform(put("/items/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void actualizarRareza_itemExistente_devuelve200() throws Exception {
        ItemRarezaDTO rarezaDTO = new ItemRarezaDTO();
        rarezaDTO.setRareza(RarezaItem.LEGENDARIO);

        ItemOutDTO response = new ItemOutDTO();
        response.setId(1L);
        response.setRareza(RarezaItem.LEGENDARIO);

        when(itemService.actualizarRareza(eq(1L), eq(RarezaItem.LEGENDARIO), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(response);
        mockMvc.perform(patch("/items/1/rareza")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rarezaDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rareza").value("LEGENDARIO"));
    }

    @Test
    public void actualizarRareza_itemNoExiste_devuelve404() throws Exception {
        ItemRarezaDTO rarezaDTO = new ItemRarezaDTO();
        rarezaDTO.setRareza(RarezaItem.LEGENDARIO);

        when(itemService.actualizarRareza(eq(999L), eq(RarezaItem.LEGENDARIO), anyString(), anyBoolean(), anyBoolean()))
                .thenThrow(new ItemNoEncontradoException(999L));

        mockMvc.perform(patch("/items/999/rareza")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rarezaDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void actualizarRareza_bodyInvalido_devuelve400() throws Exception {
        mockMvc.perform(patch("/items/1/rareza")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void eliminarItem_existente_devuelve204() throws Exception {
        mockMvc.perform(delete("/items/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void eliminarItem_noExiste_devuelve404() throws Exception {
        doThrow(new ItemNoEncontradoException(1L))
                .when(itemService).eliminarItem(eq(1L), anyString(), anyBoolean(), anyBoolean());

        mockMvc.perform(delete("/items/1")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}