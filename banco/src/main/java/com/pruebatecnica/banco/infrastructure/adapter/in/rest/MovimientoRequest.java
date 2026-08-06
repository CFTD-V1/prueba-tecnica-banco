package com.pruebatecnica.banco.infrastructure.adapter.in.rest;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Cuerpo de una consignación o de un retiro. */
public record MovimientoRequest(

        @NotNull(message = "El id del producto es obligatorio") Long productoId,

        @NotNull(message = "El monto es obligatorio") @Positive(message = "El monto debe ser mayor que cero") BigDecimal monto,

        @Size(max = 200, message = "La descripción no puede superar 200 caracteres") String descripcion) {
}
