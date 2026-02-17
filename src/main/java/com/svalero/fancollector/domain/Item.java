package com.svalero.fancollector.domain;

import com.svalero.fancollector.domain.enums.RarezaItem;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "coleccion_plantilla_id", nullable = false)
    private Coleccion coleccion;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String nombre;

    @Lob
    @Column
    private String descripcion;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Column(length = 50)
    private String tipo;

    @Enumerated(EnumType.STRING)
    private RarezaItem rareza = RarezaItem.COMUN;

    @Column(name = "anio_lanzamiento")
    private Integer anioLanzamiento;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UsuarioItem> usuarioItems = new ArrayList<>();
}
