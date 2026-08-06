package com.pruebatecnica.banco.domain.model;

/**
 * Indica si el movimiento suma o resta sobre la cuenta.
 * Una transferencia genera dos movimientos: un DEBITO en la cuenta de envio
 * y un CREDITO en la cuenta de recepcion.
 */
public enum NaturalezaMovimiento {
    CREDITO,
    DEBITO
}
