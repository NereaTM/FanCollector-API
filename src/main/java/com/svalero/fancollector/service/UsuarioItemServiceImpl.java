package com.svalero.fancollector.service;

import com.svalero.fancollector.domain.Coleccion;
import com.svalero.fancollector.domain.Item;
import com.svalero.fancollector.domain.Usuario;
import com.svalero.fancollector.domain.UsuarioItem;
import com.svalero.fancollector.domain.enums.EstadoItem;
import com.svalero.fancollector.dto.UsuarioItemDetalleDTO;
import com.svalero.fancollector.dto.UsuarioItemInDTO;
import com.svalero.fancollector.dto.UsuarioItemOutDTO;
import com.svalero.fancollector.dto.UsuarioItemPutDTO;
import com.svalero.fancollector.exception.domain.ColeccionNoEncontradaException;
import com.svalero.fancollector.exception.domain.ItemNoEncontradoException;
import com.svalero.fancollector.exception.domain.UsuarioItemNoEncontradoException;
import com.svalero.fancollector.exception.domain.UsuarioNoEncontradoException;
import com.svalero.fancollector.exception.security.AccesoDenegadoException;
import com.svalero.fancollector.exception.validation.RelacionYaExisteException;
import com.svalero.fancollector.repository.ColeccionRepository;
import com.svalero.fancollector.repository.ItemRepository;
import com.svalero.fancollector.repository.UsuarioItemRepository;
import com.svalero.fancollector.repository.UsuarioRepository;
import com.svalero.fancollector.security.auth.CurrentUserResolver;
import com.svalero.fancollector.security.auth.Permisos;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UsuarioItemServiceImpl implements UsuarioItemService {

    @Autowired
    private UsuarioItemRepository usuarioItemRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ColeccionRepository coleccionRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CurrentUserResolver currentUserResolver;

    @Override
    public UsuarioItemOutDTO crear(UsuarioItemInDTO dto, String emailUsuario, boolean esAdmin, boolean esMods) {
        Usuario usuarioActual = currentUserResolver.usuarioActual(emailUsuario);
        Usuario usuarioDueno;
        if (dto.getIdUsuario() != null) {
            usuarioDueno = usuarioRepository.findById(dto.getIdUsuario())
                    .orElseThrow(() -> new UsuarioNoEncontradoException(dto.getIdUsuario()));

            if (!esAdmin && !esMods && !Permisos.esElMismoUsuario(usuarioDueno, emailUsuario)) {
                throw new AccesoDenegadoException("Solo puedes crear items para ti mismo");
            }
        } else {
            usuarioDueno = usuarioActual;
        }
        if (usuarioItemRepository.existsByUsuarioIdAndColeccionIdAndItemId(
                dto.getIdUsuario(), dto.getIdColeccion(), dto.getIdItem())) {
            throw new RelacionYaExisteException();
        }

        Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new UsuarioNoEncontradoException(dto.getIdUsuario()));

        Coleccion coleccion = coleccionRepository.findById(dto.getIdColeccion())
                .orElseThrow(() -> new ColeccionNoEncontradaException(dto.getIdColeccion()));

        Permisos.checkPuedeVerColeccion(coleccion, usuarioActual, esAdmin);

        Item item = itemRepository.findById(dto.getIdItem())
                .orElseThrow(() -> new ItemNoEncontradoException(dto.getIdItem()));

        if (!item.getColeccion().getId().equals(coleccion.getId())) {
            throw new IllegalArgumentException("El ítem no pertenece a la colección indicada");
        }

        EstadoItem estado = dto.getEstado() != null ? dto.getEstado() : EstadoItem.BUSCO;
        boolean esVisible = dto.getEsVisible() != null ? dto.getEsVisible() : true;
        Integer cantidad = normalizarCantidad(estado, dto.getCantidad());

        UsuarioItem ui = new UsuarioItem();
        ui.setUsuario(usuario);
        ui.setColeccion(coleccion);
        ui.setItem(item);
        ui.setEstado(estado);
        ui.setEsVisible(esVisible);
        ui.setCantidad(cantidad);
        ui.setNotas(dto.getNotas());

        return modelMapper.map(usuarioItemRepository.save(ui),UsuarioItemOutDTO.class);
    }

    @Override
    public UsuarioItemOutDTO buscarPorId(Long id, String emailUsuario, boolean esAdmin, boolean esMods) {
        UsuarioItem ui = usuarioItemRepository.findById(id)
                .orElseThrow(() -> new UsuarioItemNoEncontradoException(id));

        Usuario usuarioActual = (emailUsuario == null || emailUsuario.isBlank())
                ? null
                : currentUserResolver.usuarioActual(emailUsuario);
        Permisos.checkPuedeVerUsuarioItem(ui, usuarioActual, esAdmin, esMods);

        return modelMapper.map(ui, UsuarioItemOutDTO.class);
    }

    @Override
    public List<UsuarioItemDetalleDTO> buscarPorUsuarioYColeccion(Long idUsuario, Long idColeccion, String emailUsuario, boolean esAdmin, boolean esMods) {

        List<UsuarioItem> items = usuarioItemRepository.buscarPorFiltros(
                idUsuario, null, idColeccion, null, null);

        Usuario usuarioActual = null;
        if (emailUsuario != null && !emailUsuario.isBlank()) {
            usuarioActual = currentUserResolver.usuarioActual(emailUsuario);
        }

        List<UsuarioItemDetalleDTO> resultado = new ArrayList<>();
        for (UsuarioItem ui : items) {
            if (Permisos.puedeVerUsuarioItem(ui, usuarioActual, esAdmin, esMods)) {
                resultado.add(toDetalleDTO(ui));
            }
        }
        return resultado;
    }

    @Override
    public List<UsuarioItemOutDTO> listar(Long idUsuario, Long idItem, Long idColeccion, EstadoItem estado, Boolean esVisible,
            String emailUsuario, boolean esAdmin, boolean esMods) {

        boolean noHayFiltros = true;

        if (idUsuario != null) noHayFiltros = false;
        if (idItem != null) noHayFiltros = false;
        if (idColeccion != null) noHayFiltros = false;
        if (estado != null) noHayFiltros = false;
        if (esVisible != null) noHayFiltros = false;

        List<UsuarioItem> items;

        if (noHayFiltros) {
            items = usuarioItemRepository.findAll();
        } else {
            items = usuarioItemRepository.buscarPorFiltros(
                    idUsuario, idItem, idColeccion, estado, esVisible);
        }

        Usuario usuarioActual = null;
        if (emailUsuario != null && !emailUsuario.isBlank()) {
            usuarioActual = currentUserResolver.usuarioActual(emailUsuario);
        }

        List<UsuarioItemOutDTO> resultado = new ArrayList<>();
        for (UsuarioItem ui : items) {
            if (Permisos.puedeVerUsuarioItem(ui, usuarioActual, esAdmin, esMods)) {
                resultado.add(modelMapper.map(ui, UsuarioItemOutDTO.class));
            }
        }
        return resultado;
    }

    @Override
    public UsuarioItemOutDTO actualizarCompleto(Long id, UsuarioItemPutDTO dto, String emailUsuario, boolean esAdmin, boolean esMods) {

        UsuarioItem existente = usuarioItemRepository.findById(id)
                .orElseThrow(() -> new UsuarioItemNoEncontradoException(id));

        Usuario usuarioActual = currentUserResolver.usuarioActual(emailUsuario);
        Permisos.checkPuedeEditarOBorrarUsuarioItem(existente, usuarioActual, esAdmin, esMods);

        if (dto.getEstado() != null) {
            existente.setEstado(dto.getEstado());
            existente.setCantidad(normalizarCantidad(dto.getEstado(), existente.getCantidad()));}

        if (dto.getCantidad() != null) {
            existente.setCantidad(normalizarCantidad(existente.getEstado(), dto.getCantidad()));}

        if (dto.getNotas() != null) {
            existente.setNotas(dto.getNotas());}

        if (dto.getEsVisible() != null) {
            existente.setEsVisible(dto.getEsVisible());}

        return modelMapper.map(usuarioItemRepository.save(existente), UsuarioItemOutDTO.class);
    }

    @Override
    public UsuarioItemOutDTO actualizarCompletoV2(Long id, UsuarioItemPutDTO dto, String emailUsuario, boolean esAdmin, boolean esMods) {

        UsuarioItem existente = usuarioItemRepository.findById(id)
                .orElseThrow(() -> new UsuarioItemNoEncontradoException(id));

        Usuario usuarioActual = currentUserResolver.usuarioActual(emailUsuario);
        Permisos.checkPuedeEditarOBorrarUsuarioItem(existente, usuarioActual, esAdmin, esMods);

        EstadoItem estadoFinal = dto.getEstado() != null ? dto.getEstado() : existente.getEstado();
        existente.setEstado(estadoFinal);

        // v2: la cantidad se ajusta automáticamente según el estado, sin que el cliente tenga que calcularla
        existente.setCantidad(estadoFinal == EstadoItem.BUSCO ? 0 : 1);

        if (dto.getNotas() != null) {
            existente.setNotas(dto.getNotas());}

        if (dto.getEsVisible() != null) {
            existente.setEsVisible(dto.getEsVisible());}

        return modelMapper.map(usuarioItemRepository.save(existente), UsuarioItemOutDTO.class);
    }

    @Override
    public UsuarioItemOutDTO actualizarVisibilidad(Long id, Boolean esVisible, String emailUsuario, boolean esAdmin, boolean esMods) {

        UsuarioItem usuarioItem = usuarioItemRepository.findById(id)
                .orElseThrow(() -> new UsuarioItemNoEncontradoException(id));

        Usuario usuarioActual = currentUserResolver.usuarioActual(emailUsuario);
        Permisos.checkPuedeEditarOBorrarUsuarioItem(usuarioItem, usuarioActual, esAdmin, esMods);

        usuarioItem.setEsVisible(esVisible);

        return modelMapper.map(usuarioItemRepository.save(usuarioItem),UsuarioItemOutDTO.class);
    }

    @Override
    public void eliminar(Long id, String emailUsuario, boolean esAdmin, boolean esMods) {
        UsuarioItem usuarioItem = usuarioItemRepository.findById(id)
                .orElseThrow(() -> new UsuarioItemNoEncontradoException(id));

        Usuario usuarioActual = currentUserResolver.usuarioActual(emailUsuario);
        Permisos.checkPuedeEditarOBorrarUsuarioItem(usuarioItem, usuarioActual, esAdmin, esMods);

        usuarioItemRepository.deleteById(id);
    }

    private Integer normalizarCantidad(EstadoItem estado, Integer cantidad) {
        if (estado == EstadoItem.BUSCO) return 0;
        if (cantidad == null || cantidad < 1) return 1;
        return cantidad;
    }

    private UsuarioItemDetalleDTO toDetalleDTO(UsuarioItem ui) {
        UsuarioItemDetalleDTO dto = new UsuarioItemDetalleDTO();

        dto.setId(ui.getId());
        dto.setIdUsuario(ui.getUsuario().getId());
        dto.setNombreUsuario(ui.getUsuario().getNombre());
        dto.setIdColeccion(ui.getColeccion().getId());
        dto.setNombreColeccion(ui.getColeccion().getNombre());
        dto.setIdItem(ui.getItem().getId());
        dto.setNombreItem(ui.getItem().getNombre());
        dto.setEstado(ui.getEstado());
        dto.setCantidad(ui.getCantidad());
        dto.setNotas(ui.getNotas());
        dto.setEsVisible(ui.isEsVisible());
        dto.setFechaRegistro(ui.getFechaRegistro());

        dto.setDescripcionItem(ui.getItem().getDescripcion());
        dto.setImagenUrl(ui.getItem().getImagenUrl());
        dto.setTipo(ui.getItem().getTipo());
        dto.setRareza(ui.getItem().getRareza());
        dto.setAnioLanzamiento(ui.getItem().getAnioLanzamiento());

        return dto;
    }
}
