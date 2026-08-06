package com.pruebatecnica.banco.domain.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Unico punto donde el dominio obtiene la fecha y hora actuales.
 *
 * Java maneja nanosegundos y las columnas de tipo timestamp de la base de datos
 * guardan microsegundos. Si se usara LocalDateTime.now() sin mas, la respuesta de
 * una creacion devolveria una precision que la base no conserva y ese mismo campo
 * se veria distinto al volver a consultarlo. Truncando aqui, la API devuelve
 * siempre el mismo valor.
 */
public final class FechaSistema {

    private FechaSistema() {
    }

    public static LocalDateTime ahora() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
    }
}
