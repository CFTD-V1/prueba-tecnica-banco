package com.pruebatecnica.banco.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.pruebatecnica.banco.domain.model.Transaccion;

public interface TransaccionRepositoryPort {

    Transaccion guardar(Transaccion transaccion);

    Optional<Transaccion> buscarPorId(Long id);

    List<Transaccion> listarTodas();

    List<Transaccion> listarPorProductoId(Long productoId);

    boolean existePorProductoId(Long productoId);
}
