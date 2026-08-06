package com.pruebatecnica.banco.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoJpaRepository extends JpaRepository<ProductoEntity, Long> {

    Optional<ProductoEntity> findByNumeroCuenta(String numeroCuenta);

    List<ProductoEntity> findByCliente_Id(Long clienteId);

    boolean existsByNumeroCuenta(String numeroCuenta);

    boolean existsByCliente_Id(Long clienteId);
}
