package com.pruebatecnica.banco.domain.model;

import com.pruebatecnica.banco.domain.exception.ExcepcionDeNegocio;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClienteTest {

    private Cliente.ClienteBuilder clienteValido() {
        return Cliente.builder()
                .tipoIdentificacion(TipoIdentificacion.CC)
                .numeroIdentificacion("1020304050")
                .nombres("Laura")
                .apellido("Gómez")
                .correo("laura.gomez@banco.com")
                .fechaNacimiento(LocalDate.of(1992, 7, 8));
    }

    @Test
    @DisplayName("Un cliente con datos correctos es valido")
    void elClienteValidoPasa() {
        assertThatCode(() -> clienteValido().build().validar()).doesNotThrowAnyException();
    }

    // --- Numero de identificacion ------------------------------------------

    @Test
    @DisplayName("La cedula no admite letras")
    void laCedulaNoAdmiteLetras() {
        Cliente cliente = clienteValido().numeroIdentificacion("ABC12345").build();

        assertThatThrownBy(cliente::validar)
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("solo puede contener dígitos");
    }

    @Test
    @DisplayName("La cedula no admite simbolos ni espacios")
    void laCedulaNoAdmiteSimbolos() {
        assertThatThrownBy(() -> clienteValido().numeroIdentificacion("123-456").build().validar())
                .isInstanceOf(ExcepcionDeNegocio.class);

        assertThatThrownBy(() -> clienteValido().numeroIdentificacion("123 456").build().validar())
                .isInstanceOf(ExcepcionDeNegocio.class);
    }

    @Test
    @DisplayName("El pasaporte si admite letras y numeros")
    void elPasaporteAdmiteLetras() {
        Cliente cliente = clienteValido()
                .tipoIdentificacion(TipoIdentificacion.PASAPORTE)
                .numeroIdentificacion("AB123456")
                .build();

        assertThatCode(cliente::validar).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("El pasaporte no admite simbolos")
    void elPasaporteNoAdmiteSimbolos() {
        Cliente cliente = clienteValido()
                .tipoIdentificacion(TipoIdentificacion.PASAPORTE)
                .numeroIdentificacion("AB-123456")
                .build();

        assertThatThrownBy(cliente::validar)
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("letras y números");
    }

    @Test
    @DisplayName("La identificacion debe tener una longitud minima")
    void laIdentificacionTieneLongitudMinima() {
        assertThatThrownBy(() -> clienteValido().numeroIdentificacion("123").build().validar())
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("al menos 5 caracteres");
    }

    // --- Nombres y apellido ------------------------------------------------

    @Test
    @DisplayName("Los nombres no admiten numeros")
    void losNombresNoAdmitenNumeros() {
        assertThatThrownBy(() -> clienteValido().nombres("Juan123").build().validar())
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("solo puede contener letras");
    }

    @Test
    @DisplayName("Los nombres no admiten simbolos")
    void losNombresNoAdmitenSimbolos() {
        assertThatThrownBy(() -> clienteValido().nombres("Juan@#$").build().validar())
                .isInstanceOf(ExcepcionDeNegocio.class);
    }

    @Test
    @DisplayName("El apellido no admite numeros")
    void elApellidoNoAdmiteNumeros() {
        assertThatThrownBy(() -> clienteValido().apellido("Perez99").build().validar())
                .isInstanceOf(ExcepcionDeNegocio.class);
    }

    @Test
    @DisplayName("Se aceptan tildes, enies, apostrofes y guiones en los nombres")
    void seAceptanNombresRealesConCaracteresEspeciales() {
        assertThatCode(() -> clienteValido().nombres("María José").build().validar())
                .doesNotThrowAnyException();
        assertThatCode(() -> clienteValido().apellido("Muñoz Núñez").build().validar())
                .doesNotThrowAnyException();
        assertThatCode(() -> clienteValido().apellido("D'Angelo").build().validar())
                .doesNotThrowAnyException();
        assertThatCode(() -> clienteValido().apellido("Pérez-Gómez").build().validar())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("El nombre no puede empezar por un caracter que no sea letra")
    void elNombreEmpiezaPorLetra() {
        assertThatThrownBy(() -> clienteValido().nombres("-Juan").build().validar())
                .isInstanceOf(ExcepcionDeNegocio.class);
    }

    // --- Fecha de nacimiento -----------------------------------------------

    @Test
    @DisplayName("La fecha de nacimiento no puede ser futura")
    void laFechaDeNacimientoNoPuedeSerFutura() {
        Cliente cliente = clienteValido().fechaNacimiento(LocalDate.now().plusDays(1)).build();

        assertThatThrownBy(cliente::validar)
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("no puede ser futura");
    }

    @Test
    @DisplayName("Se rechaza una edad imposible")
    void seRechazaUnaEdadImposible() {
        Cliente cliente = clienteValido().fechaNacimiento(LocalDate.of(1850, 1, 1)).build();

        assertThatThrownBy(cliente::validar)
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("120 años");
    }

    @Test
    @DisplayName("Se rechaza a un menor de edad")
    void seRechazaAUnMenorDeEdad() {
        Cliente cliente = clienteValido().fechaNacimiento(LocalDate.now().minusYears(10)).build();

        assertThatThrownBy(cliente::validar)
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("mayor de edad");
    }

    @Test
    @DisplayName("El correo debe tener el formato xxxx@xxxxx.xxx")
    void elCorreoDebeTenerFormatoValido() {
        assertThatThrownBy(() -> clienteValido().correo("ana@banco").build().validar())
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("correo");
    }
}
