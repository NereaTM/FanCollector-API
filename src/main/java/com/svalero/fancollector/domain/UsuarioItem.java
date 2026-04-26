package com.svalero.fancollector.domain;

import com.svalero.fancollector.domain.enums.EstadoItem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "usuario_items",
        uniqueConstraints = @UniqueConstraint(
                name = "unique_usuario_coleccion_item",
                columnNames = {"usuario_id", "coleccion_id", "item_id"}))

public class UsuarioItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "coleccion_id", nullable = false)
    private Coleccion coleccion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoItem estado;

    @Column
    private Integer cantidad = 1;

    @Lob
    @Column
    private String notas;

    @Column(name = "es_visible")
    private boolean esVisible = true;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }
}