package com.pruebatecnica.banco.infrastructure.adapter.in.rest;

import com.pruebatecnica.banco.domain.model.EstadoCuenta;

import jakarta.validation.constraints.NotNull;

public record CambiarEstadoRequest(

        @NotNull(message = "El estado es obligatorio") EstadoCuenta estado) {
}
