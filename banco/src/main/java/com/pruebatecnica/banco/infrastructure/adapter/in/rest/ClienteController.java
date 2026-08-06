package com.pruebatecnica.banco.infrastructure.adapter.in.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.pruebatecnica.banco.domain.model.Cliente;
import com.pruebatecnica.banco.domain.port.in.ClienteCasosDeUso;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteCasosDeUso clienteCasosDeUso;

    public ClienteController(ClienteCasosDeUso clienteCasosDeUso) {
        this.clienteCasosDeUso = clienteCasosDeUso;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse crear(@Valid @RequestBody ClienteRequest request) {
        return aResponse(clienteCasosDeUso.crear(aDominio(request)));
    }

    @GetMapping("/{id}")
    public ClienteResponse obtenerPorId(@PathVariable Long id) {
        return aResponse(clienteCasosDeUso.obtenerPorId(id));
    }

    @GetMapping
    public List<ClienteResponse> listar() {
        return clienteCasosDeUso.listar()
                .stream()
                .map(this::aResponse)
                .toList();
    }

    @PutMapping("/{id}")
    public ClienteResponse actualizar(@PathVariable Long id,
            @Valid @RequestBody ClienteRequest request) {
        return aResponse(clienteCasosDeUso.actualizar(id, aDominio(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        clienteCasosDeUso.eliminar(id);
    }

    private Cliente aDominio(ClienteRequest request) {
        return Cliente.builder()
                .tipoIdentificacion(request.tipoIdentificacion())
                .numeroIdentificacion(request.numeroIdentificacion())
                .nombres(request.nombres())
                .apellido(request.apellido())
                .correo(request.correo())
                .fechaNacimiento(request.fechaNacimiento())
                .build();
    }

    private ClienteResponse aResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getTipoIdentificacion(),
                cliente.getNumeroIdentificacion(),
                cliente.getNombres(),
                cliente.getApellido(),
                cliente.getCorreo(),
                cliente.getFechaNacimiento(),
                cliente.getFechaCreacion(),
                cliente.getFechaModificacion());
    }
}