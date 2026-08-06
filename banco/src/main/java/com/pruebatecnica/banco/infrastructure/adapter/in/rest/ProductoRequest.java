package com.pruebatecnica.banco.infrastructure.adapter.in.rest;

import com.pruebatecnica.banco.domain.model.TipoCuenta;

import jakarta.validation.constraints.NotNull;

/**
 * La cuenta se abre siempre con saldo $0: el saldo solo se mueve mediante
 * transacciones, de modo que cualquier saldo queda respaldado por un movimiento en
 * el estado de cuenta. La exención del GMF es opcional; si no se envía, la cuenta
 * nace sin exención (por eso es Boolean y no boolean).
 */
public record ProductoRequest(

        @NotNull(message = "El tipo de cuenta es obligatorio") TipoCuenta tipoCuenta,

        @NotNull(message = "El id del cliente es obligatorio") Long clienteId,

        Boolean exentaGmf) {
}
