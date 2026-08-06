package com.pruebatecnica.banco.infrastructure.adapter.in.rest;

import java.time.LocalDate;

import com.pruebatecnica.banco.domain.model.TipoIdentificacion;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Validaciones de formato de la petición. Las reglas que dependen del negocio
 * (la mayoría de edad, o que una cédula solo admita dígitos mientras un pasaporte
 * admite letras) viven en el modelo de dominio.
 */
public record ClienteRequest(

        @NotNull(message = "El tipo de identificación es obligatorio") TipoIdentificacion tipoIdentificacion,

        @NotBlank(message = "El número de identificación es obligatorio") @Size(min = 5, max = 20, message = "El número de identificación debe tener entre 5 y 20 caracteres") @Pattern(regexp = "^[A-Za-z0-9]+$", message = "El número de identificación solo puede contener letras y números, sin espacios ni símbolos") String numeroIdentificacion,

        @NotBlank(message = "Los nombres son obligatorios") @Size(min = 2, max = 100, message = "Los nombres deben tener entre 2 y 100 caracteres") @Pattern(regexp = "(?U)^\\p{L}[\\p{L} .'\\-]*$", message = "Los nombres solo pueden contener letras, espacios, apóstrofes y guiones") String nombres,

        @NotBlank(message = "El apellido es obligatorio") @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres") @Pattern(regexp = "(?U)^\\p{L}[\\p{L} .'\\-]*$", message = "El apellido solo puede contener letras, espacios, apóstrofes y guiones") String apellido,

        @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no tiene un formato válido") @Size(max = 150, message = "El correo no puede superar 150 caracteres") String correo,

        @NotNull(message = "La fecha de nacimiento es obligatoria") @Past(message = "La fecha de nacimiento debe ser anterior a hoy") LocalDate fechaNacimiento) {
}
