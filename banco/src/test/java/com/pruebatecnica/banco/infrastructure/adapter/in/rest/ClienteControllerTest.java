package com.pruebatecnica.banco.infrastructure.adapter.in.rest;

import com.pruebatecnica.banco.domain.exception.ExcepcionDeNegocio;
import com.pruebatecnica.banco.domain.model.Cliente;
import com.pruebatecnica.banco.domain.model.TipoIdentificacion;
import com.pruebatecnica.banco.domain.port.in.ClienteCasosDeUso;

import java.time.LocalDate;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    private static final String CUERPO_VALIDO = """
            {
              "tipoIdentificacion": "CC",
              "numeroIdentificacion": "1020304050",
              "nombres": "Manuel",
              "apellido": "Tafur",
              "correo": "manueldev@grupohayplan.com",
              "fechaNacimiento": "1998-05-20"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteCasosDeUso clienteCasosDeUso;

    private Cliente clienteGuardado() {
        return Cliente.builder()
                .id(1L)
                .tipoIdentificacion(TipoIdentificacion.CC)
                .numeroIdentificacion("1020304050")
                .nombres("Manuel")
                .apellido("Tafur")
                .correo("manueldev@grupohayplan.com")
                .fechaNacimiento(LocalDate.of(1998, 5, 20))
                .fechaCreacion(LocalDateTime.of(2026, 8, 5, 12, 0))
                .fechaModificacion(LocalDateTime.of(2026, 8, 5, 12, 0))
                .build();
    }

    @Test
    @DisplayName("POST /api/clientes responde 201 con el cliente creado")
    void creaClienteYResponde201() throws Exception {
        given(clienteCasosDeUso.crear(any(Cliente.class))).willReturn(clienteGuardado());

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombres").value("Manuel"))
                .andExpect(jsonPath("$.tipoIdentificacion").value("CC"))
                .andExpect(jsonPath("$.fechaCreacion").exists());
    }

    @Test
    @DisplayName("POST /api/clientes responde 400 cuando el correo no es valido")
    void responde400CuandoElCorreoEsInvalido() throws Exception {
        String cuerpoInvalido = CUERPO_VALIDO
                .replace("manueldev@grupohayplan.com", "esto-no-es-un-correo");

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Error de validacion"))
                .andExpect(jsonPath("$.errores.correo").exists());
    }

    @Test
    @DisplayName("POST /api/clientes responde 400 cuando el cliente es menor de edad")
    void responde400CuandoElClienteEsMenorDeEdad() throws Exception {
        given(clienteCasosDeUso.crear(any(Cliente.class)))
                .willThrow(new ExcepcionDeNegocio("El cliente debe ser mayor de edad"));

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Regla de negocio incumplida"))
                .andExpect(jsonPath("$.detail").value("El cliente debe ser mayor de edad"));
    }

    @Test
    @DisplayName("GET /api/clientes responde 200 con la lista de clientes")
    void listaLosClientesYResponde200() throws Exception {
        given(clienteCasosDeUso.listar()).willReturn(List.of(clienteGuardado()));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].numeroIdentificacion").value("1020304050"));
    }

    @Test
    @DisplayName("GET /api/clientes/{id} responde 200 con el cliente solicitado")
    void obtieneElClientePorIdYResponde200() throws Exception {
        given(clienteCasosDeUso.obtenerPorId(1L)).willReturn(clienteGuardado());

        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.apellido").value("Tafur"));
    }

    @Test
    @DisplayName("GET /api/clientes/{id} responde 400 cuando el cliente no existe")
    void responde400CuandoElClienteNoExiste() throws Exception {
        given(clienteCasosDeUso.obtenerPorId(99L))
                .willThrow(new ExcepcionDeNegocio("No existe un cliente con el id 99"));

        mockMvc.perform(get("/api/clientes/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("No existe un cliente con el id 99"));
    }

    @Test
    @DisplayName("PUT /api/clientes/{id} responde 200 con el cliente actualizado")
    void actualizaElClienteYResponde200() throws Exception {
        given(clienteCasosDeUso.actualizar(eq(1L), any(Cliente.class)))
                .willReturn(clienteGuardado());

        mockMvc.perform(put("/api/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("DELETE /api/clientes/{id} responde 204 sin contenido")
    void eliminaElClienteYResponde204() throws Exception {
        mockMvc.perform(delete("/api/clientes/1"))
                .andExpect(status().isNoContent());

        verify(clienteCasosDeUso, times(1)).eliminar(1L);
    }

    @Test
    @DisplayName("DELETE /api/clientes/{id} responde 400 cuando la regla de negocio lo impide")
    void responde400CuandoNoSePuedeEliminar() throws Exception {
        willThrow(new ExcepcionDeNegocio("El cliente tiene productos vinculados"))
                .given(clienteCasosDeUso).eliminar(1L);

        mockMvc.perform(delete("/api/clientes/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("El cliente tiene productos vinculados"));
    }
}
