package com.pruebatecnica.banco.infrastructure.adapter.in.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.pruebatecnica.banco.domain.model.Producto;
import com.pruebatecnica.banco.domain.port.in.ProductoCasosDeUso;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoCasosDeUso productoCasosDeUso;

    public ProductoController(ProductoCasosDeUso productoCasosDeUso) {
        this.productoCasosDeUso = productoCasosDeUso;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponse crear(@Valid @RequestBody ProductoRequest request) {
        return aResponse(productoCasosDeUso.crear(aDominio(request)));
    }

    @GetMapping("/{id}")
    public ProductoResponse obtenerPorId(@PathVariable Long id) {
        return aResponse(productoCasosDeUso.obtenerPorId(id));
    }

    /** Lista todos los productos o, si se envia clienteId, solo los de ese cliente. */
    @GetMapping
    public List<ProductoResponse> listar(@RequestParam(required = false) Long clienteId) {
        List<Producto> productos = clienteId == null
                ? productoCasosDeUso.listar()
                : productoCasosDeUso.listarPorCliente(clienteId);

        return productos.stream()
                .map(this::aResponse)
                .toList();
    }

    @PutMapping("/{id}")
    public ProductoResponse actualizar(@PathVariable Long id,
            @Valid @RequestBody ActualizarProductoRequest request) {
        Producto datos = Producto.builder()
                .exentaGmf(request.exentaGmf())
                .build();

        return aResponse(productoCasosDeUso.actualizar(id, datos));
    }

    @PatchMapping("/{id}/estado")
    public ProductoResponse cambiarEstado(@PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequest request) {
        return aResponse(productoCasosDeUso.cambiarEstado(id, request.estado()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        productoCasosDeUso.eliminar(id);
    }

    private Producto aDominio(ProductoRequest request) {
        // El saldo no se recibe: lo fija el dominio al inicializar la cuenta.
        return Producto.builder()
                .tipoCuenta(request.tipoCuenta())
                .clienteId(request.clienteId())
                .exentaGmf(Boolean.TRUE.equals(request.exentaGmf()))
                .build();
    }

    private ProductoResponse aResponse(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getTipoCuenta(),
                producto.getNumeroCuenta(),
                producto.getEstado(),
                producto.getSaldo(),
                producto.getSaldoDisponible(),
                producto.isExentaGmf(),
                producto.getClienteId(),
                producto.getFechaCreacion(),
                producto.getFechaModificacion());
    }
}
