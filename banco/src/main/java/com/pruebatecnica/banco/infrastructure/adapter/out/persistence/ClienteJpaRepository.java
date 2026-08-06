package com.pruebatecnica.banco.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteJpaRepository extends JpaRepository<ClienteEntity, Long> {

    boolean existsByNumeroIdentificacion(String numeroIdentificacion);
}