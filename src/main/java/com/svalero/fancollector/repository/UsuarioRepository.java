package com.svalero.fancollector.repository;

import com.svalero.fancollector.domain.Usuario;
import com.svalero.fancollector.domain.enums.RolUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    Optional<Usuario> findByEmail(String email);

    @Query("""
       SELECT u FROM Usuario u
       WHERE (:nombre IS NULL OR LOWER(u.nombre) LIKE CONCAT('%', LOWER(:nombre), '%'))
       AND (:email IS NULL OR LOWER(u.email) LIKE CONCAT('%', LOWER(:email), '%'))
       AND (:rol IS NULL OR u.rol = :rol)
       """)
    List<Usuario> buscarPorFiltros(
            @Param("nombre") String nombre,
            @Param("email") String email,
            @Param("rol") RolUsuario rol);
}
