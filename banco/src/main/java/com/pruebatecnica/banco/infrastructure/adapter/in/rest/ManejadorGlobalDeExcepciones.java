package com.pruebatecnica.banco.infrastructure.adapter.in.rest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.pruebatecnica.banco.domain.exception.ExcepcionDeNegocio;

@RestControllerAdvice
public class ManejadorGlobalDeExcepciones {

    @ExceptionHandler(ExcepcionDeNegocio.class)
    public ProblemDetail manejarExcepcionDeNegocio(ExcepcionDeNegocio excepcion) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, excepcion.getMessage());
        problema.setTitle("Regla de negocio incumplida");
        return problema;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail manejarErroresDeValidacion(MethodArgumentNotValidException excepcion) {
        Map<String, String> errores = new HashMap<>();
        excepcion.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "La peticion contiene campos invalidos");
        problema.setTitle("Error de validacion");
        problema.setProperty("errores", errores);
        return problema;
    }

    /** Cuerpos JSON mal formados o con valores que no corresponden al tipo esperado. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail manejarCuerpoIlegible(HttpMessageNotReadableException excepcion) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "El cuerpo de la peticion no se pudo leer o tiene valores invalidos");
        problema.setTitle("Peticion mal formada");
        return problema;
    }
}