package com.pruebatecnica.banco.infrastructure.adapter.out.persistence;

import com.pruebatecnica.banco.domain.model.Transaccion;
import com.pruebatecnica.banco.domain.port.out.TransaccionRepositoryPort;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public class TransaccionRepositoryAdapter implements TransaccionRepositoryPort {

    private final TransaccionJpaRepository transaccionJpaRepository;
    private final ProductoJpaRepository productoJpaRepository;

    public TransaccionRepositoryAdapter(TransaccionJpaRepository transaccionJpaRepository,
            ProductoJpaRepository productoJpaRepository) {
        this.transaccionJpaRepository = transaccionJpaRepository;
        this.productoJpaRepository = productoJpaRepository;
    }

    @Override
    public Transaccion guardar(Transaccion transaccion) {
        TransaccionEntity entidad = aEntidad(transaccion);
        return aDominio(transaccionJpaRepository.save(entidad));
    }

    @Override
    public Optional<Transaccion> buscarPorId(Long id) {
        return transaccionJpaRepository.findById(id).map(this::aDominio);
    }

    @Override
    public List<Transaccion> listarTodas() {
        return transaccionJpaRepository.findAll()
                .stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public List<Transaccion> listarPorProductoId(Long productoId) {
        return transaccionJpaRepository.findByProducto_IdOrderByFechaDesc(productoId)
                .stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public boolean existePorProductoId(Long productoId) {
        return transaccionJpaRepository.existsByProducto_Id(productoId);
    }

    private TransaccionEntity aEntidad(Transaccion transaccion) {
        return TransaccionEntity.builder()
                .id(transaccion.getId())
                .tipo(transaccion.getTipo())
                .naturaleza(transaccion.getNaturaleza())
                .producto(productoJpaRepository.getReferenceById(transaccion.getProductoId()))
                .productoRelacionado(transaccion.getProductoRelacionadoId() == null
                        ? null
                        : productoJpaRepository.getReferenceById(transaccion.getProductoRelacionadoId()))
                .monto(transaccion.getMonto())
                .saldoResultante(transaccion.getSaldoResultante())
                .referencia(transaccion.getReferencia())
                .descripcion(transaccion.getDescripcion())
                .fecha(transaccion.getFecha())
                .build();
    }

    private Transaccion aDominio(TransaccionEntity entidad) {
        return Transaccion.builder()
                .id(entidad.getId())
                .tipo(entidad.getTipo())
                .naturaleza(entidad.getNaturaleza())
                .productoId(entidad.getProducto().getId())
                .productoRelacionadoId(entidad.getProductoRelacionado() == null
                        ? null
                        : entidad.getProductoRelacionado().getId())
                .monto(entidad.getMonto())
                .saldoResultante(entidad.getSaldoResultante())
                .referencia(entidad.getReferencia())
                .descripcion(entidad.getDescripcion())
                .fecha(entidad.getFecha())
                .build();
    }
}
