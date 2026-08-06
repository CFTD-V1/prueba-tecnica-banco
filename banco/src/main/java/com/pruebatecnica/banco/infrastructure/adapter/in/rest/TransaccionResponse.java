package com.pruebatecnica.banco.infrastructure.adapter.in.rest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.pruebatecnica.banco.domain.model.NaturalezaMovimiento;
import com.pruebatecnica.banco.domain.model.TipoTransaccion;

public record TransaccionResponse(
        Long id,
        TipoTransaccion tipo,
        NaturalezaMovimiento naturaleza,
        Long productoId,
        Long productoRelacionadoId,
        BigDecimal monto,
        BigDecimal saldoResultante,
        String referencia,
        String descripcion,
        LocalDateTime fecha) {
}
