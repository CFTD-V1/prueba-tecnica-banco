package com.pruebatecnica.banco.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.pruebatecnica.banco.domain.model.Cliente;

public interface ClienteRepositoryPort {

    Cliente guardar(Cliente cliente);

    Optional<Cliente> buscarPorId(Long id);

    List<Cliente> listarTodos();

    void eliminarPorId(Long id);

    boolean existePorNumeroIdentificacion(String numeroIdentificacion);
}