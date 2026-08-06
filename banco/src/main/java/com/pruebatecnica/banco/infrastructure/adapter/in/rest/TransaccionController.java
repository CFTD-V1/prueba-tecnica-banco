package com.pruebatecnica.banco.infrastructure.adapter.in.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.pruebatecnica.banco.domain.model.Transaccion;
import com.pruebatecnica.banco.domain.port.in.TransaccionCasosDeUso;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    private final TransaccionCasosDeUso transaccionCasosDeUso;

    public TransaccionController(TransaccionCasosDeUso transaccionCasosDeUso) {
        this.transaccionCasosDeUso = transaccionCasosDeUso;
    }

    @PostMapping("/consignaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public TransaccionResponse consignar(@Valid @RequestBody MovimientoRequest request) {
        return aResponse(transaccionCasosDeUso.consignar(
                request.productoId(), request.monto(), request.descripcion()));
    }

    @PostMapping("/retiros")
    @ResponseStatus(HttpStatus.CREATED)
    public TransaccionResponse retirar(@Valid @RequestBody MovimientoRequest request) {
        return aResponse(transaccionCasosDeUso.retirar(
                request.productoId(), request.monto(), request.descripcion()));
    }

    /** Devuelve los dos movimientos generados: el débito del origen y el crédito del destino. */
    @PostMapping("/transferencias")
    @ResponseStatus(HttpStatus.CREATED)
    public List<TransaccionResponse> transferir(@Valid @RequestBody TransferenciaRequest request) {
        return transaccionCasosDeUso.transferir(
                        request.productoOrigenId(), request.productoDestinoId(),
                        request.monto(), request.descripcion())
                .stream()
                .map(this::aResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public TransaccionResponse obtenerPorId(@PathVariable Long id) {
        return aResponse(transaccionCasosDeUso.obtenerPorId(id));
    }

    /** Sin parámetros lista todos los movimientos; con productoId devuelve el estado de cuenta. */
    @GetMapping
    public List<TransaccionResponse> listar(@RequestParam(required = false) Long productoId) {
        List<Transaccion> movimientos = productoId == null
                ? transaccionCasosDeUso.listar()
                : transaccionCasosDeUso.listarPorProducto(productoId);

        return movimientos.stream()
                .map(this::aResponse)
                .toList();
    }

    private TransaccionResponse aResponse(Transaccion transaccion) {
        return new TransaccionResponse(
                transaccion.getId(),
                transaccion.getTipo(),
                transaccion.getNaturaleza(),
                transaccion.getProductoId(),
                transaccion.getProductoRelacionadoId(),
                transaccion.getMonto(),
                transaccion.getSaldoResultante(),
                transaccion.getReferencia(),
                transaccion.getDescripcion(),
                transaccion.getFecha());
    }
}
