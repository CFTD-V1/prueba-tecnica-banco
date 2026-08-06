package com.pruebatecnica.banco.application.service;

import com.pruebatecnica.banco.domain.exception.ExcepcionDeNegocio;
import com.pruebatecnica.banco.domain.exception.ExcepcionDeRecursoNoEncontrado;
import com.pruebatecnica.banco.domain.model.FechaSistema;
import com.pruebatecnica.banco.domain.model.NaturalezaMovimiento;
import com.pruebatecnica.banco.domain.model.Producto;
import com.pruebatecnica.banco.domain.model.TipoTransaccion;
import com.pruebatecnica.banco.domain.model.Transaccion;
import com.pruebatecnica.banco.domain.port.in.TransaccionCasosDeUso;
import com.pruebatecnica.banco.domain.port.out.ProductoRepositoryPort;
import com.pruebatecnica.banco.domain.port.out.TransaccionRepositoryPort;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransaccionService implements TransaccionCasosDeUso {

    private final TransaccionRepositoryPort transaccionRepositoryPort;
    private final ProductoRepositoryPort productoRepositoryPort;

    public TransaccionService(TransaccionRepositoryPort transaccionRepositoryPort,
            ProductoRepositoryPort productoRepositoryPort) {
        this.transaccionRepositoryPort = transaccionRepositoryPort;
        this.productoRepositoryPort = productoRepositoryPort;
    }

    @Override
    @Transactional
    public Transaccion consignar(Long productoId, BigDecimal monto, String descripcion) {
        Producto producto = productoBloqueado(productoId);

        producto.acreditar(monto);
        productoRepositoryPort.guardar(producto);

        return registrarMovimiento(TipoTransaccion.CONSIGNACION, NaturalezaMovimiento.CREDITO,
                producto, monto, descripcion, nuevaReferencia(), null);
    }

    @Override
    @Transactional
    public Transaccion retirar(Long productoId, BigDecimal monto, String descripcion) {
        Producto producto = productoBloqueado(productoId);

        producto.debitar(monto);
        productoRepositoryPort.guardar(producto);

        return registrarMovimiento(TipoTransaccion.RETIRO, NaturalezaMovimiento.DEBITO,
                producto, monto, descripcion, nuevaReferencia(), null);
    }

    /**
     * Una transferencia es una sola unidad de trabajo: o se aplican los dos movimientos
     * o no se aplica ninguno. Al estar todo dentro de la misma transaccion, si el debito
     * falla (por ejemplo, saldo insuficiente en una cuenta de ahorros) se revierte el
     * credito automaticamente.
     */
    @Override
    @Transactional
    public List<Transaccion> transferir(Long productoOrigenId, Long productoDestinoId,
            BigDecimal monto, String descripcion) {

        if (productoOrigenId == null || productoDestinoId == null) {
            throw new ExcepcionDeNegocio("Se deben indicar la cuenta de origen y la de destino");
        }
        if (productoOrigenId.equals(productoDestinoId)) {
            throw new ExcepcionDeNegocio("La cuenta de origen y la de destino deben ser distintas");
        }

        Producto origen;
        Producto destino;
        // Se bloquean siempre en el mismo orden (por id) para que dos transferencias
        // cruzadas entre las mismas cuentas no queden en interbloqueo.
        if (productoOrigenId < productoDestinoId) {
            origen = productoBloqueado(productoOrigenId);
            destino = productoBloqueado(productoDestinoId);
        } else {
            destino = productoBloqueado(productoDestinoId);
            origen = productoBloqueado(productoOrigenId);
        }

        origen.debitar(monto);
        destino.acreditar(monto);
        productoRepositoryPort.guardar(origen);
        productoRepositoryPort.guardar(destino);

        String referencia = nuevaReferencia();
        Transaccion debito = registrarMovimiento(TipoTransaccion.TRANSFERENCIA, NaturalezaMovimiento.DEBITO,
                origen, monto, descripcion, referencia, destino.getId());
        Transaccion credito = registrarMovimiento(TipoTransaccion.TRANSFERENCIA, NaturalezaMovimiento.CREDITO,
                destino, monto, descripcion, referencia, origen.getId());

        return List.of(debito, credito);
    }

    @Override
    @Transactional(readOnly = true)
    public Transaccion obtenerPorId(Long id) {
        return transaccionRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new ExcepcionDeRecursoNoEncontrado("No existe una transacción con el id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaccion> listar() {
        return transaccionRepositoryPort.listarTodas();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaccion> listarPorProducto(Long productoId) {
        if (productoRepositoryPort.buscarPorId(productoId).isEmpty()) {
            throw new ExcepcionDeRecursoNoEncontrado("No existe un producto con el id " + productoId);
        }
        return transaccionRepositoryPort.listarPorProductoId(productoId);
    }

    private Producto productoBloqueado(Long productoId) {
        if (productoId == null) {
            throw new ExcepcionDeNegocio("El movimiento debe estar asociado a un producto");
        }
        return productoRepositoryPort.buscarPorIdConBloqueo(productoId)
                .orElseThrow(() -> new ExcepcionDeRecursoNoEncontrado(
                        "No existe un producto con el id " + productoId));
    }

    private Transaccion registrarMovimiento(TipoTransaccion tipo, NaturalezaMovimiento naturaleza,
            Producto producto, BigDecimal monto, String descripcion, String referencia,
            Long productoRelacionadoId) {

        Transaccion movimiento = Transaccion.builder()
                .tipo(tipo)
                .naturaleza(naturaleza)
                .productoId(producto.getId())
                .productoRelacionadoId(productoRelacionadoId)
                .monto(monto)
                .saldoResultante(producto.getSaldo())
                .referencia(referencia)
                .descripcion(descripcion)
                .fecha(FechaSistema.ahora())
                .build();

        movimiento.validar();

        return transaccionRepositoryPort.guardar(movimiento);
    }

    private String nuevaReferencia() {
        return UUID.randomUUID().toString();
    }
}
