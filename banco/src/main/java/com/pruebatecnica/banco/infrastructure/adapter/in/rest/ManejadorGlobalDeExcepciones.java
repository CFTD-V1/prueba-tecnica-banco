package com.pruebatecnica.banco.infrastructure.adapter.in.rest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.pruebatecnica.banco.domain.exception.ExcepcionDeNegocio;
import com.pruebatecnica.banco.domain.exception.ExcepcionDeRecursoNoEncontrado;

/**
 * Traduce las excepciones del dominio y del framework a respuestas HTTP con el
 * formato ProblemDetail (RFC 7807), para que la API responda siempre igual.
 */
@RestControllerAdvice
public class ManejadorGlobalDeExcepciones {

    @ExceptionHandler(ExcepcionDeNegocio.class)
    public ProblemDetail manejarExcepcionDeNegocio(ExcepcionDeNegocio excepcion) {
        return problema(HttpStatus.BAD_REQUEST, "Regla de negocio incumplida", excepcion.getMessage());
    }

    @ExceptionHandler(ExcepcionDeRecursoNoEncontrado.class)
    public ProblemDetail manejarRecursoNoEncontrado(ExcepcionDeRecursoNoEncontrado excepcion) {
        return problema(HttpStatus.NOT_FOUND, "Recurso no encontrado", excepcion.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail manejarErroresDeValidacion(MethodArgumentNotValidException excepcion) {
        Map<String, String> errores = new HashMap<>();
        excepcion.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));

        ProblemDetail problema = problema(
                HttpStatus.BAD_REQUEST, "Error de validación", "La petición contiene campos inválidos");
        problema.setProperty("errores", errores);
        return problema;
    }

    /** Cuerpos JSON mal formados o con valores que no corresponden al tipo esperado. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail manejarCuerpoIlegible() {
        return problema(HttpStatus.BAD_REQUEST, "Petición mal formada",
                "El cuerpo de la petición no se pudo leer o tiene valores inválidos");
    }

    /**
     * Última red de seguridad de la base de datos: si dos peticiones simultáneas
     * intentan insertar un valor único repetido, la restricción de la BD lo impide
     * y aquí se traduce a un 409 en lugar de un error 500.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail manejarViolacionDeIntegridad() {
        return problema(HttpStatus.CONFLICT, "Conflicto de datos",
                "La operación no se pudo completar porque viola una restricción de la base de datos");
    }

    private ProblemDetail problema(HttpStatus estado, String titulo, String detalle) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalle);
        problema.setTitle(titulo);
        return problema;
    }
}
