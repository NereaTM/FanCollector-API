package com.svalero.fancollector.service;

import com.svalero.fancollector.domain.Coleccion;
import com.svalero.fancollector.domain.Usuario;
import com.svalero.fancollector.domain.UsuarioColeccion;
import com.svalero.fancollector.dto.ColeccionInDTO;
import com.svalero.fancollector.dto.ColeccionOutDTO;
import com.svalero.fancollector.dto.ColeccionPutDTO;
import com.svalero.fancollector.exception.domain.ColeccionNoEncontradaException;
import com.svalero.fancollector.exception.domain.UsuarioNoEncontradoException;
import com.svalero.fancollector.exception.security.AccesoDenegadoException;
import com.svalero.fancollector.repository.ColeccionRepository;
import com.svalero.fancollector.repository.UsuarioColeccionRepository;
import com.svalero.fancollector.security.auth.CurrentUserResolver;
import com.svalero.fancollector.security.auth.Permisos;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ColeccionServiceImpl implements ColeccionService {

    @Autowired
    private ColeccionRepository coleccionRepository;


    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CurrentUserResolver currentUserResolver;

    @Autowired
    private UsuarioColeccionRepository usuarioColeccionRepository;

    @Override
    @Transactional
    public ColeccionOutDTO crearColeccion(ColeccionInDTO coleccionInDto, String emailUsuario) {

        Usuario creador = currentUserResolver.usuarioActual(emailUsuario);

        Coleccion coleccion = modelMapper.map(coleccionInDto, Coleccion.class);
        coleccion.setCreador(creador);

        Coleccion guardada = coleccionRepository.save(coleccion);

        UsuarioColeccion usuarioColeccion = new UsuarioColeccion();
        usuarioColeccion.setUsuario(creador);
        usuarioColeccion.setColeccion(guardada);
        usuarioColeccion.setEsCreador(true);
        usuarioColeccion.setEsFavorita(false);
        usuarioColeccion.setEsVisible(true);
        usuarioColeccion.setFechaAgregada(LocalDateTime.now());

        usuarioColeccionRepository.save(usuarioColeccion);
        return modelMapper.map(guardada, ColeccionOutDTO.class);
    }

    @Override
    public ColeccionOutDTO buscarColeccionPorId(Long id, String emailUsuario, boolean esAdmin, boolean esMods) {
        Coleccion coleccion = coleccionRepository.findById(id)
                .orElseThrow(() -> new ColeccionNoEncontradaException(id));
        // sin logearme
        if (emailUsuario == null) {
            if (coleccion.isEsPublica()) {
                return modelMapper.map(coleccion, ColeccionOutDTO.class);
            }
            throw new ColeccionNoEncontradaException(id);
        }
        // logeada
        Usuario actual = currentUserResolver.usuarioActual(emailUsuario);

        Permisos.checkPuedeVerColeccion(coleccion, actual, esAdmin);

        return modelMapper.map(coleccion, ColeccionOutDTO.class);
    }

    @Override
    public List<ColeccionOutDTO> listarColecciones(String nombre, String categoria, Long idCreador, String nombreCreador, String emailUsuario, boolean esAdmin, boolean esMods, Boolean usableComoPlantilla) {

        List<Coleccion> colecciones;
        boolean noHayFiltros = (nombre == null || nombre.isBlank()) &&
                (categoria == null || categoria.isBlank()) &&
                (idCreador == null) &&
                (nombreCreador == null || nombreCreador.isBlank()) &&
                (usableComoPlantilla == null);
        if (noHayFiltros) {
            colecciones = coleccionRepository.findAll();
        } else {
            colecciones = coleccionRepository.buscarPorFiltros(nombre, categoria, idCreador, nombreCreador,usableComoPlantilla);
        }
        //cuando no estoy logeada
        if (emailUsuario == null || emailUsuario.isBlank()) {
            return colecciones.stream()
                    .filter(Coleccion::isEsPublica)
                    .map(c -> modelMapper.map(c, ColeccionOutDTO.class))
                    .toList();
        }
        //cuando estoy logeada
        Usuario actual = currentUserResolver.usuarioActual(emailUsuario);

         return colecciones.stream()
                .filter(c -> Permisos.puedeVerColeccion(c, actual, esAdmin))
                .map(c -> modelMapper.map(c, ColeccionOutDTO.class))
                .toList();
    }

    @Override
    public ColeccionOutDTO actualizarColeccion(Long id, ColeccionPutDTO coleccionPutDTO, String emailUsuario, boolean esAdmin, boolean esMods) {

        Coleccion existente = coleccionRepository.findById(id)
                .orElseThrow(() -> new ColeccionNoEncontradaException(id));

        Usuario actual = currentUserResolver.usuarioActual(emailUsuario);
        Permisos.checkPuedeEditarOBorrarColeccion(existente, actual, esAdmin, esMods);

        existente.setNombre(coleccionPutDTO.getNombre());
        existente.setDescripcion(coleccionPutDTO.getDescripcion());
        existente.setCategoria(coleccionPutDTO.getCategoria());
        existente.setImagenPortada(coleccionPutDTO.getImagenPortada());
        existente.setEsPublica(coleccionPutDTO.getEsPublica());
        existente.setUsableComoPlantilla(coleccionPutDTO.getUsableComoPlantilla());

        return modelMapper.map(coleccionRepository.save(existente),ColeccionOutDTO.class);
    }

    @Override
    public ColeccionOutDTO actualizarEsPublica(Long id, Boolean esPublica, String emailUsuario, boolean esAdmin, boolean esMods) {
        Coleccion coleccion = coleccionRepository.findById(id)
                .orElseThrow(() -> new ColeccionNoEncontradaException(id));

        Usuario actual = currentUserResolver.usuarioActual(emailUsuario);

        Permisos.checkPuedeEditarOBorrarColeccion(coleccion, actual, esAdmin, esMods);

        coleccion.setEsPublica(esPublica);

        return modelMapper.map(coleccionRepository.save(coleccion), ColeccionOutDTO.class);
    }

    @Override
    public ColeccionOutDTO actualizarUsableComoPlantilla(Long id, Boolean usableComoPlantilla, String emailUsuario, boolean esAdmin) {
        Coleccion coleccion = coleccionRepository.findById(id)
                .orElseThrow(() -> new ColeccionNoEncontradaException(id));

        Usuario actual = currentUserResolver.usuarioActual(emailUsuario);
        if (!esAdmin && !Permisos.esCreador(coleccion, actual)) {throw new AccesoDenegadoException();}

        coleccion.setUsableComoPlantilla(usableComoPlantilla);

        return modelMapper.map(coleccionRepository.save(coleccion), ColeccionOutDTO.class);
    }

    @Override
    @Transactional
    public void eliminarColeccion(Long id, String emailUsuario, boolean esAdmin, boolean esMods) {
        Coleccion coleccion = coleccionRepository.findById(id)
                .orElseThrow(() -> new ColeccionNoEncontradaException(id));

        Usuario actual = currentUserResolver.usuarioActual(emailUsuario);

        Permisos.checkPuedeEditarOBorrarColeccion(coleccion, actual, esAdmin, esMods);

        coleccionRepository.delete(coleccion);
    }
}