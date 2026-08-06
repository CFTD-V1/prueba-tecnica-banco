package com.pruebatecnica.banco.application.service;

import com.pruebatecnica.banco.domain.exception.ExcepcionDeNegocio;
import com.pruebatecnica.banco.domain.model.Cliente;
import com.pruebatecnica.banco.domain.port.in.ClienteCasosDeUso;
import com.pruebatecnica.banco.domain.port.out.ClienteRepositoryPort;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService implements ClienteCasosDeUso {

    private final ClienteRepositoryPort clienteRepositoryPort;

    public ClienteService(ClienteRepositoryPort clienteRepositoryPort) {
        this.clienteRepositoryPort = clienteRepositoryPort;
    }

    @Override
    @Transactional
    public Cliente crear(Cliente cliente) {
        cliente.validar();

        if (clienteRepositoryPort.existePorNumeroIdentificacion(cliente.getNumeroIdentificacion())) {
            throw new ExcepcionDeNegocio(
                    "Ya existe un cliente con el número de identificación " + cliente.getNumeroIdentificacion());
        }

        cliente.setId(null);
        cliente.setFechaCreacion(LocalDateTime.now());
        cliente.setFechaModificacion(LocalDateTime.now());

        return clienteRepositoryPort.guardar(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente obtenerPorId(Long id) {
        return clienteRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new ExcepcionDeNegocio("No existe un cliente con el id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listar() {
        return clienteRepositoryPort.listarTodos();
    }

    @Override
    @Transactional
    public Cliente actualizar(Long id, Cliente cliente) {
        Cliente existente = obtenerPorId(id);

        boolean cambioIdentificacion = !existente.getNumeroIdentificacion()
                .equals(cliente.getNumeroIdentificacion());

        if (cambioIdentificacion
                && clienteRepositoryPort.existePorNumeroIdentificacion(cliente.getNumeroIdentificacion())) {
            throw new ExcepcionDeNegocio(
                    "Ya existe un cliente con el número de identificación " + cliente.getNumeroIdentificacion());
        }

        existente.setTipoIdentificacion(cliente.getTipoIdentificacion());
        existente.setNumeroIdentificacion(cliente.getNumeroIdentificacion());
        existente.setNombres(cliente.getNombres());
        existente.setApellido(cliente.getApellido());
        existente.setCorreo(cliente.getCorreo());
        existente.setFechaNacimiento(cliente.getFechaNacimiento());

        existente.validar();
        existente.setFechaModificacion(LocalDateTime.now());

        return clienteRepositoryPort.guardar(existente);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Cliente cliente = obtenerPorId(id);

        // TODO Fase 3: impedir la eliminación si el cliente tiene productos asociados
        clienteRepositoryPort.eliminarPorId(cliente.getId());
    }
}