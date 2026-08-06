package com.pruebatecnica.banco.application.service;

import com.pruebatecnica.banco.domain.exception.ExcepcionDeNegocio;
import com.pruebatecnica.banco.domain.exception.ExcepcionDeRecursoNoEncontrado;
import com.pruebatecnica.banco.domain.model.EstadoCuenta;
import com.pruebatecnica.banco.domain.model.Producto;
import com.pruebatecnica.banco.domain.port.in.ProductoCasosDeUso;
import com.pruebatecnica.banco.domain.port.out.ClienteRepositoryPort;
import com.pruebatecnica.banco.domain.port.out.ProductoRepositoryPort;
import com.pruebatecnica.banco.domain.port.out.TransaccionRepositoryPort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService implements ProductoCasosDeUso {

    private static final int INTENTOS_NUMERO_CUENTA = 5;

    private final ProductoRepositoryPort productoRepositoryPort;
    private final ClienteRepositoryPort clienteRepositoryPort;
    private final TransaccionRepositoryPort transaccionRepositoryPort;

    public ProductoService(ProductoRepositoryPort productoRepositoryPort,
            ClienteRepositoryPort clienteRepositoryPort,
            TransaccionRepositoryPort transaccionRepositoryPort) {
        this.productoRepositoryPort = productoRepositoryPort;
        this.clienteRepositoryPort = clienteRepositoryPort;
        this.transaccionRepositoryPort = transaccionRepositoryPort;
    }

    @Override
    @Transactional
    public Producto crear(Producto producto) {
        validarQueElClienteExista(producto.getClienteId());

        producto.setId(null);
        producto.inicializar();
        producto.validar();
        asignarNumeroDeCuentaUnico(producto);

        producto.setFechaCreacion(LocalDateTime.now());
        producto.setFechaModificacion(LocalDateTime.now());

        return productoRepositoryPort.guardar(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public Producto obtenerPorId(Long id) {
        return productoRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new ExcepcionDeRecursoNoEncontrado("No existe un producto con el id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listar() {
        return productoRepositoryPort.listarTodos();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarPorCliente(Long clienteId) {
        validarQueElClienteExista(clienteId);
        return productoRepositoryPort.listarPorClienteId(clienteId);
    }

    /**
     * Solo se permite modificar la exención del GMF: el tipo, el número de cuenta y
     * el saldo son inmutables desde el CRUD (el saldo solo cambia con transacciones).
     */
    @Override
    @Transactional
    public Producto actualizar(Long id, Producto producto) {
        Producto existente = obtenerPorId(id);

        existente.setExentaGmf(producto.isExentaGmf());
        existente.validar();
        existente.setFechaModificacion(LocalDateTime.now());

        return productoRepositoryPort.guardar(existente);
    }

    @Override
    @Transactional
    public Producto cambiarEstado(Long id, EstadoCuenta estado) {
        Producto existente = obtenerPorId(id);

        existente.cambiarEstado(estado);
        existente.setFechaModificacion(LocalDateTime.now());

        return productoRepositoryPort.guardar(existente);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Producto existente = obtenerPorId(id);

        if (existente.getSaldo().compareTo(BigDecimal.ZERO) != 0) {
            throw new ExcepcionDeNegocio("No se puede eliminar un producto que tiene saldo");
        }
        if (transaccionRepositoryPort.existePorProductoId(existente.getId())) {
            throw new ExcepcionDeNegocio(
                    "No se puede eliminar un producto que tiene movimientos registrados");
        }

        productoRepositoryPort.eliminarPorId(existente.getId());
    }

    private void validarQueElClienteExista(Long clienteId) {
        if (clienteId == null) {
            throw new ExcepcionDeNegocio("El producto debe estar vinculado a un cliente");
        }
        if (clienteRepositoryPort.buscarPorId(clienteId).isEmpty()) {
            throw new ExcepcionDeRecursoNoEncontrado("No existe un cliente con el id " + clienteId);
        }
    }

    /**
     * El número de cuenta se genera en el dominio; aquí se confirma contra la base de
     * datos que no exista otro igual y se reintenta un número acotado de veces.
     */
    private void asignarNumeroDeCuentaUnico(Producto producto) {
        for (int intento = 0; intento < INTENTOS_NUMERO_CUENTA; intento++) {
            producto.asignarNumeroDeCuenta();
            if (!productoRepositoryPort.existePorNumeroCuenta(producto.getNumeroCuenta())) {
                return;
            }
        }
        throw new ExcepcionDeNegocio("No fue posible generar un número de cuenta único, intente nuevamente");
    }
}
