package com.svalero.fancollector.exception.validation;

public class RelacionYaExisteException extends RuntimeException {

    public RelacionYaExisteException() {
        super("La relación ya existe");
    }

    public RelacionYaExisteException(String mensaje) {
        super(mensaje);
    }
}