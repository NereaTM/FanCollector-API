package com.svalero.fancollector.exception;

import com.svalero.fancollector.exception.domain.*;
import com.svalero.fancollector.exception.validation.EmailDuplicadoException;
import com.svalero.fancollector.exception.validation.RelacionYaExisteException;
import com.svalero.fancollector.util.ErrorRespuesta;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.core.AuthenticationException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> handleValidacionErrores(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();

        for (var error : ex.getBindingResult().getFieldErrors()) {errores.put(error.getField(), error.getDefaultMessage());}

        ErrorRespuesta respuesta = new ErrorRespuesta
                (400, "Errores de validación", errores);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    @ExceptionHandler(EmailDuplicadoException.class)
    public ResponseEntity<ErrorRespuesta> handleEmailDuplicado(EmailDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorRespuesta.error400(ex.getMessage()));
    }

    @ExceptionHandler(RelacionYaExisteException.class)
    public ResponseEntity<ErrorRespuesta> handleRelacionYaExiste(RelacionYaExisteException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorRespuesta.error400(ex.getMessage()));
    }

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> handleUsuarioNoEncontrado(UsuarioNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorRespuesta.error404(ex.getMessage()));
    }

    @ExceptionHandler(ColeccionNoEncontradaException.class)
    public ResponseEntity<ErrorRespuesta> handleColeccionNoEncontrada(ColeccionNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorRespuesta.error404(ex.getMessage()));
    }

    @ExceptionHandler(ItemNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> handleItemNoEncontrado(ItemNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorRespuesta.error404(ex.getMessage()));
    }

    @ExceptionHandler(UsuarioColeccionNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> handleUsuarioColeccionNoEncontrado(UsuarioColeccionNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorRespuesta.error404(ex.getMessage()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorRespuesta> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(new ErrorRespuesta(405, ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorRespuesta> handleAuth(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorRespuesta(401, "Email o contraseña incorrectos"));
    }

    @ExceptionHandler(UsuarioItemNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> handleUsuarioItemNoEncontrado(UsuarioItemNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorRespuesta.error404(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> handleErrorGeneral(Exception ex) {
        System.err.println("Error: " + ex.getMessage());
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorRespuesta.error500("Error interno del servidor"));
    }


}