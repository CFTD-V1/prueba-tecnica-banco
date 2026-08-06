package com.pruebatecnica.banco;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Prueba de humo: verifica que todo el contexto de Spring se construya.
 * Usa el perfil "test" (H2 en memoria) para no depender del contenedor de PostgreSQL.
 */
@SpringBootTest
@ActiveProfiles("test")
class BancoApplicationTests {

    @Test
    void contextLoads() {
    }
}
