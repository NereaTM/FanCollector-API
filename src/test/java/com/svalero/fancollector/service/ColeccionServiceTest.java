package com.svalero.fancollector.service;

import com.svalero.fancollector.domain.Coleccion;
import com.svalero.fancollector.domain.Usuario;
import com.svalero.fancollector.domain.UsuarioColeccion;
import com.svalero.fancollector.dto.ColeccionInDTO;
import com.svalero.fancollector.dto.ColeccionOutDTO;
import com.svalero.fancollector.dto.ColeccionPutDTO;
import com.svalero.fancollector.exception.domain.ColeccionNoEncontradaException;
import com.svalero.fancollector.exception.domain.UsuarioNoEncontradoException;
import com.svalero.fancollector.repository.ColeccionRepository;
import com.svalero.fancollector.repository.UsuarioColeccionRepository;
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
public class ColeccionServiceTest {
    private static final String EMAIL = "nerea@test.com";

    @InjectMocks
    private ColeccionServiceImpl coleccionService;

    @Mock
    private ColeccionRepository coleccionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioColeccionRepository usuarioColeccionRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private CurrentUserResolver currentUserResolver;

    @Test
    public void crearColeccion_datosValidos_devuelveColeccionCreada() {
        ColeccionInDTO coleccionInDTO = new ColeccionInDTO();
        coleccionInDTO.setIdCreador(1L);
        coleccionInDTO.setNombre("Figuras Anime");
        coleccionInDTO.setCategoria("Anime");

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail(EMAIL);
        usuario.setNombre("Nerea");

        when(currentUserResolver.usuarioActual(EMAIL)).thenReturn(usuario);

        Coleccion coleccionMapeada = new Coleccion();
        coleccionMapeada.setNombre("Figuras Anime");

        Coleccion coleccionGuardada = new Coleccion();
        coleccionGuardada.setId(1L);
        coleccionGuardada.setNombre("Figuras Anime");
        coleccionGuardada.setCreador(usuario);

        ColeccionOutDTO coleccionOutDTO = new ColeccionOutDTO();
        coleccionOutDTO.setId(1L);
        coleccionOutDTO.setNombre("Figuras Anime");

        when(modelMapper.map(coleccionInDTO, Coleccion.class)).thenReturn(coleccionMapeada);
        when(coleccionRepository.save(any(Coleccion.class))).thenReturn(coleccionGuardada);
        when(modelMapper.map(coleccionGuardada, ColeccionOutDTO.class)).thenReturn(coleccionOutDTO);

        ColeccionOutDTO resultado = coleccionService.crearColeccion(coleccionInDTO, EMAIL);

        assertEquals(1L, resultado.getId());
        assertEquals("Figuras Anime", resultado.getNombre());
        verify(currentUserResolver, times(1)).usuarioActual(EMAIL);
        verify(coleccionRepository, times(1)).save(any(Coleccion.class));
        verify(usuarioColeccionRepository, times(1)).save(any(UsuarioColeccion.class));
    }

