package com.pruebatecnica.banco.infrastructure.adapter.in.rest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.pruebatecnica.banco.domain.model.EstadoCuenta;
import com.pruebatecnica.banco.domain.model.TipoCuenta;

public record ProductoResponse(
        Long id,
        TipoCuenta tipoCuenta,
        String numeroCuenta,
        EstadoCuenta estado,
        BigDecimal saldo,
        BigDecimal saldoDisponible,
        boolean exentaGmf,
        Long clienteId,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion) {
}
