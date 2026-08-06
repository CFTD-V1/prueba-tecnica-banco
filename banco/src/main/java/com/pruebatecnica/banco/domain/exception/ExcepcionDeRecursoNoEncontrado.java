package com.pruebatecnica.banco.domain.exception;

/**
 * Se lanza cuando se consulta un recurso que no existe.
 * Se separa de {@link ExcepcionDeNegocio} porque la respuesta HTTP es distinta:
 * 404 (no existe) frente a 400 (la peticion viola una regla de negocio).
 */
public class ExcepcionDeRecursoNoEncontrado extends RuntimeException {

    public ExcepcionDeRecursoNoEncontrado(String mensaje) {
        super(mensaje);
    }
}
