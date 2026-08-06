package com.pruebatecnica.banco.domain.port.in;

import com.pruebatecnica.banco.domain.model.Transaccion;

import java.math.BigDecimal;
import java.util.List;

/**
 * Los movimientos no se actualizan ni se eliminan: un asiento contable es inmutable.
 * Por eso solo se exponen las operaciones de creacion y consulta.
 */
public interface TransaccionCasosDeUso {

    Transaccion consignar(Long productoId, BigDecimal monto, String descripcion);

    Transaccion retirar(Long productoId, BigDecimal monto, String descripcion);

    /** Devuelve los dos movimientos generados: el debito del origen y el credito del destino. */
    List<Transaccion> transferir(Long productoOrigenId, Long productoDestinoId, BigDecimal monto,
            String descripcion);

    Transaccion obtenerPorId(Long id);

    List<Transaccion> listar();

    /** Estado de cuenta: todos los movimientos de un producto. */
    List<Transaccion> listarPorProducto(Long productoId);
}
