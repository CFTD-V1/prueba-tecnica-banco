package com.pruebatecnica.banco.infrastructure.adapter.in.rest;

import java.math.BigDecimal;

import com.pruebatecnica.banco.domain.model.TipoCuenta;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductoRequest(

        @NotNull(message = "El tipo de cuenta es obligatorio") TipoCuenta tipoCuenta,

        @NotNull(message = "El id del cliente es obligatorio") Long clienteId,

        @PositiveOrZero(message = "El saldo inicial no puede ser negativo") BigDecimal saldoInicial,

        boolean exentaGmf) {
}
