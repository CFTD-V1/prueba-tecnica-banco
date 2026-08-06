package com.pruebatecnica.banco.infrastructure.adapter.in.rest;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.pruebatecnica.banco.domain.model.TipoIdentificacion;

public record ClienteResponse(
        Long id,
        TipoIdentificacion tipoIdentificacion,
        String numeroIdentificacion,
        String nombres,
        String apellido,
        String correo,
        LocalDate fechaNacimiento,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion) {
}
