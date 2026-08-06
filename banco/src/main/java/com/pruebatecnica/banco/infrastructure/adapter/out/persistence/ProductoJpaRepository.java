package com.pruebatecnica.banco.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface ProductoJpaRepository extends JpaRepository<ProductoEntity, Long> {

    List<ProductoEntity> findByCliente_Id(Long clienteId);

    boolean existsByNumeroCuenta(String numeroCuenta);

    boolean existsByCliente_Id(Long clienteId);

    /**
     * Bloqueo pesimista de escritura: se traduce en un SELECT ... FOR UPDATE, de modo
     * que otra transaccion que quiera tocar la misma cuenta espera a que esta termine.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProductoEntity p where p.id = :id")
    Optional<ProductoEntity> buscarPorIdConBloqueo(@Param("id") Long id);
}
