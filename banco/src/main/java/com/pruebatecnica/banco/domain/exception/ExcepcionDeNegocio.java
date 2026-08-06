package com.pruebatecnica.banco.domain.exception;

public class ExcepcionDeNegocio extends RuntimeException {

    public ExcepcionDeNegocio(String mensaje) {
        super(mensaje);
    }
}