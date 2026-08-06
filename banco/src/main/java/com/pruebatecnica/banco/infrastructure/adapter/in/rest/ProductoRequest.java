package com.pruebatecnica.banco.infrastructure.adapter.in.rest;

import java.math.BigDecimal;

import com.pruebatecnica.banco.domain.model.TipoCuenta;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * El saldo inicial y la exencion del GMF son opcionales: si no se envian, la cuenta
 * nace con saldo $0 y sin exencion. Por eso exentaGmf es Boolean y no boolean.
 */
public record ProductoRequest(

        @NotNull(message = "El tipo de cuenta es obligatorio") TipoCuenta tipoCuenta,

        @NotNull(message = "El id del cliente es obligatorio") Long clienteId,

        @PositiveOrZero(message = "El saldo inicial no puede ser negativo") BigDecimal saldoInicial,

        Boolean exentaGmf) {
}
