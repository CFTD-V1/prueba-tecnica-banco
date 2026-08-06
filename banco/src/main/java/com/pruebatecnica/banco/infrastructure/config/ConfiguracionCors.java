package com.pruebatecnica.banco.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Permite que el front, servido desde un origen distinto al de la API, pueda
 * consumirla desde el navegador.
 *
 * Sin esta configuracion el navegador bloquea la respuesta por la politica del
 * mismo origen: el front corre en el puerto 4200 y la API en el 8080, que para
 * el navegador son origenes diferentes.
 *
 * Los origenes permitidos se leen de la propiedad app.cors.origenes-permitidos,
 * de modo que al desplegar solo hay que cambiar configuracion, no codigo.
 */
@Configuration
public class ConfiguracionCors implements WebMvcConfigurer {

    private final String[] origenesPermitidos;

    public ConfiguracionCors(
            @Value("${app.cors.origenes-permitidos:http://localhost:4200}") String[] origenesPermitidos) {
        this.origenesPermitidos = origenesPermitidos;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origenesPermitidos)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                // Tiempo que el navegador puede cachear la respuesta de verificacion
                // previa, para no repetirla en cada peticion.
                .maxAge(3600);
    }
}
