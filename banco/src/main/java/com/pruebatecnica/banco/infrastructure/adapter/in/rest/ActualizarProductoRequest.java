package com.pruebatecnica.banco.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.NotNull;

public record ActualizarProductoRequest(

        @NotNull(message = "La exencion del GMF es obligatoria") Boolean exentaGmf) {
}
