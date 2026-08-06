package com.pruebatecnica.banco.application.service;

import com.pruebatecnica.banco.domain.exception.ExcepcionDeNegocio;
import com.pruebatecnica.banco.domain.exception.ExcepcionDeRecursoNoEncontrado;
import com.pruebatecnica.banco.domain.model.Cliente;
import com.pruebatecnica.banco.domain.model.TipoIdentificacion;
import com.pruebatecnica.banco.domain.port.out.ClienteRepositoryPort;
import com.pruebatecnica.banco.domain.port.out.ProductoRepositoryPort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepositoryPort clienteRepositoryPort;

    @Mock
    private ProductoRepositoryPort productoRepositoryPort;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente clienteValido() {
        return Cliente.builder()
                .tipoIdentificacion(TipoIdentificacion.CC)
                .numeroIdentificacion("1020304050")
                .nombres("Manuel")
                .apellido("Tafur")
                .correo("manueldev@grupohayplan.com")
                .fechaNacimiento(LocalDate.of(1998, 5, 20))
                .build();
    }

    @Test
    @DisplayName("Crea el cliente cuando los datos son validos")
    void creaClienteCuandoLosDatosSonValidos() {
        given(clienteRepositoryPort.existePorNumeroIdentificacion("1020304050"))
                .willReturn(false);
        given(clienteRepositoryPort.guardar(any(Cliente.class)))
                .willAnswer(invocacion -> invocacion.getArgument(0));

        Cliente resultado = clienteService.crear(clienteValido());

        assertThat(resultado.getNombres()).isEqualTo("Manuel");
        assertThat(resultado.getFechaCreacion()).isNotNull();
        assertThat(resultado.getFechaModificacion()).isNotNull();
        verify(clienteRepositoryPort, times(1)).guardar(any(Cliente.class));
    }

    @Test
    @DisplayName("Rechaza el cliente cuando la identificacion ya existe")
    void rechazaClienteCuandoLaIdentificacionYaExiste() {
        given(clienteRepositoryPort.existePorNumeroIdentificacion("1020304050"))
                .willReturn(true);

        assertThatThrownBy(() -> clienteService.crear(clienteValido()))
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("Ya existe un cliente");

        verify(clienteRepositoryPort, never()).guardar(any(Cliente.class));
    }

    @Test
    @DisplayName("Rechaza el cliente cuando es menor de edad")
    void rechazaClienteCuandoEsMenorDeEdad() {
        Cliente menor = clienteValido();
        menor.setFechaNacimiento(LocalDate.now().minusYears(10));

        assertThatThrownBy(() -> clienteService.crear(menor))
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("mayor de edad");

        verify(clienteRepositoryPort, never()).guardar(any(Cliente.class));
    }

    @Test
    @DisplayName("Rechaza el cliente cuando el correo no tiene formato valido")
    void rechazaClienteCuandoElCorreoEsInvalido() {
        Cliente cliente = clienteValido();
        cliente.setCorreo("esto-no-es-un-correo");

        assertThatThrownBy(() -> clienteService.crear(cliente))
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("correo");

        verify(clienteRepositoryPort, never()).guardar(any(Cliente.class));
    }

    @Test
    @DisplayName("Rechaza el cliente cuando el nombre tiene menos de dos caracteres")
    void rechazaClienteCuandoElNombreEsMuyCorto() {
        Cliente cliente = clienteValido();
        cliente.setNombres("A");

        assertThatThrownBy(() -> clienteService.crear(cliente))
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("al menos 2 caracteres");

        verify(clienteRepositoryPort, never()).guardar(any(Cliente.class));
    }

    @Test
    @DisplayName("Devuelve el cliente cuando existe")
    void devuelveElClienteCuandoExiste() {
        Cliente existente = clienteValido();
        existente.setId(1L);
        given(clienteRepositoryPort.buscarPorId(1L)).willReturn(Optional.of(existente));

        Cliente resultado = clienteService.obtenerPorId(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNumeroIdentificacion()).isEqualTo("1020304050");
    }

    @Test
    @DisplayName("Lanza excepcion cuando se consulta un cliente que no existe")
    void lanzaExcepcionCuandoElClienteNoExiste() {
        given(clienteRepositoryPort.buscarPorId(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.obtenerPorId(99L))
                .isInstanceOf(ExcepcionDeRecursoNoEncontrado.class)
                .hasMessageContaining("No existe un cliente");
    }

    @Test
    @DisplayName("Lista todos los clientes")
    void listaTodosLosClientes() {
        given(clienteRepositoryPort.listarTodos()).willReturn(List.of(clienteValido()));

        List<Cliente> resultado = clienteService.listar();

        assertThat(resultado).hasSize(1);
        verify(clienteRepositoryPort, times(1)).listarTodos();
    }

    @Test
    @DisplayName("Actualiza el cliente conservando la fecha de creacion original")
    void actualizaElClienteConservandoLaFechaDeCreacion() {
        LocalDateTime fechaCreacionOriginal = LocalDateTime.of(2020, 1, 1, 10, 0);
        Cliente existente = clienteValido();
        existente.setId(1L);
        existente.setFechaCreacion(fechaCreacionOriginal);

        Cliente datosNuevos = clienteValido();
        datosNuevos.setNombres("Manuel Alberto");

        given(clienteRepositoryPort.buscarPorId(1L)).willReturn(Optional.of(existente));
        given(clienteRepositoryPort.guardar(any(Cliente.class)))
                .willAnswer(invocacion -> invocacion.getArgument(0));

        Cliente resultado = clienteService.actualizar(1L, datosNuevos);

        assertThat(resultado.getNombres()).isEqualTo("Manuel Alberto");
        assertThat(resultado.getFechaCreacion()).isEqualTo(fechaCreacionOriginal);
        assertThat(resultado.getFechaModificacion()).isAfter(fechaCreacionOriginal);
    }

    @Test
    @DisplayName("Rechaza la actualizacion cuando la nueva identificacion ya pertenece a otro cliente")
    void rechazaLaActualizacionCuandoLaIdentificacionYaExiste() {
        Cliente existente = clienteValido();
        existente.setId(1L);

        Cliente datosNuevos = clienteValido();
        datosNuevos.setNumeroIdentificacion("9999999999");

        given(clienteRepositoryPort.buscarPorId(1L)).willReturn(Optional.of(existente));
        given(clienteRepositoryPort.existePorNumeroIdentificacion("9999999999"))
                .willReturn(true);

        assertThatThrownBy(() -> clienteService.actualizar(1L, datosNuevos))
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("Ya existe un cliente");

        verify(clienteRepositoryPort, never()).guardar(any(Cliente.class));
    }

    @Test
    @DisplayName("Elimina el cliente cuando existe")
    void eliminaElClienteCuandoExiste() {
        Cliente existente = clienteValido();
        existente.setId(1L);
        given(clienteRepositoryPort.buscarPorId(1L)).willReturn(Optional.of(existente));
        given(productoRepositoryPort.existePorClienteId(1L)).willReturn(false);

        clienteService.eliminar(1L);

        verify(clienteRepositoryPort, times(1)).eliminarPorId(1L);
    }

    @Test
    @DisplayName("No elimina el cliente cuando tiene productos vinculados")
    void noEliminaElClienteCuandoTieneProductosVinculados() {
        Cliente existente = clienteValido();
        existente.setId(1L);
        given(clienteRepositoryPort.buscarPorId(1L)).willReturn(Optional.of(existente));
        given(productoRepositoryPort.existePorClienteId(1L)).willReturn(true);

        assertThatThrownBy(() -> clienteService.eliminar(1L))
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("productos vinculados");

        verify(clienteRepositoryPort, never()).eliminarPorId(any());
    }

    @Test
    @DisplayName("No elimina cuando el cliente no existe")
    void noEliminaCuandoElClienteNoExiste() {
        given(clienteRepositoryPort.buscarPorId(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.eliminar(99L))
                .isInstanceOf(ExcepcionDeRecursoNoEncontrado.class)
                .hasMessageContaining("No existe un cliente");

        verify(clienteRepositoryPort, never()).eliminarPorId(any());
    }
}
