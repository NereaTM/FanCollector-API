package com.svalero.fancollector.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.fancollector.domain.enums.EstadoItem;
import com.svalero.fancollector.dto.UsuarioItemInDTO;
import com.svalero.fancollector.dto.UsuarioItemOutDTO;
import com.svalero.fancollector.dto.UsuarioItemPutDTO;
import com.svalero.fancollector.dto.patches.UsuarioItemVisibleDTO;
import com.svalero.fancollector.exception.domain.ColeccionNoEncontradaException;
import com.svalero.fancollector.exception.domain.ItemNoEncontradoException;
import com.svalero.fancollector.exception.domain.UsuarioItemNoEncontradoException;
import com.svalero.fancollector.exception.domain.UsuarioNoEncontradoException;
import com.svalero.fancollector.exception.validation.RelacionYaExisteException;
import com.svalero.fancollector.security.jwt.JwtService;
import com.svalero.fancollector.service.UsuarioItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
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

@WebMvcTest(UsuarioItemController.class)
@WithMockUser(username = "nerea@test.com", roles = {"USER"})
public class UsuarioItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private
    JwtService jwtService;

    @MockitoBean private
    UserDetailsService userDetailsService;

    @MockitoBean
    private UsuarioItemService usuarioItemService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void listarUsuarioItems_sinFiltros_devuelve200() throws Exception {
        UsuarioItemOutDTO ui1 = new UsuarioItemOutDTO();
        ui1.setId(1L);
        ui1.setIdUsuario(1L);
        ui1.setNombreUsuario("Nerea");
        ui1.setIdItem(1L);
        ui1.setNombreItem("Darkrai");
        ui1.setEstado(EstadoItem.TENGO);
        ui1.setCantidad(2);
        ui1.setEsVisible(true);
        ui1.setFechaRegistro(LocalDateTime.now());

        UsuarioItemOutDTO ui2 = new UsuarioItemOutDTO();
        ui2.setId(2L);
        ui2.setIdUsuario(1L);
        ui2.setNombreUsuario("Nerea");
        ui2.setIdItem(2L);
        ui2.setNombreItem("Pikachu");
        ui2.setEstado(EstadoItem.BUSCO);
        ui2.setCantidad(0);
        ui2.setEsVisible(true);
        ui2.setFechaRegistro(LocalDateTime.now());

        List<UsuarioItemOutDTO> lista = List.of(ui1, ui2);

        when(usuarioItemService.listar(isNull(), isNull(), isNull(), isNull(), isNull(), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(lista);

        mockMvc.perform(get("/usuario-items")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].estado").value("TENGO"))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    public void buscarUsuarioItemPorId_existente_devuelve200() throws Exception {
        UsuarioItemOutDTO dto = new UsuarioItemOutDTO();
        dto.setId(1L);
        dto.setIdUsuario(1L);
        dto.setNombreUsuario("Nerea");
        dto.setIdItem(1L);
        dto.setNombreItem("Darkrai");
        dto.setEstado(EstadoItem.TENGO);
        dto.setCantidad(2);
        dto.setEsVisible(true);

        when(usuarioItemService.buscarPorId(eq(1L), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(dto);

        mockMvc.perform(get("/usuario-items/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.estado").value("TENGO"))
                .andExpect(jsonPath("$.cantidad").value(2));
    }

    @Test
    public void buscarUsuarioItemPorId_noExiste_devuelve404() throws Exception {
        when(usuarioItemService.buscarPorId(eq(999L), anyString(), anyBoolean(), anyBoolean()))
                .thenThrow(new UsuarioItemNoEncontradoException(999L));

        mockMvc.perform(get("/usuario-items/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void crearUsuarioItem_datosValidos_devuelve201() throws Exception {
        UsuarioItemInDTO inDTO = new UsuarioItemInDTO();
        inDTO.setIdUsuario(1L);
        inDTO.setIdItem(1L);
        inDTO.setIdColeccion(1L);
        inDTO.setEstado(EstadoItem.TENGO);
        inDTO.setCantidad(2);
        inDTO.setEsVisible(true);

        UsuarioItemOutDTO outDTO = new UsuarioItemOutDTO();
        outDTO.setId(1L);
        outDTO.setIdUsuario(1L);
        outDTO.setIdItem(1L);
        outDTO.setIdColeccion(1L);
        outDTO.setEstado(EstadoItem.TENGO);
        outDTO.setCantidad(2);

        when(usuarioItemService.crear(any(UsuarioItemInDTO.class), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(outDTO);

        mockMvc.perform(post("/usuario-items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inDTO))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.estado").value("TENGO"))
                .andExpect(jsonPath("$.cantidad").value(2));
    }

    @Test
    public void crearUsuarioItem_bodyInvalido_devuelve400() throws Exception {
        mockMvc.perform(post("/usuario-items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void crearUsuarioItem_usuarioNoExiste_devuelve404() throws Exception {
        UsuarioItemInDTO inDTO = new UsuarioItemInDTO();
        inDTO.setIdUsuario(999L);
        inDTO.setIdItem(1L);
        inDTO.setIdColeccion(1L);

        when(usuarioItemService.crear(any(UsuarioItemInDTO.class), anyString(), anyBoolean(), anyBoolean()))
                .thenThrow(new UsuarioNoEncontradoException(999L));

        mockMvc.perform(post("/usuario-items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void crearUsuarioItem_itemNoExiste_devuelve404() throws Exception {
        UsuarioItemInDTO inDTO = new UsuarioItemInDTO();
        inDTO.setIdUsuario(1L);
        inDTO.setIdItem(999L);
        inDTO.setIdColeccion(1L);

        when(usuarioItemService.crear(any(UsuarioItemInDTO.class), anyString(), anyBoolean(), anyBoolean()))
                .thenThrow(new ItemNoEncontradoException(999L));

        mockMvc.perform(post("/usuario-items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void crearUsuarioItem_coleccionNoExiste_devuelve404() throws Exception {
        UsuarioItemInDTO inDTO = new UsuarioItemInDTO();
        inDTO.setIdUsuario(1L);
        inDTO.setIdItem(1L);
        inDTO.setIdColeccion(999L);

        when(usuarioItemService.crear(any(UsuarioItemInDTO.class), anyString(), anyBoolean(), anyBoolean()))
                .thenThrow(new ColeccionNoEncontradaException(999L));

        mockMvc.perform(post("/usuario-items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void crearUsuarioItem_relacionDuplicada_devuelve400() throws Exception {
        when(usuarioItemService.crear(any(UsuarioItemInDTO.class), anyString(), anyBoolean(), anyBoolean()))
                .thenThrow(new RelacionYaExisteException("La relación ya existe"));

        mockMvc.perform(post("/usuario-items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UsuarioItemInDTO())))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void modificarUsuarioItem_existente_devuelve200() throws Exception {
        UsuarioItemPutDTO putDTO = new UsuarioItemPutDTO();
        putDTO.setEstado(EstadoItem.EN_CAMINO);
        putDTO.setCantidad(3);
        putDTO.setEsVisible(false);

        UsuarioItemOutDTO outDTO = new UsuarioItemOutDTO();
        outDTO.setId(1L);
        outDTO.setEstado(EstadoItem.EN_CAMINO);
        outDTO.setCantidad(3);
        outDTO.setEsVisible(false);

        when(usuarioItemService.actualizarCompleto(eq(1L), any(UsuarioItemPutDTO.class), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(outDTO);

        mockMvc.perform(put("/usuario-items/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_CAMINO"))
                .andExpect(jsonPath("$.cantidad").value(3))
                .andExpect(jsonPath("$.esVisible").value(false));
    }

    @Test
    public void modificarUsuarioItem_noExiste_devuelve404() throws Exception {
        UsuarioItemPutDTO putDTO = new UsuarioItemPutDTO();
        putDTO.setEstado(EstadoItem.TENGO);

        when(usuarioItemService.actualizarCompleto(eq(1L), any(UsuarioItemPutDTO.class), anyString(), anyBoolean(), anyBoolean()))
                .thenThrow(new UsuarioItemNoEncontradoException(1L));

        mockMvc.perform(put("/usuario-items/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void modificarUsuarioItem_jsonInvalido_devuelve500() throws Exception {
        mockMvc.perform(put("/usuario-items/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cantidad\": \"no es un número\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500));
    }

    @Test
    public void actualizarVisibilidad_itemExistente_devuelve200() throws Exception {
        UsuarioItemVisibleDTO visibleDTO = new UsuarioItemVisibleDTO();
        visibleDTO.setEsVisible(false);

        UsuarioItemOutDTO outDTO = new UsuarioItemOutDTO();
        outDTO.setId(1L);
        outDTO.setEsVisible(false);

        when(usuarioItemService.actualizarVisibilidad(eq(1L), eq(false), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(outDTO);

        mockMvc.perform(patch("/usuario-items/1/visible")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(visibleDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.esVisible").value(false));
    }

    @Test
    public void actualizarVisibilidad_itemNoExiste_devuelve404() throws Exception {
        UsuarioItemVisibleDTO visibleDTO = new UsuarioItemVisibleDTO();
        visibleDTO.setEsVisible(false);

        when(usuarioItemService.actualizarVisibilidad(eq(999L), eq(false), anyString(), anyBoolean(), anyBoolean()))
                .thenThrow(new UsuarioItemNoEncontradoException(999L));

        mockMvc.perform(patch("/usuario-items/999/visible")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(visibleDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void actualizarVisibilidad_bodyInvalido_devuelve400() throws Exception {
        mockMvc.perform(patch("/usuario-items/1/visible")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void eliminarUsuarioItem_existente_devuelve204() throws Exception {
        mockMvc.perform(delete("/usuario-items/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void eliminarUsuarioItem_noExiste_devuelve404() throws Exception {
        doThrow(new UsuarioItemNoEncontradoException(1L))
                .when(usuarioItemService).eliminar(eq(1L), anyString(), anyBoolean(), anyBoolean());

        mockMvc.perform(delete("/usuario-items/1")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void buscarPorUsuarioYColeccionV2_existente_devuelve200() throws Exception {
        com.svalero.fancollector.dto.UsuarioItemDetalleDTO dto =
                new com.svalero.fancollector.dto.UsuarioItemDetalleDTO();
        dto.setId(1L);
        dto.setIdUsuario(1L);
        dto.setNombreUsuario("Nerea");
        dto.setIdItem(1L);
        dto.setNombreItem("Darkrai");
        dto.setEstado(EstadoItem.TENGO);
        dto.setCantidad(2);
        dto.setEsVisible(true);
        dto.setDescripcionItem("Figura legendaria");
        dto.setTipo("Figura");

        when(usuarioItemService.buscarPorUsuarioYColeccion(eq(1L), eq(1L), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/usuario-items/v2")
                        .param("idUsuario", "1")
                        .param("idColeccion", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].estado").value("TENGO"))
                .andExpect(jsonPath("$[0].descripcionItem").value("Figura legendaria"))
                .andExpect(jsonPath("$[0].tipo").value("Figura"));
    }

    @Test
    public void buscarPorUsuarioYColeccionV2_sinResultados_devuelve200ListaVacia() throws Exception {
        when(usuarioItemService.buscarPorUsuarioYColeccion(eq(1L), eq(99L), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(List.of());

        mockMvc.perform(get("/usuario-items/v2")
                        .param("idUsuario", "1")
                        .param("idColeccion", "99")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void buscarPorUsuarioYColeccionV2_sinParametros_devuelve500() throws Exception {
        mockMvc.perform(get("/usuario-items/v2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void actualizarCompletoV2_estadoBusco_devuelve200ConCantidadCero() throws Exception {
        UsuarioItemPutDTO putDTO = new UsuarioItemPutDTO();
        putDTO.setEstado(EstadoItem.BUSCO);
        putDTO.setEsVisible(true);
        putDTO.setNotas("Lo sigo buscando");

        UsuarioItemOutDTO outDTO = new UsuarioItemOutDTO();
        outDTO.setId(1L);
        outDTO.setEstado(EstadoItem.BUSCO);
        outDTO.setCantidad(0);
        outDTO.setEsVisible(true);

        when(usuarioItemService.actualizarCompletoV2(eq(1L), any(UsuarioItemPutDTO.class), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(outDTO);

        mockMvc.perform(put("/usuario-items/v2/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("BUSCO"))
                .andExpect(jsonPath("$.cantidad").value(0));
    }

    @Test
    public void actualizarCompletoV2_estadoTengo_devuelve200ConCantidadUno() throws Exception {
        UsuarioItemPutDTO putDTO = new UsuarioItemPutDTO();
        putDTO.setEstado(EstadoItem.TENGO);
        putDTO.setEsVisible(true);

        UsuarioItemOutDTO outDTO = new UsuarioItemOutDTO();
        outDTO.setId(1L);
        outDTO.setEstado(EstadoItem.TENGO);
        outDTO.setCantidad(1);
        outDTO.setEsVisible(true);

        when(usuarioItemService.actualizarCompletoV2(eq(1L), any(UsuarioItemPutDTO.class), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(outDTO);

        mockMvc.perform(put("/usuario-items/v2/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("TENGO"))
                .andExpect(jsonPath("$.cantidad").value(1));
    }

    @Test
    public void actualizarCompletoV2_noExiste_devuelve404() throws Exception {
        UsuarioItemPutDTO putDTO = new UsuarioItemPutDTO();
        putDTO.setEstado(EstadoItem.TENGO);

        when(usuarioItemService.actualizarCompletoV2(eq(999L), any(UsuarioItemPutDTO.class), anyString(), anyBoolean(), anyBoolean()))
                .thenThrow(new UsuarioItemNoEncontradoException(999L));

        mockMvc.perform(put("/usuario-items/v2/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isNotFound());
    }
}