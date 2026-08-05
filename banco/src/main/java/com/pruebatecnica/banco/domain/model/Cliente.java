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
    private static final String REGEX_CORREO = "^[\\w.+-]+@[\\w-]+\\.[\\w.]+$";

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
        validarMayoriaDeEdad();
        validarCorreo();
        validarLongitud(nombres, "nombres");
        validarLongitud(apellido, "apellido");
    }

    private void validarMayoriaDeEdad() {
        if (fechaNacimiento == null) {
            throw new ExcepcionDeNegocio("La fecha de nacimiento es obligatoria");
        }
        int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
        if (edad < EDAD_MINIMA) {
            throw new ExcepcionDeNegocio("El cliente debe ser mayor de edad");
        }
    }

    private void validarCorreo() {
        if (correo == null || !correo.matches(REGEX_CORREO)) {
            throw new ExcepcionDeNegocio("El correo no tiene un formato válido");
        }
    }

    private void validarLongitud(String valor, String campo) {
        if (valor == null || valor.trim().length() < 2) {
            throw new ExcepcionDeNegocio("El campo " + campo + " debe tener al menos 2 caracteres");
        }
    }
}