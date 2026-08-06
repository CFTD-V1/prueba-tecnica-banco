package com.pruebatecnica.banco.domain.model;

import com.pruebatecnica.banco.domain.exception.ExcepcionDeNegocio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

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
public class Producto {

    public static final int LONGITUD_NUMERO_CUENTA = 10;

    private static final int DIGITOS_ALEATORIOS = LONGITUD_NUMERO_CUENTA - 2;

    private Long id;
    private TipoCuenta tipoCuenta;
    private String numeroCuenta;
    private EstadoCuenta estado;
    private BigDecimal saldo;
    private BigDecimal saldoDisponible;
    private boolean exentaGmf;
    private Long clienteId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    /** Reglas que debe cumplir un producto para poder existir. */
    public void validar() {
        if (tipoCuenta == null) {
            throw new ExcepcionDeNegocio("El tipo de cuenta es obligatorio");
        }
        if (clienteId == null) {
            throw new ExcepcionDeNegocio("El producto debe estar vinculado a un cliente");
        }
        if (saldo == null) {
            throw new ExcepcionDeNegocio("El saldo es obligatorio");
        }
        validarSaldoDeAhorros();
    }

    /** Estado con el que nace una cuenta recien creada. */
    public void inicializar() {
        if (saldo == null) {
            saldo = BigDecimal.ZERO;
        }
        this.estado = EstadoCuenta.ACTIVA;
        this.saldoDisponible = this.saldo;
    }

    /** Genera un numero de cuenta de 10 digitos con el prefijo del tipo de cuenta. */
    public void asignarNumeroDeCuenta() {
        StringBuilder numero = new StringBuilder(tipoCuenta.getPrefijo());
        for (int i = 0; i < DIGITOS_ALEATORIOS; i++) {
            numero.append(ThreadLocalRandom.current().nextInt(10));
        }
        this.numeroCuenta = numero.toString();
    }

    public void activar() {
        validarNoCancelada();
        this.estado = EstadoCuenta.ACTIVA;
    }

    public void inactivar() {
        validarNoCancelada();
        this.estado = EstadoCuenta.INACTIVA;
    }

    public void cancelar() {
        if (saldo == null || saldo.compareTo(BigDecimal.ZERO) != 0) {
            throw new ExcepcionDeNegocio("Solo se puede cancelar una cuenta con saldo igual a $0");
        }
        this.estado = EstadoCuenta.CANCELADA;
    }

    public void cambiarEstado(EstadoCuenta nuevoEstado) {
        if (nuevoEstado == null) {
            throw new ExcepcionDeNegocio("El estado es obligatorio");
        }
        switch (nuevoEstado) {
            case ACTIVA -> activar();
            case INACTIVA -> inactivar();
            case CANCELADA -> cancelar();
        }
    }

    /** Suma un monto al saldo de la cuenta (consignaciones y transferencias recibidas). */
    public void acreditar(BigDecimal monto) {
        validarMonto(monto);
        validarOperable();
        this.saldo = this.saldo.add(monto);
        this.saldoDisponible = this.saldo;
    }

    /** Resta un monto del saldo de la cuenta (retiros y transferencias enviadas). */
    public void debitar(BigDecimal monto) {
        validarMonto(monto);
        validarOperable();
        this.saldo = this.saldo.subtract(monto);
        validarSaldoDeAhorros();
        this.saldoDisponible = this.saldo;
    }

    public boolean esDeAhorros() {
        return tipoCuenta == TipoCuenta.AHORROS;
    }

    private void validarSaldoDeAhorros() {
        if (esDeAhorros() && saldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new ExcepcionDeNegocio("Una cuenta de ahorros no puede tener un saldo menor a $0");
        }
    }

    private void validarNoCancelada() {
        if (estado == EstadoCuenta.CANCELADA) {
            throw new ExcepcionDeNegocio("Una cuenta cancelada no puede cambiar de estado");
        }
    }

    private void validarOperable() {
        if (estado != EstadoCuenta.ACTIVA) {
            throw new ExcepcionDeNegocio("Solo se pueden realizar movimientos sobre una cuenta activa");
        }
    }

    private void validarMonto(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ExcepcionDeNegocio("El monto del movimiento debe ser mayor que cero");
        }
    }
}
