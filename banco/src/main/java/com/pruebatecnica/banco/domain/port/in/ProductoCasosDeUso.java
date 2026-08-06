package com.pruebatecnica.banco.domain.port.in;

import com.pruebatecnica.banco.domain.model.EstadoCuenta;
import com.pruebatecnica.banco.domain.model.Producto;

import java.util.List;

public interface ProductoCasosDeUso {

    Producto crear(Producto producto);

    Producto obtenerPorId(Long id);

    List<Producto> listar();

    List<Producto> listarPorCliente(Long clienteId);

    Producto actualizar(Long id, Producto producto);

    Producto cambiarEstado(Long id, EstadoCuenta estado);

    void eliminar(Long id);
}
