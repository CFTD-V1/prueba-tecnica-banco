package com.pruebatecnica.banco.domain.port.in;

import com.pruebatecnica.banco.domain.model.Cliente;

import java.util.List;

public interface ClienteCasosDeUso {

    Cliente crear(Cliente cliente);

    Cliente obtenerPorId(Long id);

    List<Cliente> listar();

    Cliente actualizar(Long id, Cliente cliente);

    void eliminar(Long id);
}