package com.svalero.fancollector.service;

import com.svalero.fancollector.domain.*;
import com.svalero.fancollector.dto.UsuarioColeccionInDTO;
import com.svalero.fancollector.dto.UsuarioColeccionOutDTO;
import com.svalero.fancollector.dto.UsuarioColeccionPutDTO;
import com.svalero.fancollector.dto.patches.UsuarioColeccionFavoritaDTO;
import com.svalero.fancollector.dto.patches.UsuarioColeccionVisibleDTO;
import com.svalero.fancollector.exception.domain.UsuarioColeccionNoEncontradoException;
import com.svalero.fancollector.exception.security.AccesoDenegadoException;
import com.svalero.fancollector.exception.validation.RelacionYaExisteException;
import com.svalero.fancollector.repository.ColeccionRepository;
import com.svalero.fancollector.repository.UsuarioColeccionRepository;
import com.svalero.fancollector.repository.UsuarioItemRepository;
import com.svalero.fancollector.repository.UsuarioRepository;
import com.svalero.fancollector.security.auth.CurrentUserResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioColeccionServiceTest {
    private static final String EMAIL = "nerea@test.com";

    @InjectMocks
    private UsuarioColeccionServiceImpl usuarioColeccionService;

    @Mock
    private UsuarioColeccionRepository usuarioColeccionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ColeccionRepository coleccionRepository;

    @Mock
    private UsuarioItemRepository usuarioItemRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private CurrentUserResolver currentUserResolver;

    @Test
    public void crear_datosValidos_devuelveRelacionCreada() {
        boolean esAdmin = false;
        boolean esMods = false;

        UsuarioColeccionInDTO dto = new UsuarioColeccionInDTO();
        dto.setIdUsuario(1L);
        dto.setIdColeccion(1L);
        dto.setEsFavorita(false);

        Usuario usuarioActual = new Usuario();
        usuarioActual.setId(1L);
        usuarioActual.setEmail(EMAIL);

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Coleccion coleccion = new Coleccion();
        coleccion.setId(1L);
        coleccion.setEsPublica(true);
        coleccion.setUsableComoPlantilla(true);

        UsuarioColeccion ucMapeado = new UsuarioColeccion();
        ucMapeado.setEsFavorita(false);

        UsuarioColeccion ucGuardado = new UsuarioColeccion();
        ucGuardado.setId(1L);
        ucGuardado.setUsuario(usuario);
        ucGuardado.setColeccion(coleccion);

        UsuarioColeccionOutDTO outDTO = new UsuarioColeccionOutDTO();
        outDTO.setId(1L);
        outDTO.setIdUsuario(1L);
        outDTO.setIdColeccion(1L);

        when(currentUserResolver.usuarioActual(EMAIL)).thenReturn(usuarioActual);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(coleccionRepository.findById(1L)).thenReturn(Optional.of(coleccion));
        when(usuarioColeccionRepository.existsByUsuario_IdAndColeccion_Id(1L, 1L)).thenReturn(false);
        when(modelMapper.map(dto, UsuarioColeccion.class)).thenReturn(ucMapeado);
        when(usuarioColeccionRepository.save(any(UsuarioColeccion.class))).thenReturn(ucGuardado);
        when(modelMapper.map(ucGuardado, UsuarioColeccionOutDTO.class)).thenReturn(outDTO);

        UsuarioColeccionOutDTO resultado = usuarioColeccionService.crear(dto, EMAIL, esAdmin, esMods);

        assertEquals(1L, resultado.getId());
        assertEquals(1L, resultado.getIdUsuario());
        assertEquals(1L, resultado.getIdColeccion());
        verify(usuarioColeccionRepository, times(1)).save(any(UsuarioColeccion.class));
    }

    @Test
    public void crear_relacionDuplicada_lanzaRelacionYaExisteException() {
        boolean esAdmin = false;
        boolean esMods = false;

        UsuarioColeccionInDTO dto = new UsuarioColeccionInDTO();
        dto.setIdUsuario(1L);
        dto.setIdColeccion(1L);

        Usuario usuarioActual = new Usuario();
        usuarioActual.setId(1L);

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Coleccion coleccion = new Coleccion();
        coleccion.setId(1L);
        coleccion.setEsPublica(true);
        coleccion.setUsableComoPlantilla(true);

        when(currentUserResolver.usuarioActual(EMAIL)).thenReturn(usuarioActual);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(coleccionRepository.findById(1L)).thenReturn(Optional.of(coleccion));
        when(usuarioColeccionRepository.existsByUsuario_IdAndColeccion_Id(1L, 1L)).thenReturn(true);

        assertThrows(RelacionYaExisteException.class, () ->
                usuarioColeccionService.crear(dto, EMAIL, esAdmin, esMods)
        );

        verify(usuarioColeccionRepository, never()).save(any(UsuarioColeccion.class));
    }

    @Test
    public void crear_coleccionNoPublicaNiPlantilla_lanzaAccesoDenegadoException() {
        boolean esAdmin = false;
        boolean esMods = false;

        UsuarioColeccionInDTO dto = new UsuarioColeccionInDTO();
        dto.setIdUsuario(1L);
        dto.setIdColeccion(1L);

        Usuario usuarioActual = new Usuario();
        usuarioActual.setId(2L);

        when(currentUserResolver.usuarioActual(EMAIL)).thenReturn(usuarioActual);

        assertThrows(AccesoDenegadoException.class, () ->
                usuarioColeccionService.crear(dto, EMAIL, esAdmin, esMods)
        );

        verify(usuarioColeccionRepository, never()).save(any(UsuarioColeccion.class));
    }

    // crear v2
    @Test
    public void crearV2_datosValidos_creaRelacionYCopiaSusItems() {
        Usuario usuarioActual = new Usuario();
        usuarioActual.setId(1L);
        usuarioActual.setEmail(EMAIL);

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Item item1 = new Item();
        item1.setId(1L);

        Item item2 = new Item();
        item2.setId(2L);

        Coleccion coleccion = new Coleccion();
        coleccion.setId(1L);
        coleccion.setEsPublica(true);
        coleccion.setUsableComoPlantilla(true);
        coleccion.setItems(List.of(item1, item2));

        UsuarioColeccionInDTO dto = new UsuarioColeccionInDTO();
        dto.setIdUsuario(1L);
        dto.setIdColeccion(1L);

        UsuarioColeccion ucMapeado = new UsuarioColeccion();
        UsuarioColeccion ucGuardado = new UsuarioColeccion();
        ucGuardado.setId(1L);
        ucGuardado.setUsuario(usuario);
        ucGuardado.setColeccion(coleccion);

        UsuarioColeccionOutDTO outDTO = new UsuarioColeccionOutDTO();
        outDTO.setId(1L);

        when(currentUserResolver.usuarioActual(EMAIL)).thenReturn(usuarioActual);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(coleccionRepository.findById(1L)).thenReturn(Optional.of(coleccion));
        when(usuarioColeccionRepository.existsByUsuario_IdAndColeccion_Id(1L, 1L)).thenReturn(false);
        when(modelMapper.map(dto, UsuarioColeccion.class)).thenReturn(ucMapeado);
        when(usuarioColeccionRepository.save(any(UsuarioColeccion.class))).thenReturn(ucGuardado);
        when(modelMapper.map(ucGuardado, UsuarioColeccionOutDTO.class)).thenReturn(outDTO);
        when(usuarioItemRepository.existsByUsuarioIdAndColeccionIdAndItemId(anyLong(), anyLong(), anyLong())).thenReturn(false);

        UsuarioColeccionOutDTO resultado = usuarioColeccionService.crearV2(dto, EMAIL, false, false);

        assertEquals(1L, resultado.getId());
        verify(usuarioColeccionRepository, times(1)).save(any(UsuarioColeccion.class));
        verify(usuarioItemRepository, times(2)).save(any(UsuarioItem.class));
    }

    @Test
    public void crearV2_relacionDuplicada_lanzaRelacionYaExisteException() {
        Usuario usuarioActual = new Usuario();
        usuarioActual.setId(1L);

        Coleccion coleccion = new Coleccion();
        coleccion.setId(1L);
        coleccion.setEsPublica(true);
        coleccion.setUsableComoPlantilla(true);

        UsuarioColeccionInDTO dto = new UsuarioColeccionInDTO();
        dto.setIdUsuario(1L);
        dto.setIdColeccion(1L);

        when(currentUserResolver.usuarioActual(EMAIL)).thenReturn(usuarioActual);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(new Usuario()));
        when(coleccionRepository.findById(1L)).thenReturn(Optional.of(coleccion));
        when(usuarioColeccionRepository.existsByUsuario_IdAndColeccion_Id(1L, 1L)).thenReturn(true);

        assertThrows(RelacionYaExisteException.class, () ->
                usuarioColeccionService.crearV2(dto, EMAIL, false, false)
        );

        verify(usuarioColeccionRepository, never()).save(any(UsuarioColeccion.class));
        verify(usuarioItemRepository, never()).save(any(UsuarioItem.class));
    }

    @Test
    public void buscarPorId_existente_devuelveRelacion() {
        boolean esAdmin = true;
        boolean esMods = false;

        UsuarioColeccion uc = new UsuarioColeccion();
        uc.setId(1L);

        UsuarioColeccionOutDTO outDTO = new UsuarioColeccionOutDTO();
        outDTO.setId(1L);

        when(usuarioColeccionRepository.findById(1L)).thenReturn(Optional.of(uc));
        when(modelMapper.map(uc, UsuarioColeccionOutDTO.class)).thenReturn(outDTO);

        UsuarioColeccionOutDTO resultado = usuarioColeccionService.buscarPorId(1L, EMAIL, esAdmin, esMods);

        assertEquals(1L, resultado.getId());
        verify(usuarioColeccionRepository, times(1)).findById(1L);
    }

    @Test
    public void buscarPorId_noExiste_lanzaUsuarioColeccionNoEncontradoException() {
        boolean esAdmin = true;
        boolean esMods = false;

        when(usuarioColeccionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UsuarioColeccionNoEncontradoException.class, () ->
            usuarioColeccionService.buscarPorId(999L, EMAIL, esAdmin, esMods)
        );

        verify(usuarioColeccionRepository, times(1)).findById(999L);
    }

    @Test
    public void listar_sinFiltros_devuelveTodas() {
        boolean esAdmin = false;
        boolean esMods = false;

        Usuario usuarioActual = new Usuario();
        usuarioActual.setId(1L);

        UsuarioColeccion uc1 = new UsuarioColeccion();
        uc1.setId(1L);
        uc1.setUsuario(usuarioActual);

        UsuarioColeccion uc2 = new UsuarioColeccion();
        uc2.setId(2L);
        uc2.setUsuario(usuarioActual);

        List<UsuarioColeccion> lista = List.of(uc1, uc2);

        UsuarioColeccionOutDTO dto1 = new UsuarioColeccionOutDTO();
        dto1.setId(1L);

        UsuarioColeccionOutDTO dto2 = new UsuarioColeccionOutDTO();
        dto2.setId(2L);

        when(currentUserResolver.usuarioActual(EMAIL)).thenReturn(usuarioActual);
        when(usuarioColeccionRepository.findAll()).thenReturn(lista);
        when(modelMapper.map(uc1, UsuarioColeccionOutDTO.class)).thenReturn(dto1);
        when(modelMapper.map(uc2, UsuarioColeccionOutDTO.class)).thenReturn(dto2);

        List<UsuarioColeccionOutDTO> resultado = usuarioColeccionService.listar(null, null, null, null, EMAIL, esAdmin, esMods);

        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals(2L, resultado.get(1).getId());
        verify(usuarioColeccionRepository, times(1)).findAll();
    }

    @Test
    public void actualizar_datosValidos_devuelveRelacionActualizada() {
        boolean esAdmin = true;
        boolean esMods = false;

        Usuario usuarioActual = new Usuario();
        usuarioActual.setId(999L);
        when(currentUserResolver.usuarioActual(EMAIL)).thenReturn(usuarioActual);

        UsuarioColeccionPutDTO dto = new UsuarioColeccionPutDTO();
        dto.setEsFavorita(true);

        UsuarioColeccion ucExistente = new UsuarioColeccion();
        ucExistente.setId(1L);
        ucExistente.setEsFavorita(false);

        UsuarioColeccion ucActualizado = new UsuarioColeccion();
        ucActualizado.setId(1L);
        ucActualizado.setEsFavorita(true);

        UsuarioColeccionOutDTO outDTO = new UsuarioColeccionOutDTO();
        outDTO.setId(1L);
        outDTO.setEsFavorita(true);

        when(usuarioColeccionRepository.findById(1L)).thenReturn(Optional.of(ucExistente));
        when(usuarioColeccionRepository.save(any(UsuarioColeccion.class))).thenReturn(ucActualizado);
        when(modelMapper.map(ucActualizado, UsuarioColeccionOutDTO.class)).thenReturn(outDTO);

        UsuarioColeccionOutDTO resultado = usuarioColeccionService.actualizar(1L, dto, EMAIL, esAdmin, esMods);

        assertTrue(resultado.getEsFavorita());
        verify(usuarioColeccionRepository, times(1)).findById(1L);
        verify(usuarioColeccionRepository, times(1)).save(any(UsuarioColeccion.class));
    }

    @Test
    public void actualizar_noExiste_lanzaUsuarioColeccionNoEncontradoException() {
        boolean esAdmin = true;
        boolean esMods = false;

        UsuarioColeccionPutDTO dto = new UsuarioColeccionPutDTO();
        dto.setEsFavorita(true);

        when(usuarioColeccionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UsuarioColeccionNoEncontradoException.class, () ->
                usuarioColeccionService.actualizar(999L, dto, EMAIL, esAdmin, esMods)
        );

        verify(usuarioColeccionRepository, times(1)).findById(999L);
        verify(usuarioColeccionRepository, never()).save(any(UsuarioColeccion.class));
    }

    @Test
    public void actualizarFavorita_relacionExistente_actualizaFavorita() {
        boolean esAdmin = true;
        boolean esMods = false;

        Usuario usuarioActual = new Usuario();
        usuarioActual.setId(1L);

        when(currentUserResolver.usuarioActual(EMAIL)).thenReturn(usuarioActual);

        UsuarioColeccionFavoritaDTO dto = new UsuarioColeccionFavoritaDTO();
        dto.setEsFavorita(true);

        UsuarioColeccion uc = new UsuarioColeccion();
        uc.setId(1L);
        uc.setEsFavorita(false);

        uc.setUsuario(usuarioActual);

        UsuarioColeccionOutDTO outDTO = new UsuarioColeccionOutDTO();
        outDTO.setId(1L);
        outDTO.setEsFavorita(true);

        when(usuarioColeccionRepository.findById(1L)).thenReturn(Optional.of(uc));
        when(usuarioColeccionRepository.save(any(UsuarioColeccion.class))).thenReturn(uc);
        when(modelMapper.map(uc, UsuarioColeccionOutDTO.class)).thenReturn(outDTO);

        UsuarioColeccionOutDTO resultado = usuarioColeccionService.actualizarFavorita(1L, dto, EMAIL, esAdmin, esMods);

        assertTrue(resultado.getEsFavorita());
        verify(usuarioColeccionRepository, times(1)).findById(1L);
        verify(usuarioColeccionRepository, times(1)).save(any(UsuarioColeccion.class));
        verify(currentUserResolver, times(1)).usuarioActual(EMAIL);
    }

    @Test
    public void actualizarFavorita_noExiste_lanzaUsuarioColeccionNoEncontradoException() {
        boolean esAdmin = true;
        boolean esMods = false;

        UsuarioColeccionFavoritaDTO dto = new UsuarioColeccionFavoritaDTO();
        dto.setEsFavorita(true);

        when(usuarioColeccionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UsuarioColeccionNoEncontradoException.class, () ->
                usuarioColeccionService.actualizarFavorita(999L, dto, EMAIL, esAdmin, esMods)
        );

        verify(usuarioColeccionRepository, times(1)).findById(999L);
        verify(usuarioColeccionRepository, never()).save(any(UsuarioColeccion.class));
    }

    @Test
    public void actualizarVisible_relacionExistente_actualizaVisibilidad() {
        boolean esAdmin = true;
        boolean esMods = false;

        UsuarioColeccionVisibleDTO dto = new UsuarioColeccionVisibleDTO();
        dto.setEsVisible(false);

        UsuarioColeccion uc = new UsuarioColeccion();
        uc.setId(1L);
        uc.setEsVisible(true);

        UsuarioColeccionOutDTO outDTO = new UsuarioColeccionOutDTO();
        outDTO.setId(1L);
        outDTO.setEsVisible(false);

        when(usuarioColeccionRepository.findById(1L)).thenReturn(Optional.of(uc));
        when(usuarioColeccionRepository.save(any(UsuarioColeccion.class))).thenReturn(uc);
        when(modelMapper.map(uc, UsuarioColeccionOutDTO.class)).thenReturn(outDTO);

        UsuarioColeccionOutDTO resultado = usuarioColeccionService.actualizarVisible(1L, dto, EMAIL, esAdmin, esMods);

        assertFalse(resultado.getEsVisible());
        verify(usuarioColeccionRepository, times(1)).findById(1L);
        verify(usuarioColeccionRepository, times(1)).save(any(UsuarioColeccion.class));
    }

    @Test
    public void actualizarVisible_noExiste_lanzaUsuarioColeccionNoEncontradoException() {
        boolean esAdmin = true;
        boolean esMods = false;

        UsuarioColeccionVisibleDTO dto = new UsuarioColeccionVisibleDTO();
        dto.setEsVisible(false);

        when(usuarioColeccionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UsuarioColeccionNoEncontradoException.class, () ->
                        usuarioColeccionService.actualizarVisible(999L, dto, EMAIL, esAdmin, esMods)
        );

        verify(usuarioColeccionRepository, times(1)).findById(999L);
        verify(usuarioColeccionRepository, never()).save(any(UsuarioColeccion.class));
    }

    // V1 - ELIMINAR
    @Test
    public void eliminar_existente_soloEliminaRelacion() {
        when(usuarioColeccionRepository.findById(1L)).thenReturn(Optional.of(new UsuarioColeccion()));

        usuarioColeccionService.eliminar(1L);

        verify(usuarioColeccionRepository, times(1)).findById(1L);
        verify(usuarioItemRepository, never()).deleteByUsuario_IdAndColeccion_Id(any(), any());
        verify(usuarioColeccionRepository, times(1)).delete(any(UsuarioColeccion.class));
    }

    @Test
    public void eliminar_noExiste_lanzaUsuarioColeccionNoEncontradoException() {
        when(usuarioColeccionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UsuarioColeccionNoEncontradoException.class, () ->
                usuarioColeccionService.eliminar(999L)
        );

        verify(usuarioColeccionRepository, times(1)).findById(999L);
        verify(usuarioColeccionRepository, never()).delete(any(UsuarioColeccion.class));
    }

    // V2 - ELIMINAR
    @Test
    public void eliminarV2_existente_eliminaRelacionYSusItems() {
        boolean esAdmin = true;
        boolean esMods = false;

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail(EMAIL);

        Coleccion coleccion = new Coleccion();
        coleccion.setId(1L);

        UsuarioColeccion uc = new UsuarioColeccion();
        uc.setId(1L);
        uc.setUsuario(usuario);
        uc.setColeccion(coleccion);

        when(usuarioColeccionRepository.findById(1L)).thenReturn(Optional.of(uc));
        when(currentUserResolver.usuarioActual(EMAIL)).thenReturn(usuario);

        usuarioColeccionService.eliminarV2(1L, EMAIL, esAdmin, esMods);

        verify(usuarioColeccionRepository, times(1)).findById(1L);
        verify(usuarioItemRepository, times(1))
                .deleteByUsuario_IdAndColeccion_Id(1L, 1L);
        verify(usuarioColeccionRepository, times(1)).delete(uc);
    }

    @Test
    public void eliminarV2_noExiste_lanzaUsuarioColeccionNoEncontradoException() {
        boolean esAdmin = true;
        boolean esMods = false;

        when(usuarioColeccionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UsuarioColeccionNoEncontradoException.class, () ->
                usuarioColeccionService.eliminarV2(999L, EMAIL, esAdmin, esMods)
        );

        verify(usuarioColeccionRepository, times(1)).findById(999L);
        verify(usuarioColeccionRepository, never()).delete(any(UsuarioColeccion.class));
    }
}