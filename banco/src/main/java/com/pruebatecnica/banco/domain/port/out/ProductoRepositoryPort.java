package com.pruebatecnica.banco.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.pruebatecnica.banco.domain.model.Producto;

public interface ProductoRepositoryPort {

    Producto guardar(Producto producto);

    Optional<Producto> buscarPorId(Long id);

    Optional<Producto> buscarPorNumeroCuenta(String numeroCuenta);

    List<Producto> listarTodos();

    List<Producto> listarPorClienteId(Long clienteId);

    void eliminarPorId(Long id);

    boolean existePorNumeroCuenta(String numeroCuenta);

    boolean existePorClienteId(Long clienteId);
}
