package com.pruebatecnica.banco.application.service;

import com.pruebatecnica.banco.domain.exception.ExcepcionDeNegocio;
import com.pruebatecnica.banco.domain.exception.ExcepcionDeRecursoNoEncontrado;
import com.pruebatecnica.banco.domain.model.Cliente;
import com.pruebatecnica.banco.domain.model.EstadoCuenta;
import com.pruebatecnica.banco.domain.model.Producto;
import com.pruebatecnica.banco.domain.model.TipoCuenta;
import com.pruebatecnica.banco.domain.port.out.ClienteRepositoryPort;
import com.pruebatecnica.banco.domain.port.out.ProductoRepositoryPort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
class ProductoServiceTest {

    @Mock
    private ProductoRepositoryPort productoRepositoryPort;

    @Mock
    private ClienteRepositoryPort clienteRepositoryPort;

    @InjectMocks
    private ProductoService productoService;

    private Producto solicitud(TipoCuenta tipoCuenta, BigDecimal saldo) {
        return Producto.builder()
                .tipoCuenta(tipoCuenta)
                .clienteId(1L)
                .saldo(saldo)
                .build();
    }

    private Producto guardado(TipoCuenta tipoCuenta, BigDecimal saldo, EstadoCuenta estado) {
        return Producto.builder()
                .id(10L)
                .tipoCuenta(tipoCuenta)
                .numeroCuenta(tipoCuenta.getPrefijo() + "12345678")
                .estado(estado)
                .saldo(saldo)
                .saldoDisponible(saldo)
                .clienteId(1L)
                .build();
    }

    private void clienteExiste() {
        given(clienteRepositoryPort.buscarPorId(1L))
                .willReturn(Optional.of(Cliente.builder().id(1L).build()));
    }

    private void devuelveLoQueSeGuarda() {
        given(productoRepositoryPort.guardar(any(Producto.class)))
                .willAnswer(invocacion -> invocacion.getArgument(0));
    }

    @Test
    @DisplayName("Crea una cuenta de ahorros activa, con numero de 10 digitos que inicia en 53")
    void creaCuentaDeAhorros() {
        clienteExiste();
        given(productoRepositoryPort.existePorNumeroCuenta(anyString())).willReturn(false);
        devuelveLoQueSeGuarda();

        Producto resultado = productoService.crear(solicitud(TipoCuenta.AHORROS, new BigDecimal("100000")));

        assertThat(resultado.getNumeroCuenta()).hasSize(10).startsWith("53").containsOnlyDigits();
        assertThat(resultado.getEstado()).isEqualTo(EstadoCuenta.ACTIVA);
        assertThat(resultado.getSaldo()).isEqualByComparingTo("100000");
        assertThat(resultado.getSaldoDisponible()).isEqualByComparingTo("100000");
        assertThat(resultado.getFechaCreacion()).isNotNull();
        assertThat(resultado.getFechaModificacion()).isNotNull();
        verify(productoRepositoryPort, times(1)).guardar(any(Producto.class));
    }

    @Test
    @DisplayName("Crea una cuenta corriente con numero que inicia en 33")
    void creaCuentaCorriente() {
        clienteExiste();
        given(productoRepositoryPort.existePorNumeroCuenta(anyString())).willReturn(false);
        devuelveLoQueSeGuarda();

        Producto resultado = productoService.crear(solicitud(TipoCuenta.CORRIENTE, BigDecimal.ZERO));

        assertThat(resultado.getNumeroCuenta()).hasSize(10).startsWith("33").containsOnlyDigits();
        assertThat(resultado.getEstado()).isEqualTo(EstadoCuenta.ACTIVA);
    }