    @Test
    public void crearColeccion_usuarioNoExiste_lanzaUsuarioNoEncontradoException() {
        ColeccionInDTO coleccionInDTO = new ColeccionInDTO();

        when(currentUserResolver.usuarioActual(EMAIL)).thenThrow(new UsuarioNoEncontradoException(999L));

        assertThrows(UsuarioNoEncontradoException.class, () -> {
            coleccionService.crearColeccion(coleccionInDTO, EMAIL);
        });

        verify(currentUserResolver, times(1)).usuarioActual(EMAIL);
        verify(coleccionRepository, never()).save(any(Coleccion.class));
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    public void buscarColeccionPorId_existente_devuelveColeccion() {
        boolean esAdmin = true;
        boolean esMods = false;

        Coleccion coleccion = new Coleccion();
        coleccion.setId(1L);
        coleccion.setNombre("Figuras Anime");

        ColeccionOutDTO coleccionOutDTO = new ColeccionOutDTO();
        coleccionOutDTO.setId(1L);
        coleccionOutDTO.setNombre("Figuras Anime");

        when(coleccionRepository.findById(1L)).thenReturn(Optional.of(coleccion));
        when(modelMapper.map(coleccion, ColeccionOutDTO.class)).thenReturn(coleccionOutDTO);

        ColeccionOutDTO resultado = coleccionService.buscarColeccionPorId(1L, EMAIL, esAdmin, esMods);

        assertEquals(1L, resultado.getId());
        assertEquals("Figuras Anime", resultado.getNombre());
        verify(coleccionRepository, times(1)).findById(1L);
    }

    @Test
    public void buscarColeccionPorId_noExiste_lanzaColeccionNoEncontradaException() {
        boolean esAdmin = true;
        boolean esMods = false;

        when(coleccionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ColeccionNoEncontradaException.class, () -> {
            coleccionService.buscarColeccionPorId(999L, EMAIL, esAdmin, esMods);
        });

        verify(coleccionRepository, times(1)).findById(999L);
    }

    @Test
    public void listarColecciones_sinFiltros_devuelveTodas() {
        boolean esAdmin = true;
        boolean esMods = false;

        Coleccion coleccion1 = new Coleccion();
        coleccion1.setId(1L);
        coleccion1.setNombre("Figuras Anime");

        Coleccion coleccion2 = new Coleccion();
        coleccion2.setId(2L);
        coleccion2.setNombre("Trading Cards");

        List<Coleccion> colecciones = List.of(coleccion1, coleccion2);

        ColeccionOutDTO dto1 = new ColeccionOutDTO();
        dto1.setId(1L);
        dto1.setNombre("Figuras Anime");

        ColeccionOutDTO dto2 = new ColeccionOutDTO();
        dto2.setId(2L);
        dto2.setNombre("Trading Cards");

        when(coleccionRepository.findAll()).thenReturn(colecciones);
        when(modelMapper.map(coleccion1, ColeccionOutDTO.class)).thenReturn(dto1);
        when(modelMapper.map(coleccion2, ColeccionOutDTO.class)).thenReturn(dto2);

        List<ColeccionOutDTO> resultado = coleccionService.listarColecciones(null, null, null, null, EMAIL, esAdmin, esMods, null);

        assertEquals(2, resultado.size());
        assertEquals("Figuras Anime", resultado.get(0).getNombre());
        assertEquals("Trading Cards", resultado.get(1).getNombre());
        verify(coleccionRepository, times(1)).findAll();
    }

    @Test
    public void actualizarColeccion_datosValidos_devuelveColeccionActualizada() {
        boolean esAdmin = true;
        boolean esMods = false;

        ColeccionPutDTO coleccionPutDTO = new ColeccionPutDTO();
        coleccionPutDTO.setNombre("Figuras Anime Actualizado");
        coleccionPutDTO.setCategoria("Anime");
        coleccionPutDTO.setEsPublica(false);
        coleccionPutDTO.setUsableComoPlantilla(false);

        Coleccion coleccionExistente = new Coleccion();
        coleccionExistente.setId(1L);
        coleccionExistente.setNombre("Figuras Anime");

        Coleccion coleccionActualizada = new Coleccion();
        coleccionActualizada.setId(1L);
        coleccionActualizada.setNombre("Figuras Anime Actualizado");

        ColeccionOutDTO coleccionOutDTO = new ColeccionOutDTO();
        coleccionOutDTO.setId(1L);
        coleccionOutDTO.setNombre("Figuras Anime Actualizado");

        when(coleccionRepository.findById(1L)).thenReturn(Optional.of(coleccionExistente));
        when(coleccionRepository.save(any(Coleccion.class))).thenReturn(coleccionActualizada);
        when(modelMapper.map(coleccionActualizada, ColeccionOutDTO.class)).thenReturn(coleccionOutDTO);

        ColeccionOutDTO resultado = coleccionService.actualizarColeccion(1L, coleccionPutDTO, EMAIL, esAdmin, esMods);

        assertEquals("Figuras Anime Actualizado", resultado.getNombre());
        verify(coleccionRepository, times(1)).findById(1L);
        verify(coleccionRepository, times(1)).save(any(Coleccion.class));
    }

    @Test
    public void actualizarColeccion_noExiste_lanzaColeccionNoEncontradaException() {
        boolean esAdmin = true;
        boolean esMods = false;

        ColeccionPutDTO coleccionPutDTO = new ColeccionPutDTO();
        coleccionPutDTO.setNombre("Figuras Anime");
        coleccionPutDTO.setCategoria("Anime");

        when(coleccionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ColeccionNoEncontradaException.class, () ->
                coleccionService.actualizarColeccion(999L, coleccionPutDTO, EMAIL, esAdmin, esMods)
        );

        verify(coleccionRepository, times(1)).findById(999L);
        verify(coleccionRepository, never()).save(any(Coleccion.class));
    }

    @Test
    public void actualizarEsPublica_coleccionExistente_actualizaVisibilidad() {
        boolean esAdmin = true;
        boolean esMods = false;

        Coleccion coleccion = new Coleccion();
        coleccion.setId(1L);
        coleccion.setNombre("Figuras Anime");
        coleccion.setEsPublica(false);

        ColeccionOutDTO coleccionOutDTO = new ColeccionOutDTO();
        coleccionOutDTO.setId(1L);
        coleccionOutDTO.setEsPublica(true);

        when(coleccionRepository.findById(1L)).thenReturn(Optional.of(coleccion));
        when(coleccionRepository.save(any(Coleccion.class))).thenReturn(coleccion);
        when(modelMapper.map(coleccion, ColeccionOutDTO.class)).thenReturn(coleccionOutDTO);

        ColeccionOutDTO resultado = coleccionService.actualizarEsPublica(1L, true, EMAIL, esAdmin, esMods);

        assertTrue(resultado.isEsPublica());
        verify(coleccionRepository, times(1)).findById(1L);
        verify(coleccionRepository, times(1)).save(any(Coleccion.class));
    }

    @Test
    public void actualizarEsPublica_noExiste_lanzaColeccionNoEncontradaException() {
        boolean esAdmin = true;
        boolean esMods = false;

        when(coleccionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ColeccionNoEncontradaException.class, () ->
                coleccionService.actualizarEsPublica(999L, true, EMAIL, esAdmin, esMods)
        );

        verify(coleccionRepository, times(1)).findById(999L);
        verify(coleccionRepository, never()).save(any(Coleccion.class));
    }

    @Test
    public void actualizarUsableComoPlantilla_coleccionExistente_actualizaPlantilla() {
        boolean esAdmin = true;

        Coleccion coleccion = new Coleccion();
        coleccion.setId(1L);
        coleccion.setUsableComoPlantilla(false);

        ColeccionOutDTO coleccionOutDTO = new ColeccionOutDTO();
        coleccionOutDTO.setId(1L);
        coleccionOutDTO.setUsableComoPlantilla(true);

        when(coleccionRepository.findById(1L)).thenReturn(Optional.of(coleccion));
        when(coleccionRepository.save(any(Coleccion.class))).thenReturn(coleccion);
        when(modelMapper.map(coleccion, ColeccionOutDTO.class)).thenReturn(coleccionOutDTO);

        ColeccionOutDTO resultado = coleccionService.actualizarUsableComoPlantilla(1L, true, EMAIL, esAdmin);

        assertTrue(resultado.isUsableComoPlantilla());
        verify(coleccionRepository, times(1)).findById(1L);
        verify(coleccionRepository, times(1)).save(any(Coleccion.class));
    }

    @Test
    public void actualizarUsableComoPlantilla_noExiste_lanzaColeccionNoEncontradaException() {
        boolean esAdmin = true;

        when(coleccionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ColeccionNoEncontradaException.class, () ->
                coleccionService.actualizarUsableComoPlantilla(999L, true, EMAIL, esAdmin)
        );

        verify(coleccionRepository, times(1)).findById(999L);
        verify(coleccionRepository, never()).save(any(Coleccion.class));
    }

    @Test
    public void eliminarColeccion_existente_eliminaCorrectamente() {
        boolean esAdmin = true;
        boolean esMods = false;

        Coleccion coleccion = new Coleccion();
        coleccion.setId(1L);

        when(coleccionRepository.findById(1L)).thenReturn(Optional.of(coleccion));

        coleccionService.eliminarColeccion(1L, EMAIL, esAdmin, esMods);

        verify(coleccionRepository, times(1)).findById(1L);
        verify(coleccionRepository, times(1)).delete(coleccion);
    }

    @Test
    public void eliminarColeccion_noExiste_lanzaColeccionNoEncontradaException() {
        boolean esAdmin = true;
        boolean esMods = false;

        when(coleccionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ColeccionNoEncontradaException.class, () -> {
            coleccionService.eliminarColeccion(999L, EMAIL, esAdmin, esMods);});

        verify(coleccionRepository, times(1)).findById(999L);
        verify(coleccionRepository, never()).delete(any(Coleccion.class));
    }
}