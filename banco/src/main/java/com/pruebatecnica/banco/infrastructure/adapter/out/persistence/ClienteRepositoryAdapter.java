package com.pruebatecnica.banco.infrastructure.adapter.out.persistence;

import com.pruebatecnica.banco.domain.model.Cliente;
import com.pruebatecnica.banco.domain.port.out.ClienteRepositoryPort;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public class ClienteRepositoryAdapter implements ClienteRepositoryPort {

    private final ClienteJpaRepository clienteJpaRepository;

    public ClienteRepositoryAdapter(ClienteJpaRepository clienteJpaRepository) {
        this.clienteJpaRepository = clienteJpaRepository;
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        ClienteEntity entidad = aEntidad(cliente);
        return aDominio(clienteJpaRepository.save(entidad));
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        return clienteJpaRepository.findById(id).map(this::aDominio);
    }

    @Override
    public List<Cliente> listarTodos() {
        return clienteJpaRepository.findAll()
                .stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public void eliminarPorId(Long id) {
        clienteJpaRepository.deleteById(id);
    }

    @Override
    public boolean existePorNumeroIdentificacion(String numeroIdentificacion) {
        return clienteJpaRepository.existsByNumeroIdentificacion(numeroIdentificacion);
    }

    private ClienteEntity aEntidad(Cliente cliente) {
        return ClienteEntity.builder()
                .id(cliente.getId())
                .tipoIdentificacion(cliente.getTipoIdentificacion())
                .numeroIdentificacion(cliente.getNumeroIdentificacion())
                .nombres(cliente.getNombres())
                .apellido(cliente.getApellido())
                .correo(cliente.getCorreo())
                .fechaNacimiento(cliente.getFechaNacimiento())
                .fechaCreacion(cliente.getFechaCreacion())
                .fechaModificacion(cliente.getFechaModificacion())
                .build();
    }

    private Cliente aDominio(ClienteEntity entidad) {
        return Cliente.builder()
                .id(entidad.getId())
                .tipoIdentificacion(entidad.getTipoIdentificacion())
                .numeroIdentificacion(entidad.getNumeroIdentificacion())
                .nombres(entidad.getNombres())
                .apellido(entidad.getApellido())
                .correo(entidad.getCorreo())
                .fechaNacimiento(entidad.getFechaNacimiento())
                .fechaCreacion(entidad.getFechaCreacion())
                .fechaModificacion(entidad.getFechaModificacion())
                .build();
    }
}