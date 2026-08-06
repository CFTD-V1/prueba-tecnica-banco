package com.pruebatecnica.banco.infrastructure.adapter.in.rest;

import com.pruebatecnica.banco.domain.exception.ExcepcionDeNegocio;
import com.pruebatecnica.banco.domain.exception.ExcepcionDeRecursoNoEncontrado;
import com.pruebatecnica.banco.domain.model.NaturalezaMovimiento;
import com.pruebatecnica.banco.domain.model.TipoTransaccion;
import com.pruebatecnica.banco.domain.model.Transaccion;
import com.pruebatecnica.banco.domain.port.in.TransaccionCasosDeUso;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TransaccionController.class)
class TransaccionControllerTest {

    private static final String MOVIMIENTO_VALIDO = """
            {
              "productoId": 1,
              "monto": 50000,
              "descripcion": "Pago nomina"
            }
            """;

    private static final String TRANSFERENCIA_VALIDA = """
            {
              "productoOrigenId": 1,
              "productoDestinoId": 2,
              "monto": 80000,
              "descripcion": "Pago arriendo"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransaccionCasosDeUso transaccionCasosDeUso;

    private Transaccion movimiento(Long id, TipoTransaccion tipo, NaturalezaMovimiento naturaleza,
            Long productoId, Long relacionadoId) {
        return Transaccion.builder()
                .id(id)
                .tipo(tipo)
                .naturaleza(naturaleza)
                .productoId(productoId)
                .productoRelacionadoId(relacionadoId)
                .monto(new BigDecimal("50000"))
                .saldoResultante(new BigDecimal("150000"))
                .referencia("8f14e45f-ea8d-4b3a-9c2e-000000000001")
                .descripcion("Pago nomina")
                .fecha(LocalDateTime.of(2026, 8, 5, 12, 0))
                .build();
    }

    @Test
    @DisplayName("POST /api/transacciones/consignaciones responde 201 con el movimiento credito")
    void consignaYResponde201() throws Exception {
        given(transaccionCasosDeUso.consignar(eq(1L), any(BigDecimal.class), any()))
                .willReturn(movimiento(1L, TipoTransaccion.CONSIGNACION, NaturalezaMovimiento.CREDITO, 1L, null));

        mockMvc.perform(post("/api/transacciones/consignaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MOVIMIENTO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tipo").value("CONSIGNACION"))
                .andExpect(jsonPath("$.naturaleza").value("CREDITO"))
                .andExpect(jsonPath("$.productoId").value(1))
                .andExpect(jsonPath("$.saldoResultante").exists())
                .andExpect(jsonPath("$.referencia").exists());
    }

    @Test
    @DisplayName("POST /api/transacciones/consignaciones responde 400 cuando falta el producto")
    void responde400CuandoFaltaElProducto() throws Exception {
        mockMvc.perform(post("/api/transacciones/consignaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\": 50000}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Error de validación"))
                .andExpect(jsonPath("$.errores.productoId").exists());
    }

    @Test
    @DisplayName("POST /api/transacciones/consignaciones responde 400 cuando el monto no es positivo")
    void responde400CuandoElMontoNoEsPositivo() throws Exception {
        mockMvc.perform(post("/api/transacciones/consignaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\": 1, \"monto\": -100}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.monto").exists());
    }

    @Test
    @DisplayName("POST /api/transacciones/consignaciones responde 400 cuando el monto trae mas de dos decimales")
    void responde400CuandoElMontoTraeMasDeDosDecimales() throws Exception {
        mockMvc.perform(post("/api/transacciones/consignaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\": 1, \"monto\": 100.999}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.monto").exists());
    }

    @Test
    @DisplayName("POST /api/transacciones/consignaciones responde 404 cuando la cuenta no existe")
    void responde404CuandoLaCuentaNoExiste() throws Exception {
        given(transaccionCasosDeUso.consignar(eq(1L), any(BigDecimal.class), any()))
                .willThrow(new ExcepcionDeRecursoNoEncontrado("No existe un producto con el id 1"));

        mockMvc.perform(post("/api/transacciones/consignaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MOVIMIENTO_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"));
    }

    @Test
    @DisplayName("POST /api/transacciones/retiros responde 201 con el movimiento debito")
    void retiraYResponde201() throws Exception {
        given(transaccionCasosDeUso.retirar(eq(1L), any(BigDecimal.class), any()))
                .willReturn(movimiento(2L, TipoTransaccion.RETIRO, NaturalezaMovimiento.DEBITO, 1L, null));

        mockMvc.perform(post("/api/transacciones/retiros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MOVIMIENTO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("RETIRO"))
                .andExpect(jsonPath("$.naturaleza").value("DEBITO"));
    }

    @Test
    @DisplayName("POST /api/transacciones/retiros responde 400 cuando no hay saldo suficiente")
    void responde400CuandoNoHaySaldoSuficiente() throws Exception {
        given(transaccionCasosDeUso.retirar(eq(1L), any(BigDecimal.class), any()))
                .willThrow(new ExcepcionDeNegocio("Una cuenta de ahorros no puede tener un saldo menor a $0"));

        mockMvc.perform(post("/api/transacciones/retiros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MOVIMIENTO_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Regla de negocio incumplida"));
    }

    @Test
    @DisplayName("POST /api/transacciones/transferencias responde 201 con los dos movimientos")
    void transfiereYResponde201ConDosMovimientos() throws Exception {
        given(transaccionCasosDeUso.transferir(eq(1L), eq(2L), any(BigDecimal.class), any()))
                .willReturn(List.of(
                        movimiento(3L, TipoTransaccion.TRANSFERENCIA, NaturalezaMovimiento.DEBITO, 1L, 2L),
                        movimiento(4L, TipoTransaccion.TRANSFERENCIA, NaturalezaMovimiento.CREDITO, 2L, 1L)));

        mockMvc.perform(post("/api/transacciones/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TRANSFERENCIA_VALIDA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].naturaleza").value("DEBITO"))
                .andExpect(jsonPath("$[0].productoId").value(1))
                .andExpect(jsonPath("$[0].productoRelacionadoId").value(2))
                .andExpect(jsonPath("$[1].naturaleza").value("CREDITO"))
                .andExpect(jsonPath("$[1].productoId").value(2))
                .andExpect(jsonPath("$[1].productoRelacionadoId").value(1));
    }

    @Test
    @DisplayName("POST /api/transacciones/transferencias responde 400 si origen y destino son la misma cuenta")
    void responde400CuandoOrigenYDestinoSonIguales() throws Exception {
        given(transaccionCasosDeUso.transferir(eq(1L), eq(1L), any(BigDecimal.class), any()))
                .willThrow(new ExcepcionDeNegocio("La cuenta de origen y la de destino deben ser distintas"));

        mockMvc.perform(post("/api/transacciones/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TRANSFERENCIA_VALIDA.replace("\"productoDestinoId\": 2", "\"productoDestinoId\": 1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("La cuenta de origen y la de destino deben ser distintas"));
    }

    @Test
    @DisplayName("POST /api/transacciones/transferencias responde 400 cuando falta la cuenta de destino")
    void responde400CuandoFaltaLaCuentaDeDestino() throws Exception {
        mockMvc.perform(post("/api/transacciones/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoOrigenId\": 1, \"monto\": 1000}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.productoDestinoId").exists());
    }

    @Test
    @DisplayName("GET /api/transacciones/{id} responde 200 con el movimiento")
    void obtieneElMovimientoPorId() throws Exception {
        given(transaccionCasosDeUso.obtenerPorId(1L))
                .willReturn(movimiento(1L, TipoTransaccion.CONSIGNACION, NaturalezaMovimiento.CREDITO, 1L, null));

        mockMvc.perform(get("/api/transacciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /api/transacciones/{id} responde 404 cuando no existe")
    void responde404CuandoElMovimientoNoExiste() throws Exception {
        given(transaccionCasosDeUso.obtenerPorId(99L))
                .willThrow(new ExcepcionDeRecursoNoEncontrado("No existe una transacción con el id 99"));

        mockMvc.perform(get("/api/transacciones/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/transacciones responde 200 con todos los movimientos")
    void listaTodosLosMovimientos() throws Exception {
        given(transaccionCasosDeUso.listar())
                .willReturn(List.of(movimiento(1L, TipoTransaccion.CONSIGNACION, NaturalezaMovimiento.CREDITO, 1L, null)));

        mockMvc.perform(get("/api/transacciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("GET /api/transacciones?productoId=1 devuelve el estado de cuenta")
    void devuelveElEstadoDeCuenta() throws Exception {
        given(transaccionCasosDeUso.listarPorProducto(1L))
                .willReturn(List.of(movimiento(1L, TipoTransaccion.CONSIGNACION, NaturalezaMovimiento.CREDITO, 1L, null)));

        mockMvc.perform(get("/api/transacciones").param("productoId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productoId").value(1));

        verify(transaccionCasosDeUso, times(1)).listarPorProducto(1L);
    }
}
