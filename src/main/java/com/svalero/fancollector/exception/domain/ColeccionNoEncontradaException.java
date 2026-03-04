package com.svalero.fancollector.exception.domain;

public class ColeccionNoEncontradaException extends RuntimeException {

    public ColeccionNoEncontradaException(Long id) {
        super("Colección con id " + id + " no encontrada");
    }

    public ColeccionNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
