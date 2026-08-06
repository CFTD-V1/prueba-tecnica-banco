package com.pruebatecnica.banco.infrastructure.config;

import com.pruebatecnica.banco.domain.port.in.ClienteCasosDeUso;
import com.pruebatecnica.banco.infrastructure.adapter.in.rest.ClienteController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;

@WebMvcTest(ClienteController.class)
class ConfiguracionCorsTest {

    private static final String ORIGEN_PERMITIDO = "http://localhost:4200";
    private static final String ORIGEN_NO_PERMITIDO = "http://sitio-no-autorizado.com";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteCasosDeUso clienteCasosDeUso;

    @Test
    @DisplayName("La verificacion previa desde el front autorizado devuelve las cabeceras de CORS")
    void permiteLaVerificacionPreviaDesdeElFront() throws Exception {
        mockMvc.perform(options("/api/clientes")
                        .header("Origin", ORIGEN_PERMITIDO)
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGEN_PERMITIDO))
                .andExpect(header().string("Access-Control-Max-Age", "3600"));
    }

    @Test
    @DisplayName("Una peticion real desde el front autorizado incluye la cabecera de origen permitido")
    void permiteLaPeticionRealDesdeElFront() throws Exception {
        given(clienteCasosDeUso.listar()).willReturn(List.of());

        mockMvc.perform(get("/api/clientes").header("Origin", ORIGEN_PERMITIDO))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGEN_PERMITIDO));
    }

    @Test
    @DisplayName("Se rechaza la verificacion previa desde un origen no autorizado")
    void rechazaLosOrigenesNoAutorizados() throws Exception {
        mockMvc.perform(options("/api/clientes")
                        .header("Origin", ORIGEN_NO_PERMITIDO)
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
