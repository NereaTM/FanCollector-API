package com.svalero.fancollector.exception.domain;

public class ItemNoEncontradoException extends RuntimeException {

    public ItemNoEncontradoException(Long id) {
        super("Item con id " + id + " no encontrado");
    }

    public ItemNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
