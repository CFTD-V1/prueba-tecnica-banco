package com.pruebatecnica.banco.infrastructure.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransaccionJpaRepository extends JpaRepository<TransaccionEntity, Long> {

    List<TransaccionEntity> findByProducto_IdOrderByFechaDesc(Long productoId);

    boolean existsByProducto_Id(Long productoId);
}
