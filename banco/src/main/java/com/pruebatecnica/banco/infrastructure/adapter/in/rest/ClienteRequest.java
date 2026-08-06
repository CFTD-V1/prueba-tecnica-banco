package com.pruebatecnica.banco.infrastructure.adapter.in.rest;

import java.time.LocalDate;

import com.pruebatecnica.banco.domain.model.TipoIdentificacion;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClienteRequest(

        @NotNull(message = "El tipo de identificacion es obligatorio") TipoIdentificacion tipoIdentificacion,

        @NotBlank(message = "El numero de identificacion es obligatorio") @Size(max = 20, message = "El numero de identificacion no puede superar 20 caracteres") String numeroIdentificacion,

        @NotBlank(message = "Los nombres son obligatorios") @Size(min = 2, max = 100, message = "Los nombres deben tener entre 2 y 100 caracteres") String nombres,

        @NotBlank(message = "El apellido es obligatorio") @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres") String apellido,

        @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no tiene un formato valido") @Size(max = 150, message = "El correo no puede superar 150 caracteres") String correo,

        @NotNull(message = "La fecha de nacimiento es obligatoria") LocalDate fechaNacimiento) {
}