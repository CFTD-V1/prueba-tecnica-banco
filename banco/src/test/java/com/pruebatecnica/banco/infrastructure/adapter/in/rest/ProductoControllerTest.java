package com.pruebatecnica.banco.infrastructure.adapter.in.rest;

import com.pruebatecnica.banco.domain.exception.ExcepcionDeNegocio;
import com.pruebatecnica.banco.domain.exception.ExcepcionDeRecursoNoEncontrado;
import com.pruebatecnica.banco.domain.model.EstadoCuenta;
import com.pruebatecnica.banco.domain.model.Producto;
import com.pruebatecnica.banco.domain.model.TipoCuenta;
import com.pruebatecnica.banco.domain.port.in.ProductoCasosDeUso;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    private static final String CUERPO_VALIDO = """
            {
              "tipoCuenta": "AHORROS",
              "clienteId": 1,
              "saldoInicial": 100000,
              "exentaGmf": false
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoCasosDeUso productoCasosDeUso;

    private Producto productoGuardado() {
        return Producto.builder()
                .id(10L)
                .tipoCuenta(TipoCuenta.AHORROS)
                .numeroCuenta("5312345678")
                .estado(EstadoCuenta.ACTIVA)
                .saldo(new BigDecimal("100000"))
                .saldoDisponible(new BigDecimal("100000"))
                .exentaGmf(false)
                .clienteId(1L)
                .fechaCreacion(LocalDateTime.of(2026, 8, 5, 12, 0))
                .fechaModificacion(LocalDateTime.of(2026, 8, 5, 12, 0))
                .build();
    }

    @Test
    @DisplayName("POST /api/productos responde 201 con el producto creado")
    void creaProductoYResponde201() throws Exception {
        given(productoCasosDeUso.crear(any(Producto.class))).willReturn(productoGuardado());

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.tipoCuenta").value("AHORROS"))
                .andExpect(jsonPath("$.numeroCuenta").value("5312345678"))
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.clienteId").value(1))
                .andExpect(jsonPath("$.saldo").exists())
                .andExpect(jsonPath("$.saldoDisponible").exists())
                .andExpect(jsonPath("$.fechaCreacion").exists());
    }

    @Test
    @DisplayName("POST /api/productos acepta el cuerpo minimo: saldo inicial y GMF son opcionales")
    void creaProductoConElCuerpoMinimo() throws Exception {
        given(productoCasosDeUso.crear(any(Producto.class))).willReturn(productoGuardado());

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoCuenta\": \"CORRIENTE\", \"clienteId\": 1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("POST /api/productos responde 400 cuando el tipo de cuenta no existe")
    void responde400CuandoElTipoDeCuentaEsDesconocido() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoCuenta\": \"NOMINA\", \"clienteId\": 1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Petición mal formada"));
    }

    @Test
    @DisplayName("POST /api/productos responde 400 cuando falta el cliente")
    void responde400CuandoFaltaElCliente() throws Exception {
        String cuerpoSinCliente = """
                {
                  "tipoCuenta": "AHORROS",
                  "saldoInicial": 0,
                  "exentaGmf": false
                }
                """;

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoSinCliente))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Error de validación"))
                .andExpect(jsonPath("$.errores.clienteId").exists());
    }

    @Test
    @DisplayName("POST /api/productos responde 400 cuando el saldo inicial es negativo")
    void responde400CuandoElSaldoInicialEsNegativo() throws Exception {
        String cuerpoNegativo = CUERPO_VALIDO.replace("100000", "-5000");

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoNegativo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.saldoInicial").exists());
    }

    @Test
    @DisplayName("POST /api/productos responde 404 cuando el cliente no existe")
    void responde404CuandoElClienteNoExiste() throws Exception {
        given(productoCasosDeUso.crear(any(Producto.class)))
                .willThrow(new ExcepcionDeRecursoNoEncontrado("No existe un cliente con el id 1"));

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"))
                .andExpect(jsonPath("$.detail").value("No existe un cliente con el id 1"));
    }

    @Test
    @DisplayName("POST /api/productos responde 409 cuando la base de datos rechaza un duplicado")
    void responde409CuandoLaBaseDeDatosRechazaUnDuplicado() throws Exception {
        given(productoCasosDeUso.crear(any(Producto.class)))
                .willThrow(new DataIntegrityViolationException("uk_productos_numero_cuenta"));

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflicto de datos"));
    }

    @Test
    @DisplayName("GET /api/productos responde 200 con la lista de productos")
    void listaLosProductosYResponde200() throws Exception {
        given(productoCasosDeUso.listar()).willReturn(List.of(productoGuardado()));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].numeroCuenta").value("5312345678"));
    }

    @Test
    @DisplayName("GET /api/productos?clienteId=1 filtra por cliente")
    void listaLosProductosDeUnClienteYResponde200() throws Exception {
        given(productoCasosDeUso.listarPorCliente(1L)).willReturn(List.of(productoGuardado()));

        mockMvc.perform(get("/api/productos").param("clienteId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clienteId").value(1));

        verify(productoCasosDeUso, times(1)).listarPorCliente(1L);
    }

    @Test
    @DisplayName("GET /api/productos/{id} responde 200 con el producto solicitado")
    void obtieneElProductoPorIdYResponde200() throws Exception {
        given(productoCasosDeUso.obtenerPorId(10L)).willReturn(productoGuardado());

        mockMvc.perform(get("/api/productos/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("GET /api/productos/{id} responde 404 cuando el producto no existe")
    void responde404CuandoElProductoNoExiste() throws Exception {
        given(productoCasosDeUso.obtenerPorId(99L))
                .willThrow(new ExcepcionDeRecursoNoEncontrado("No existe un producto con el id 99"));

        mockMvc.perform(get("/api/productos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe un producto con el id 99"));
    }

    @Test
    @DisplayName("PUT /api/productos/{id} actualiza la exencion del GMF")
    void actualizaElProductoYResponde200() throws Exception {
        given(productoCasosDeUso.actualizar(eq(10L), any(Producto.class)))
                .willReturn(productoGuardado());

        mockMvc.perform(put("/api/productos/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"exentaGmf\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("PATCH /api/productos/{id}/estado responde 200 con el nuevo estado")
    void cambiaElEstadoYResponde200() throws Exception {
        Producto inactivo = productoGuardado();
        inactivo.setEstado(EstadoCuenta.INACTIVA);
        given(productoCasosDeUso.cambiarEstado(10L, EstadoCuenta.INACTIVA)).willReturn(inactivo);

        mockMvc.perform(patch("/api/productos/10/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"INACTIVA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVA"));
    }

    @Test
    @DisplayName("PATCH /api/productos/{id}/estado responde 400 al cancelar una cuenta con saldo")
    void responde400AlCancelarConSaldo() throws Exception {
        given(productoCasosDeUso.cambiarEstado(10L, EstadoCuenta.CANCELADA))
                .willThrow(new ExcepcionDeNegocio("Solo se puede cancelar una cuenta con saldo igual a $0"));

        mockMvc.perform(patch("/api/productos/10/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"CANCELADA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Solo se puede cancelar una cuenta con saldo igual a $0"));
    }

    @Test
    @DisplayName("DELETE /api/productos/{id} responde 204 sin contenido")
    void eliminaElProductoYResponde204() throws Exception {
        mockMvc.perform(delete("/api/productos/10"))
                .andExpect(status().isNoContent());

        verify(productoCasosDeUso, times(1)).eliminar(10L);
    }

    @Test
    @DisplayName("DELETE /api/productos/{id} responde 400 cuando el producto tiene saldo")
    void responde400CuandoElProductoTieneSaldo() throws Exception {
        willThrow(new ExcepcionDeNegocio("No se puede eliminar un producto que tiene saldo"))
                .given(productoCasosDeUso).eliminar(10L);

        mockMvc.perform(delete("/api/productos/10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("No se puede eliminar un producto que tiene saldo"));
    }
}
