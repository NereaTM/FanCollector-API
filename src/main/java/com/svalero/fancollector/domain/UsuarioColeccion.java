package com.svalero.fancollector.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuario_coleccion",
        uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "coleccion_id"}))

public class UsuarioColeccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "coleccion_id")
    private Coleccion coleccion;

    @Column(name = "fecha_agregada")
    private LocalDateTime fechaAgregada = LocalDateTime.now();

    @Column(name = "es_favorita")
    private boolean esFavorita = false;

    @Column(name = "es_creador")
    private boolean esCreador = false;

    @Column(name = "es_visible")
    private boolean esVisible = true;
}