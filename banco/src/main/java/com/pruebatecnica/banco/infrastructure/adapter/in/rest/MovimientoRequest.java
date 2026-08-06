package com.pruebatecnica.banco.infrastructure.adapter.in.rest;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de una consignación o de un retiro.
 * El monto se limita a dos decimales porque es la precisión con la que se guarda
 * el dinero; aceptar más decimales obligaría a redondear en silencio.
 */
public record MovimientoRequest(

        @NotNull(message = "El id del producto es obligatorio") Long productoId,

        @NotNull(message = "El monto es obligatorio") @Positive(message = "El monto debe ser mayor que cero") @Digits(integer = 15, fraction = 2, message = "El monto admite máximo 2 decimales") BigDecimal monto,

        @Size(max = 200, message = "La descripción no puede superar 200 caracteres") String descripcion) {
}
