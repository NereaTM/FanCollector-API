package com.svalero.fancollector.service;

import com.svalero.fancollector.domain.*;
import com.svalero.fancollector.domain.enums.EstadoItem;
import com.svalero.fancollector.dto.UsuarioColeccionInDTO;
import com.svalero.fancollector.dto.UsuarioColeccionOutDTO;
import com.svalero.fancollector.dto.UsuarioColeccionPutDTO;
import com.svalero.fancollector.dto.patches.UsuarioColeccionFavoritaDTO;
import com.svalero.fancollector.dto.patches.UsuarioColeccionVisibleDTO;
import com.svalero.fancollector.exception.domain.ColeccionNoEncontradaException;
import com.svalero.fancollector.exception.domain.UsuarioColeccionNoEncontradoException;
import com.svalero.fancollector.exception.domain.UsuarioNoEncontradoException;
import com.svalero.fancollector.exception.security.AccesoDenegadoException;
import com.svalero.fancollector.exception.validation.RelacionYaExisteException;
import com.svalero.fancollector.repository.ColeccionRepository;
import com.svalero.fancollector.repository.UsuarioColeccionRepository;
import com.svalero.fancollector.repository.UsuarioItemRepository;
import com.svalero.fancollector.repository.UsuarioRepository;
import com.svalero.fancollector.security.auth.CurrentUserResolver;
import com.svalero.fancollector.security.auth.Permisos;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UsuarioColeccionServiceImpl implements UsuarioColeccionService {

    @Autowired
    private UsuarioColeccionRepository usuarioColeccionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ColeccionRepository coleccionRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CurrentUserResolver currentUserResolver;

    @Autowired
    private UsuarioItemRepository usuarioItemRepository;

    @Override
    public UsuarioColeccionOutDTO crear(UsuarioColeccionInDTO dto, String emailUsuario, boolean esAdmin, boolean esMods) {

        Usuario usuarioActual = currentUserResolver.usuarioActual(emailUsuario);
        if (!esAdmin && !dto.getIdUsuario().equals(usuarioActual.getId()))
            throw new AccesoDenegadoException();

        Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new UsuarioNoEncontradoException(dto.getIdUsuario()));

        Coleccion coleccion = coleccionRepository.findById(dto.getIdColeccion())
                .orElseThrow(() -> new ColeccionNoEncontradaException(dto.getIdColeccion()));

        if (!coleccion.isEsPublica() || !coleccion.isUsableComoPlantilla()) {
            throw new AccesoDenegadoException();
        }

        if (usuarioColeccionRepository.existsByUsuario_IdAndColeccion_Id(
                dto.getIdUsuario(), dto.getIdColeccion())) {
            throw new RelacionYaExisteException();
        }

        UsuarioColeccion uc = modelMapper.map(dto, UsuarioColeccion.class);
        uc.setUsuario(usuario);
        uc.setColeccion(coleccion);

        return modelMapper.map(usuarioColeccionRepository.save(uc), UsuarioColeccionOutDTO.class);
    }

    @Transactional
    @Override
    public UsuarioColeccionOutDTO crearV2(UsuarioColeccionInDTO dto, String emailUsuario, boolean esAdmin, boolean esMods) {

        Usuario usuarioActual = currentUserResolver.usuarioActual(emailUsuario);
        if (!esAdmin && !dto.getIdUsuario().equals(usuarioActual.getId()))
            throw new AccesoDenegadoException();

        Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new UsuarioNoEncontradoException(dto.getIdUsuario()));

        Coleccion coleccion = coleccionRepository.findById(dto.getIdColeccion())
                .orElseThrow(() -> new ColeccionNoEncontradaException(dto.getIdColeccion()));

        if (!coleccion.isEsPublica() || !coleccion.isUsableComoPlantilla()) {
            throw new AccesoDenegadoException();
        }

        if (usuarioColeccionRepository.existsByUsuario_IdAndColeccion_Id(
                dto.getIdUsuario(), dto.getIdColeccion())) {
            throw new RelacionYaExisteException();
        }

        UsuarioColeccion uc = modelMapper.map(dto, UsuarioColeccion.class);
        uc.setUsuario(usuario);
        uc.setColeccion(coleccion);

        UsuarioColeccion guardada = usuarioColeccionRepository.save(uc);

        // si un usuario se une a na plantilla pasan los items a BUSCO y 0
        for (Item item : coleccion.getItems()) {
            boolean existe = usuarioItemRepository.existsByUsuarioIdAndColeccionIdAndItemId(
                    usuario.getId(), coleccion.getId(), item.getId());
            if (existe) continue;

            UsuarioItem ui = new UsuarioItem();
            ui.setUsuario(usuario);
            ui.setColeccion(coleccion);
            ui.setItem(item);
            ui.setEstado(EstadoItem.BUSCO);
            ui.setCantidad(0);
            ui.setEsVisible(true);
            ui.setFechaRegistro(LocalDateTime.now());

            usuarioItemRepository.save(ui);
        }

        return modelMapper.map(guardada, UsuarioColeccionOutDTO.class);
    }

    @Override
    public UsuarioColeccionOutDTO buscarPorId(Long id, String emailUsuario, boolean esAdmin, boolean esMods) {
        UsuarioColeccion uc = usuarioColeccionRepository.findById(id)
                .orElseThrow(() -> new UsuarioColeccionNoEncontradoException(id));

        Usuario usuarioActual = currentUserResolver.usuarioActual(emailUsuario);
        Permisos.checkPuedeVerUsuarioColeccion(uc, usuarioActual, esAdmin, esMods);

        return modelMapper.map(uc, UsuarioColeccionOutDTO.class);
    }

    @Override
    public List<UsuarioColeccionOutDTO> listar(Long idUsuario, Long idColeccion, Boolean soloFavoritas, Boolean esVisible, String emailUsuario, boolean esAdmin, boolean esMods) {

        boolean noHayFiltros = true;

        if (idUsuario != null) noHayFiltros = false;
        if (idColeccion != null) noHayFiltros = false;
        if (soloFavoritas != null) noHayFiltros = false;
        if (esVisible != null) noHayFiltros = false;

        List<UsuarioColeccion> relaciones;

        if (noHayFiltros) {
            relaciones = usuarioColeccionRepository.findAll();
        } else {
            relaciones = usuarioColeccionRepository.buscarPorFiltros(idUsuario, idColeccion, soloFavoritas, esVisible);
        }

        Usuario usuarioActual = null;
        if (emailUsuario != null && !emailUsuario.isBlank()) {
            usuarioActual = currentUserResolver.usuarioActual(emailUsuario);
        }

        List<UsuarioColeccionOutDTO> resultado = new ArrayList<>();
        for (UsuarioColeccion uc : relaciones) {
            if (Permisos.puedeVerUsuarioColeccion(uc, usuarioActual, esAdmin, esMods)) {
                resultado.add(modelMapper.map(uc, UsuarioColeccionOutDTO.class));
            }
        }
        return resultado;
    }

    @Override
    public UsuarioColeccionOutDTO actualizar(Long id, UsuarioColeccionPutDTO dto, String emailUsuario, boolean esAdmin, boolean esMods) {

        UsuarioColeccion existente = usuarioColeccionRepository.findById(id)
                .orElseThrow(() -> new UsuarioColeccionNoEncontradoException(id));

        Usuario usuarioActual = currentUserResolver.usuarioActual(emailUsuario);
        Permisos.checkPuedeEditarOBorrarUsuarioColeccion(existente, usuarioActual, esAdmin, esMods);

        if (dto.getEsFavorita() != null) {
            existente.setEsFavorita(dto.getEsFavorita());}

        if (dto.getEsVisible() != null) {
            existente.setEsVisible(dto.getEsVisible());
        }

        return modelMapper.map(usuarioColeccionRepository.save(existente),UsuarioColeccionOutDTO.class);
    }

    @Override
    public UsuarioColeccionOutDTO actualizarFavorita(Long id, UsuarioColeccionFavoritaDTO dto, String emailUsuario, boolean esAdmin, boolean esMods) {

        UsuarioColeccion uc = usuarioColeccionRepository.findById(id)
                .orElseThrow(() -> new UsuarioColeccionNoEncontradoException(id));

        Usuario usuarioActual = currentUserResolver.usuarioActual(emailUsuario);
        Permisos.checkPuedeEditarOBorrarUsuarioColeccion(uc, usuarioActual, esAdmin, esMods);

        uc.setEsFavorita(dto.getEsFavorita());

        UsuarioColeccion guardado = usuarioColeccionRepository.save(uc);
        return modelMapper.map(guardado, UsuarioColeccionOutDTO.class);
    }

    @Override
    public UsuarioColeccionOutDTO actualizarVisible(Long id, UsuarioColeccionVisibleDTO dto, String emailUsuario, boolean esAdmin, boolean esMods) {

        UsuarioColeccion uc = usuarioColeccionRepository.findById(id)
                .orElseThrow(() -> new UsuarioColeccionNoEncontradoException(id));

        Usuario usuarioActual = currentUserResolver.usuarioActual(emailUsuario);
        Permisos.checkPuedeEditarOBorrarUsuarioColeccion(uc, usuarioActual, esAdmin, esMods);
        uc.setEsVisible(dto.getEsVisible());

        return modelMapper.map(usuarioColeccionRepository.save(uc),UsuarioColeccionOutDTO.class);
    }

    @Override
    public void eliminar(Long id)
            throws UsuarioColeccionNoEncontradoException {

        UsuarioColeccion uc = usuarioColeccionRepository.findById(id)
                .orElseThrow(() -> new UsuarioColeccionNoEncontradoException(id));

        usuarioColeccionRepository.delete(uc);
    }

    @Transactional
    @Override
    public void eliminarV2(Long id, String emailUsuario, boolean esAdmin, boolean esMods) {
        UsuarioColeccion uc = usuarioColeccionRepository.findById(id)
                .orElseThrow(() -> new UsuarioColeccionNoEncontradoException(id));
        Usuario usuarioActual = currentUserResolver.usuarioActual(emailUsuario);
        Permisos.checkPuedeEditarOBorrarUsuarioColeccion(uc, usuarioActual, esAdmin, esMods);

        usuarioItemRepository.deleteByUsuario_IdAndColeccion_Id(
                uc.getUsuario().getId(),
                uc.getColeccion().getId()
        );

        usuarioColeccionRepository.delete(uc);
    }
}
