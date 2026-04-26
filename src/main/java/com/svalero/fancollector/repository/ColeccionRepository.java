package com.svalero.fancollector.repository;

import com.svalero.fancollector.domain.Coleccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColeccionRepository extends JpaRepository<Coleccion, Long> {

    @Query("""
        SELECT c FROM Coleccion c WHERE
        (:nombre IS NULL OR :nombre = '' OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
        AND (:categoria IS NULL OR :categoria = '' OR LOWER(c.categoria) LIKE LOWER(CONCAT('%', :categoria, '%')))
        AND (:idCreador IS NULL OR c.creador.id = :idCreador)
        AND (:nombreCreador IS NULL OR :nombreCreador = '' OR LOWER(c.creador.nombre) LIKE LOWER(CONCAT('%', :nombreCreador, '%')))
        AND (:usableComoPlantilla IS NULL OR c.usableComoPlantilla = :usableComoPlantilla)
    """)
    List<Coleccion> buscarPorFiltros(
            @Param("nombre") String nombre,
            @Param("categoria") String categoria,
            @Param("idCreador") Long idCreador,
            @Param("nombreCreador") String nombreCreador,
            @Param("usableComoPlantilla") Boolean usableComoPlantilla
    );
}