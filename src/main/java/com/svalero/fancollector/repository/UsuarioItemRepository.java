package com.svalero.fancollector.repository;

import com.svalero.fancollector.domain.UsuarioItem;
import com.svalero.fancollector.domain.enums.EstadoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioItemRepository extends JpaRepository<UsuarioItem, Long> {

    boolean existsByUsuarioIdAndColeccionIdAndItemId(Long usuarioId, Long coleccionId, Long itemId);
    void deleteByUsuario_IdAndColeccion_Id(Long usuarioId, Long coleccionId);

    @Query("""
        SELECT ui FROM UsuarioItem ui
        WHERE (:idUsuario IS NULL OR ui.usuario.id = :idUsuario)
          AND (:idItem IS NULL OR ui.item.id = :idItem)
          AND (:idColeccion IS NULL OR ui.coleccion.id = :idColeccion)
          AND (:estado IS NULL OR ui.estado = :estado)
          AND (:esVisible IS NULL OR ui.esVisible = :esVisible)
    """)
    List<UsuarioItem> buscarPorFiltros(
            @Param("idUsuario") Long idUsuario,
            @Param("idItem") Long idItem,
            @Param("idColeccion") Long idColeccion,
            @Param("estado") EstadoItem estado,
            @Param("esVisible") Boolean esVisible
    );

}
