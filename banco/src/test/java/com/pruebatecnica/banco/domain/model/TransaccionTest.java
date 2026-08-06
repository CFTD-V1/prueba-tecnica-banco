package com.pruebatecnica.banco.domain.model;

import com.pruebatecnica.banco.domain.exception.ExcepcionDeNegocio;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransaccionTest {

    private Transaccion.TransaccionBuilder consignacionValida() {
        return Transaccion.builder()
                .tipo(TipoTransaccion.CONSIGNACION)
                .naturaleza(NaturalezaMovimiento.CREDITO)
                .productoId(1L)
                .monto(new BigDecimal("50000"));
    }

    @Test
    @DisplayName("Una consignacion con datos completos es valida")
    void laConsignacionEsValida() {
        assertThatCode(() -> consignacionValida().build().validar()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("El monto debe ser mayor que cero")
    void elMontoDebeSerMayorQueCero() {
        Transaccion sinMonto = consignacionValida().monto(BigDecimal.ZERO).build();

        assertThatThrownBy(sinMonto::validar)
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("mayor que cero");
    }

    @Test
    @DisplayName("El movimiento debe estar asociado a un producto")
    void elMovimientoNecesitaProducto() {
        Transaccion sinProducto = consignacionValida().productoId(null).build();

        assertThatThrownBy(sinProducto::validar)
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("asociado a un producto");
    }

    @Test
    @DisplayName("Una transferencia sin cuenta contraparte no es valida")
    void laTransferenciaNecesitaContraparte() {
        Transaccion transferencia = consignacionValida()
                .tipo(TipoTransaccion.TRANSFERENCIA)
                .naturaleza(NaturalezaMovimiento.DEBITO)
                .build();

        assertThatThrownBy(transferencia::validar)
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("contraparte");
    }
}
