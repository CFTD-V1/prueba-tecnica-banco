package com.pruebatecnica.banco.infrastructure.adapter.out.persistence;

import com.pruebatecnica.banco.domain.model.Producto;
import com.pruebatecnica.banco.domain.port.out.ProductoRepositoryPort;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public class ProductoRepositoryAdapter implements ProductoRepositoryPort {

    private final ProductoJpaRepository productoJpaRepository;
    private final ClienteJpaRepository clienteJpaRepository;

    public ProductoRepositoryAdapter(ProductoJpaRepository productoJpaRepository,
            ClienteJpaRepository clienteJpaRepository) {
        this.productoJpaRepository = productoJpaRepository;
        this.clienteJpaRepository = clienteJpaRepository;
    }

    @Override
    public Producto guardar(Producto producto) {
        ProductoEntity entidad = aEntidad(producto);
        return aDominio(productoJpaRepository.save(entidad));
    }

    @Override
    public Optional<Producto> buscarPorId(Long id) {
        return productoJpaRepository.findById(id).map(this::aDominio);
    }

    @Override
    public Optional<Producto> buscarPorNumeroCuenta(String numeroCuenta) {
        return productoJpaRepository.findByNumeroCuenta(numeroCuenta).map(this::aDominio);
    }

    @Override
    public List<Producto> listarTodos() {
        return productoJpaRepository.findAll()
                .stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public List<Producto> listarPorClienteId(Long clienteId) {
        return productoJpaRepository.findByCliente_Id(clienteId)
                .stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public void eliminarPorId(Long id) {
        productoJpaRepository.deleteById(id);
    }

    @Override
    public boolean existePorNumeroCuenta(String numeroCuenta) {
        return productoJpaRepository.existsByNumeroCuenta(numeroCuenta);
    }

    @Override
    public boolean existePorClienteId(Long clienteId) {
        return productoJpaRepository.existsByCliente_Id(clienteId);
    }

    private ProductoEntity aEntidad(Producto producto) {
        return ProductoEntity.builder()
                .id(producto.getId())
                .tipoCuenta(producto.getTipoCuenta())
                .numeroCuenta(producto.getNumeroCuenta())
                .estado(producto.getEstado())
                .saldo(producto.getSaldo())
                .saldoDisponible(producto.getSaldoDisponible())
                .exentaGmf(producto.isExentaGmf())
                .cliente(clienteJpaRepository.getReferenceById(producto.getClienteId()))
                .fechaCreacion(producto.getFechaCreacion())
                .fechaModificacion(producto.getFechaModificacion())
                .build();
    }

    private Producto aDominio(ProductoEntity entidad) {
        return Producto.builder()
                .id(entidad.getId())
                .tipoCuenta(entidad.getTipoCuenta())
                .numeroCuenta(entidad.getNumeroCuenta())
                .estado(entidad.getEstado())
                .saldo(entidad.getSaldo())
                .saldoDisponible(entidad.getSaldoDisponible())
                .exentaGmf(entidad.isExentaGmf())
                .clienteId(entidad.getCliente().getId())
                .fechaCreacion(entidad.getFechaCreacion())
                .fechaModificacion(entidad.getFechaModificacion())
                .build();
    }
}
