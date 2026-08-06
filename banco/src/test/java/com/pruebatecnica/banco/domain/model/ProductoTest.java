package com.pruebatecnica.banco.domain.model;

import com.pruebatecnica.banco.domain.exception.ExcepcionDeNegocio;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductoTest {

    private Producto cuenta(TipoCuenta tipoCuenta, BigDecimal saldo) {
        Producto producto = Producto.builder()
                .tipoCuenta(tipoCuenta)
                .clienteId(1L)
                .saldo(saldo)
                .build();
        producto.inicializar();
        producto.asignarNumeroDeCuenta();
        return producto;
    }

    @Test
    @DisplayName("El numero de una cuenta de ahorros tiene 10 digitos e inicia en 53")
    void numeroDeCuentaDeAhorrosIniciaEn53() {
        Producto producto = cuenta(TipoCuenta.AHORROS, BigDecimal.ZERO);

        assertThat(producto.getNumeroCuenta()).hasSize(10);
        assertThat(producto.getNumeroCuenta()).startsWith("53");
        assertThat(producto.getNumeroCuenta()).containsOnlyDigits();
    }

    @Test
    @DisplayName("El numero de una cuenta corriente tiene 10 digitos e inicia en 33")
    void numeroDeCuentaCorrienteIniciaEn33() {
        Producto producto = cuenta(TipoCuenta.CORRIENTE, BigDecimal.ZERO);

        assertThat(producto.getNumeroCuenta()).hasSize(10);
        assertThat(producto.getNumeroCuenta()).startsWith("33");
        assertThat(producto.getNumeroCuenta()).containsOnlyDigits();
    }

    @Test
    @DisplayName("Una cuenta nace activa")
    void laCuentaNaceActiva() {
        Producto producto = cuenta(TipoCuenta.AHORROS, BigDecimal.ZERO);

        assertThat(producto.getEstado()).isEqualTo(EstadoCuenta.ACTIVA);
    }

    @Test
    @DisplayName("Una cuenta de ahorros no puede quedar con saldo negativo")
    void laCuentaDeAhorrosNoPuedeQuedarNegativa() {
        Producto producto = cuenta(TipoCuenta.AHORROS, new BigDecimal("50000"));

        assertThatThrownBy(() -> producto.debitar(new BigDecimal("60000")))
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("no puede tener un saldo menor a $0");
    }

    @Test
    @DisplayName("Una cuenta corriente si puede quedar en sobregiro")
    void laCuentaCorrienteAdmiteSobregiro() {
        Producto producto = cuenta(TipoCuenta.CORRIENTE, new BigDecimal("50000"));

        producto.debitar(new BigDecimal("60000"));

        assertThat(producto.getSaldo()).isEqualByComparingTo("-10000");
    }

    @Test
    @DisplayName("Solo se puede cancelar una cuenta con saldo cero")
    void soloSeCancelaConSaldoCero() {
        Producto conSaldo = cuenta(TipoCuenta.AHORROS, new BigDecimal("1000"));

        assertThatThrownBy(conSaldo::cancelar)
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("saldo igual a $0");

        Producto sinSaldo = cuenta(TipoCuenta.AHORROS, BigDecimal.ZERO);
        sinSaldo.cancelar();

        assertThat(sinSaldo.getEstado()).isEqualTo(EstadoCuenta.CANCELADA);
    }

    @Test
    @DisplayName("Una cuenta cancelada no puede volver a cambiar de estado")
    void laCuentaCanceladaNoCambiaDeEstado() {
        Producto producto = cuenta(TipoCuenta.AHORROS, BigDecimal.ZERO);
        producto.cancelar();

        assertThatThrownBy(producto::activar)
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("cancelada");
    }

    @Test
    @DisplayName("Las cuentas se pueden activar e inactivar en cualquier momento")
    void laCuentaSeActivaEInactiva() {
        Producto producto = cuenta(TipoCuenta.CORRIENTE, BigDecimal.ZERO);

        producto.inactivar();
        assertThat(producto.getEstado()).isEqualTo(EstadoCuenta.INACTIVA);

        producto.activar();
        assertThat(producto.getEstado()).isEqualTo(EstadoCuenta.ACTIVA);
    }

    @Test
    @DisplayName("No se pueden realizar movimientos sobre una cuenta inactiva")
    void noSeOperaSobreCuentaInactiva() {
        Producto producto = cuenta(TipoCuenta.AHORROS, new BigDecimal("1000"));
        producto.inactivar();

        assertThatThrownBy(() -> producto.acreditar(new BigDecimal("500")))
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("cuenta activa");
    }

    @Test
    @DisplayName("El monto de un movimiento debe ser mayor que cero")
    void elMontoDebeSerMayorQueCero() {
        Producto producto = cuenta(TipoCuenta.AHORROS, new BigDecimal("1000"));

        assertThatThrownBy(() -> producto.acreditar(BigDecimal.ZERO))
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("mayor que cero");
    }

    @Test
    @DisplayName("Acreditar suma al saldo y actualiza el saldo disponible")
    void acreditarSumaAlSaldo() {
        Producto producto = cuenta(TipoCuenta.AHORROS, new BigDecimal("1000"));

        producto.acreditar(new BigDecimal("500"));

        assertThat(producto.getSaldo()).isEqualByComparingTo("1500");
        assertThat(producto.getSaldoDisponible()).isEqualByComparingTo("1500");
    }

    @Test
    @DisplayName("Un producto sin cliente no es valido")
    void elProductoSinClienteNoEsValido() {
        Producto producto = Producto.builder()
                .tipoCuenta(TipoCuenta.AHORROS)
                .saldo(BigDecimal.ZERO)
                .build();

        assertThatThrownBy(producto::validar)
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("vinculado a un cliente");
    }
}