    @Test
    @DisplayName("Rechaza el producto cuando el cliente no existe")
    void rechazaElProductoCuandoElClienteNoExiste() {
        given(clienteRepositoryPort.buscarPorId(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.crear(solicitud(TipoCuenta.AHORROS, BigDecimal.ZERO)))
                .isInstanceOf(ExcepcionDeRecursoNoEncontrado.class)
                .hasMessageContaining("No existe un cliente");

        verify(productoRepositoryPort, never()).guardar(any(Producto.class));
    }

    @Test
    @DisplayName("Rechaza el producto cuando no se envia el cliente")
    void rechazaElProductoSinCliente() {
        Producto sinCliente = Producto.builder()
                .tipoCuenta(TipoCuenta.AHORROS)
                .saldo(BigDecimal.ZERO)
                .build();

        assertThatThrownBy(() -> productoService.crear(sinCliente))
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("vinculado a un cliente");

        verify(productoRepositoryPort, never()).guardar(any(Producto.class));
    }

    @Test
    @DisplayName("Rechaza una cuenta de ahorros con saldo inicial negativo")
    void rechazaCuentaDeAhorrosConSaldoNegativo() {
        clienteExiste();

        assertThatThrownBy(() -> productoService.crear(solicitud(TipoCuenta.AHORROS, new BigDecimal("-1"))))
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("menor a $0");

        verify(productoRepositoryPort, never()).guardar(any(Producto.class));
    }

    @Test
    @DisplayName("Reintenta la generacion cuando el numero de cuenta ya existe")
    void reintentaCuandoElNumeroDeCuentaYaExiste() {
        clienteExiste();
        given(productoRepositoryPort.existePorNumeroCuenta(anyString()))
                .willReturn(true, true, false);
        devuelveLoQueSeGuarda();

        Producto resultado = productoService.crear(solicitud(TipoCuenta.AHORROS, BigDecimal.ZERO));

        assertThat(resultado.getNumeroCuenta()).hasSize(10);
        verify(productoRepositoryPort, times(3)).existePorNumeroCuenta(anyString());
        verify(productoRepositoryPort, times(1)).guardar(any(Producto.class));
    }

    @Test
    @DisplayName("Falla cuando no logra generar un numero de cuenta unico")
    void fallaCuandoNoLograGenerarNumeroUnico() {
        clienteExiste();
        given(productoRepositoryPort.existePorNumeroCuenta(anyString())).willReturn(true);

        assertThatThrownBy(() -> productoService.crear(solicitud(TipoCuenta.AHORROS, BigDecimal.ZERO)))
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("numero de cuenta unico");

        verify(productoRepositoryPort, never()).guardar(any(Producto.class));
    }

    @Test
    @DisplayName("Devuelve el producto cuando existe")
    void devuelveElProductoCuandoExiste() {
        given(productoRepositoryPort.buscarPorId(10L))
                .willReturn(Optional.of(guardado(TipoCuenta.AHORROS, BigDecimal.ZERO, EstadoCuenta.ACTIVA)));

        Producto resultado = productoService.obtenerPorId(10L);

        assertThat(resultado.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Lanza excepcion cuando se consulta un producto que no existe")
    void lanzaExcepcionCuandoElProductoNoExiste() {
        given(productoRepositoryPort.buscarPorId(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.obtenerPorId(99L))
                .isInstanceOf(ExcepcionDeRecursoNoEncontrado.class)
                .hasMessageContaining("No existe un producto");
    }

    @Test
    @DisplayName("Lista todos los productos")
    void listaTodosLosProductos() {
        given(productoRepositoryPort.listarTodos())
                .willReturn(List.of(guardado(TipoCuenta.AHORROS, BigDecimal.ZERO, EstadoCuenta.ACTIVA)));

        assertThat(productoService.listar()).hasSize(1);
    }

    @Test
    @DisplayName("Lista los productos de un cliente existente")
    void listaLosProductosDeUnCliente() {
        clienteExiste();
        given(productoRepositoryPort.listarPorClienteId(1L))
                .willReturn(List.of(guardado(TipoCuenta.CORRIENTE, BigDecimal.ZERO, EstadoCuenta.ACTIVA)));

        assertThat(productoService.listarPorCliente(1L)).hasSize(1);
    }

    @Test
    @DisplayName("Actualiza la exencion del GMF conservando el numero de cuenta")
    void actualizaLaExencionDelGmf() {
        Producto existente = guardado(TipoCuenta.AHORROS, new BigDecimal("5000"), EstadoCuenta.ACTIVA);
        given(productoRepositoryPort.buscarPorId(10L)).willReturn(Optional.of(existente));
        devuelveLoQueSeGuarda();

        Producto datos = Producto.builder().exentaGmf(true).build();
        Producto resultado = productoService.actualizar(10L, datos);

        assertThat(resultado.isExentaGmf()).isTrue();
        assertThat(resultado.getNumeroCuenta()).isEqualTo(existente.getNumeroCuenta());
        assertThat(resultado.getFechaModificacion()).isNotNull();
    }

    @Test
    @DisplayName("Inactiva una cuenta activa")
    void inactivaUnaCuentaActiva() {
        given(productoRepositoryPort.buscarPorId(10L))
                .willReturn(Optional.of(guardado(TipoCuenta.AHORROS, new BigDecimal("5000"), EstadoCuenta.ACTIVA)));
        devuelveLoQueSeGuarda();

        Producto resultado = productoService.cambiarEstado(10L, EstadoCuenta.INACTIVA);

        assertThat(resultado.getEstado()).isEqualTo(EstadoCuenta.INACTIVA);
    }

    @Test
    @DisplayName("No cancela una cuenta que tiene saldo")
    void noCancelaUnaCuentaConSaldo() {
        given(productoRepositoryPort.buscarPorId(10L))
                .willReturn(Optional.of(guardado(TipoCuenta.AHORROS, new BigDecimal("5000"), EstadoCuenta.ACTIVA)));

        assertThatThrownBy(() -> productoService.cambiarEstado(10L, EstadoCuenta.CANCELADA))
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("saldo igual a $0");

        verify(productoRepositoryPort, never()).guardar(any(Producto.class));
    }

    @Test
    @DisplayName("Cancela una cuenta con saldo cero")
    void cancelaUnaCuentaConSaldoCero() {
        given(productoRepositoryPort.buscarPorId(10L))
                .willReturn(Optional.of(guardado(TipoCuenta.AHORROS, BigDecimal.ZERO, EstadoCuenta.ACTIVA)));
        devuelveLoQueSeGuarda();

        Producto resultado = productoService.cambiarEstado(10L, EstadoCuenta.CANCELADA);

        assertThat(resultado.getEstado()).isEqualTo(EstadoCuenta.CANCELADA);
    }

    @Test
    @DisplayName("Elimina el producto cuando no tiene saldo")
    void eliminaElProductoSinSaldo() {
        given(productoRepositoryPort.buscarPorId(10L))
                .willReturn(Optional.of(guardado(TipoCuenta.AHORROS, BigDecimal.ZERO, EstadoCuenta.ACTIVA)));

        productoService.eliminar(10L);

        verify(productoRepositoryPort, times(1)).eliminarPorId(10L);
    }

    @Test
    @DisplayName("No elimina el producto cuando tiene saldo")
    void noEliminaElProductoConSaldo() {
        given(productoRepositoryPort.buscarPorId(10L))
                .willReturn(Optional.of(guardado(TipoCuenta.AHORROS, new BigDecimal("100"), EstadoCuenta.ACTIVA)));

        assertThatThrownBy(() -> productoService.eliminar(10L))
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("tiene saldo");

        verify(productoRepositoryPort, never()).eliminarPorId(any());
    }
}
