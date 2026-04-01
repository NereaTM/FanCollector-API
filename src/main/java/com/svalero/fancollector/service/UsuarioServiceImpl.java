package com.svalero.fancollector.service;

import com.svalero.fancollector.domain.Usuario;
import com.svalero.fancollector.domain.enums.RolUsuario;
import com.svalero.fancollector.dto.UsuarioInDTO;
import com.svalero.fancollector.dto.UsuarioOutDTO;
import com.svalero.fancollector.dto.UsuarioPutDTO;
import com.svalero.fancollector.exception.domain.UsuarioNoEncontradoException;
import com.svalero.fancollector.exception.security.AccesoDenegadoException;
import com.svalero.fancollector.exception.validation.EmailDuplicadoException;
import com.svalero.fancollector.repository.UsuarioRepository;
import com.svalero.fancollector.security.auth.Permisos;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UsuarioOutDTO crearUsuario(UsuarioInDTO usuarioInDTO) {
        if (usuarioRepository.existsByEmail(usuarioInDTO.getEmail())) {
            throw new EmailDuplicadoException(usuarioInDTO.getEmail());
        }
        Usuario usuario = modelMapper.map(usuarioInDTO, Usuario.class);
        usuario.setRol(RolUsuario.USER);
        usuario.setContrasena(passwordEncoder.encode(usuarioInDTO.getContrasena()));
        Usuario guardado = usuarioRepository.save(usuario);
        return modelMapper.map(guardado, UsuarioOutDTO.class);
    }

    @Override
    public UsuarioOutDTO crearUsuarioComoAdmin(UsuarioInDTO dto, String emailUsuario, boolean esAdmin) {
        if (!esAdmin) {throw new AccesoDenegadoException("No eres ADMIN");}
        if (usuarioRepository.existsByEmail(dto.getEmail())) {throw new EmailDuplicadoException(dto.getEmail());
        }
        Usuario usuario = modelMapper.map(dto, Usuario.class);
        usuario.setRol(RolUsuario.USER);
        usuario.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        Usuario guardado = usuarioRepository.save(usuario);
        return modelMapper.map(guardado, UsuarioOutDTO.class);
    }

    @Override
    public UsuarioOutDTO buscarUsuarioPorId(long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));

        UsuarioOutDTO resultado = modelMapper.map(usuario, UsuarioOutDTO.class);
        return resultado;
    }


    @Override
    public List<UsuarioOutDTO> listarUsuarios(String nombre, String email, RolUsuario rol) {
        List<Usuario> usuarios;

        boolean noHayFiltros = true;
        if (rol != null) noHayFiltros = false;
        if (nombre != null && !nombre.isBlank()) noHayFiltros = false;
        if (email != null && !email.isBlank()) noHayFiltros = false;

        if (noHayFiltros) {
            usuarios = usuarioRepository.findAll();
        } else {
            usuarios = usuarioRepository.buscarPorFiltros(nombre, email, rol);
        }

        List<UsuarioOutDTO> resultado = new ArrayList<>();
        for (Usuario u : usuarios) {
            resultado.add(modelMapper.map(u, UsuarioOutDTO.class));
        }
        return resultado;
    }

    @Override
    public UsuarioOutDTO modificarUsuario(long id, UsuarioPutDTO usuarioPutDTO, String emailUsuario, boolean esAdmin, boolean esMods) {

        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));

        Permisos.checkPuedeEditarUsuario(existente, emailUsuario, esAdmin, esMods);

        existente.setNombre(usuarioPutDTO.getNombre());
        existente.setUrlAvatar(usuarioPutDTO.getUrlAvatar());
        existente.setDescripcion(usuarioPutDTO.getDescripcion());
        existente.setContactoPublico(usuarioPutDTO.getContactoPublico());

        // Validar email si cambió
        String email = usuarioPutDTO.getEmail();
        if (email != null && !email.equalsIgnoreCase(existente.getEmail())) {
            if (usuarioRepository.existsByEmailAndIdNot(email, id)) {
                throw new EmailDuplicadoException(email);
            }
            existente.setEmail(email);
        }

        Usuario guardado = usuarioRepository.save(existente);
        return modelMapper.map(guardado, UsuarioOutDTO.class);
    }

    @Override
    public UsuarioOutDTO actualizarContrasena(long id, String nuevaContrasena, String emailUsuario, boolean esAdmin, boolean esMods) {

        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));

        Permisos.checkPuedeEditarUsuario(existente, emailUsuario, esAdmin, esMods);

        existente.setContrasena(passwordEncoder.encode(nuevaContrasena));
        Usuario guardado = usuarioRepository.save(existente);
        return modelMapper.map(guardado, UsuarioOutDTO.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public UsuarioOutDTO actualizarRol(long id, RolUsuario nuevoRol, String emailUsuario) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
        // el admin no se puede autoeliminar
        if (usuario.getEmail().equalsIgnoreCase(emailUsuario)
                && nuevoRol != RolUsuario.ADMIN) {
            throw new AccesoDenegadoException("No puedes quitarte el rol de ADMIN.");
        }

        usuario.setRol(nuevoRol);
        Usuario guardado = usuarioRepository.save(usuario);
        return modelMapper.map(guardado, UsuarioOutDTO.class);
    }

    @Override
    public void borrarUsuario(long id, String emailUsuario, boolean esAdmin, boolean esMods) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));

        Permisos.checkPuedeEditarUsuario(usuario, emailUsuario, esAdmin, esMods);
        // el admin no se puede autoeliminar
        if (esAdmin && usuario.getEmail().equalsIgnoreCase(emailUsuario)) {
            throw new AccesoDenegadoException("No puedes borrarte la cuenta a ti mismo");
        }

        // un moderador no puede borrar a un admin
        if (esMods && !esAdmin && usuario.getRol() == RolUsuario.ADMIN) {
            throw new AccesoDenegadoException("No tienes permisos para eliminar a un administrador");
        }

        usuarioRepository.delete(usuario);
    }
}
