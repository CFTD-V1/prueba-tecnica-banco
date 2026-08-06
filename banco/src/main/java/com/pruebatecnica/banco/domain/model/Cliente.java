package com.pruebatecnica.banco.domain.model;

import com.pruebatecnica.banco.domain.exception.ExcepcionDeNegocio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    private static final int EDAD_MINIMA = 18;
    private static final int EDAD_MAXIMA = 120;
    private static final int LONGITUD_MINIMA_NOMBRE = 2;
    private static final int LONGITUD_MINIMA_IDENTIFICACION = 5;

    private static final String REGEX_CORREO = "^[\\w.+-]+@[\\w-]+\\.[\\w.]+$";

    /**
     * Letras (incluidas tildes y eñes), espacios, apóstrofes, guiones y puntos.
     * El prefijo (?U) hace que \p{L} reconozca letras de cualquier idioma.
     * Debe empezar por letra, para que "123" o "-Juan" no pasen.
     */
    private static final String REGEX_NOMBRE = "(?U)^\\p{L}[\\p{L} .'\\-]*$";

    /** Cédulas de ciudadanía y de extranjería: solo dígitos. */
    private static final String REGEX_IDENTIFICACION_NUMERICA = "^\\d+$";

    /** Los pasaportes admiten letras y números, pero no espacios ni símbolos. */
    private static final String REGEX_PASAPORTE = "^[A-Za-z0-9]+$";

    private Long id;
    private TipoIdentificacion tipoIdentificacion;
    private String numeroIdentificacion;
    private String nombres;
    private String apellido;
    private String correo;
    private LocalDate fechaNacimiento;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    public void validar() {
        validarEdad();
        validarCorreo();
        validarNombre(nombres, "nombres");
        validarNombre(apellido, "apellido");
        validarNumeroIdentificacion();
    }

    private void validarEdad() {
        if (fechaNacimiento == null) {
            throw new ExcepcionDeNegocio("La fecha de nacimiento es obligatoria");
        }
        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new ExcepcionDeNegocio("La fecha de nacimiento no puede ser futura");
        }

        int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
        if (edad < EDAD_MINIMA) {
            throw new ExcepcionDeNegocio("El cliente debe ser mayor de edad");
        }
        if (edad > EDAD_MAXIMA) {
            throw new ExcepcionDeNegocio(
                    "La fecha de nacimiento no es válida: supera los " + EDAD_MAXIMA + " años");
        }
    }

    private void validarCorreo() {
        if (correo == null || !correo.matches(REGEX_CORREO)) {
            throw new ExcepcionDeNegocio("El correo no tiene un formato válido");
        }
    }

    private void validarNombre(String valor, String campo) {
        if (valor == null || valor.trim().length() < LONGITUD_MINIMA_NOMBRE) {
            throw new ExcepcionDeNegocio(
                    "El campo " + campo + " debe tener al menos " + LONGITUD_MINIMA_NOMBRE + " caracteres");
        }
        if (!valor.trim().matches(REGEX_NOMBRE)) {
            throw new ExcepcionDeNegocio(
                    "El campo " + campo + " solo puede contener letras, espacios, apóstrofes y guiones");
        }
    }

    /**
     * El formato depende del tipo de documento: las cédulas son numéricas y los
     * pasaportes admiten letras.
     */
    private void validarNumeroIdentificacion() {
        if (tipoIdentificacion == null) {
            throw new ExcepcionDeNegocio("El tipo de identificación es obligatorio");
        }
        if (numeroIdentificacion == null || numeroIdentificacion.isBlank()) {
            throw new ExcepcionDeNegocio("El número de identificación es obligatorio");
        }

        String numero = numeroIdentificacion.trim();

        if (numero.length() < LONGITUD_MINIMA_IDENTIFICACION) {
            throw new ExcepcionDeNegocio("El número de identificación debe tener al menos "
                    + LONGITUD_MINIMA_IDENTIFICACION + " caracteres");
        }

        if (tipoIdentificacion == TipoIdentificacion.PASAPORTE) {
            if (!numero.matches(REGEX_PASAPORTE)) {
                throw new ExcepcionDeNegocio(
                        "El número de pasaporte solo puede contener letras y números");
            }
            return;
        }

        if (!numero.matches(REGEX_IDENTIFICACION_NUMERICA)) {
            throw new ExcepcionDeNegocio(
                    "El número de identificación de una cédula solo puede contener dígitos");
        }
    }
}
