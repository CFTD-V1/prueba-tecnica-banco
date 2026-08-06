package com.pruebatecnica.banco.application.service;

import com.pruebatecnica.banco.domain.exception.ExcepcionDeNegocio;
import com.pruebatecnica.banco.domain.exception.ExcepcionDeRecursoNoEncontrado;
import com.pruebatecnica.banco.domain.model.EstadoCuenta;
import com.pruebatecnica.banco.domain.model.NaturalezaMovimiento;
import com.pruebatecnica.banco.domain.model.Producto;
import com.pruebatecnica.banco.domain.model.TipoCuenta;
import com.pruebatecnica.banco.domain.model.TipoTransaccion;
import com.pruebatecnica.banco.domain.model.Transaccion;
import com.pruebatecnica.banco.domain.port.out.ProductoRepositoryPort;
import com.pruebatecnica.banco.domain.port.out.TransaccionRepositoryPort;

import java.math.BigDecimal;
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
class TransaccionServiceTest {

    @Mock
    private TransaccionRepositoryPort transaccionRepositoryPort;

    @Mock
    private ProductoRepositoryPort productoRepositoryPort;

    @InjectMocks
    private TransaccionService transaccionService;

    private Producto cuenta(Long id, TipoCuenta tipo, String saldo, EstadoCuenta estado) {
        return Producto.builder()
                .id(id)
                .tipoCuenta(tipo)
                .numeroCuenta(tipo.getPrefijo() + "12345678")
                .estado(estado)
                .saldo(new BigDecimal(saldo))
                .saldoDisponible(new BigDecimal(saldo))
                .clienteId(1L)
                .build();
    }

    private void devuelveLoQueSeGuarda() {
        given(transaccionRepositoryPort.guardar(any(Transaccion.class)))
                .willAnswer(invocacion -> invocacion.getArgument(0));
    }

    @Test
    @DisplayName("La consignacion suma al saldo y registra un movimiento credito")
    void laConsignacionSumaAlSaldo() {
        Producto producto = cuenta(1L, TipoCuenta.AHORROS, "100000", EstadoCuenta.ACTIVA);
        given(productoRepositoryPort.buscarPorIdConBloqueo(1L)).willReturn(Optional.of(producto));
        devuelveLoQueSeGuarda();

        Transaccion resultado = transaccionService.consignar(1L, new BigDecimal("50000"), "Pago nomina");

        assertThat(producto.getSaldo()).isEqualByComparingTo("150000");
        assertThat(producto.getSaldoDisponible()).isEqualByComparingTo("150000");
        assertThat(resultado.getTipo()).isEqualTo(TipoTransaccion.CONSIGNACION);
        assertThat(resultado.getNaturaleza()).isEqualTo(NaturalezaMovimiento.CREDITO);
        assertThat(resultado.getSaldoResultante()).isEqualByComparingTo("150000");
        assertThat(resultado.getReferencia()).isNotBlank();
        assertThat(resultado.getFecha()).isNotNull();
        verify(productoRepositoryPort, times(1)).guardar(producto);
    }

    @Test
    @DisplayName("No se puede consignar en una cuenta que no existe")
    void noSeConsignaEnCuentaInexistente() {
        given(productoRepositoryPort.buscarPorIdConBloqueo(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> transaccionService.consignar(99L, new BigDecimal("1000"), null))
                .isInstanceOf(ExcepcionDeRecursoNoEncontrado.class)
                .hasMessageContaining("No existe un producto");

        verify(transaccionRepositoryPort, never()).guardar(any(Transaccion.class));
    }

    @Test
    @DisplayName("No se puede consignar en una cuenta inactiva")
    void noSeConsignaEnCuentaInactiva() {
        given(productoRepositoryPort.buscarPorIdConBloqueo(1L))
                .willReturn(Optional.of(cuenta(1L, TipoCuenta.AHORROS, "1000", EstadoCuenta.INACTIVA)));

        assertThatThrownBy(() -> transaccionService.consignar(1L, new BigDecimal("1000"), null))
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("cuenta activa");

        verify(transaccionRepositoryPort, never()).guardar(any(Transaccion.class));
    }

    @Test
    @DisplayName("El retiro resta del saldo y registra un movimiento debito")
    void elRetiroRestaDelSaldo() {
        Producto producto = cuenta(1L, TipoCuenta.AHORROS, "100000", EstadoCuenta.ACTIVA);
        given(productoRepositoryPort.buscarPorIdConBloqueo(1L)).willReturn(Optional.of(producto));
        devuelveLoQueSeGuarda();

        Transaccion resultado = transaccionService.retirar(1L, new BigDecimal("30000"), "Cajero");

        assertThat(producto.getSaldo()).isEqualByComparingTo("70000");
        assertThat(resultado.getTipo()).isEqualTo(TipoTransaccion.RETIRO);
        assertThat(resultado.getNaturaleza()).isEqualTo(NaturalezaMovimiento.DEBITO);
        assertThat(resultado.getSaldoResultante()).isEqualByComparingTo("70000");
    }

    @Test
    @DisplayName("Una cuenta de ahorros no puede quedar en negativo tras un retiro")
    void elRetiroNoDejaLaCuentaDeAhorrosEnNegativo() {
        given(productoRepositoryPort.buscarPorIdConBloqueo(1L))
                .willReturn(Optional.of(cuenta(1L, TipoCuenta.AHORROS, "10000", EstadoCuenta.ACTIVA)));

        assertThatThrownBy(() -> transaccionService.retirar(1L, new BigDecimal("50000"), null))
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("no puede tener un saldo menor a $0");

        verify(transaccionRepositoryPort, never()).guardar(any(Transaccion.class));
    }

    @Test
    @DisplayName("El monto de un movimiento debe ser mayor que cero")
    void elMontoDebeSerMayorQueCero() {
        given(productoRepositoryPort.buscarPorIdConBloqueo(1L))
                .willReturn(Optional.of(cuenta(1L, TipoCuenta.AHORROS, "10000", EstadoCuenta.ACTIVA)));

        assertThatThrownBy(() -> transaccionService.consignar(1L, BigDecimal.ZERO, null))
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("mayor que cero");
    }

    @Test
    @DisplayName("La transferencia genera un debito en el origen y un credito en el destino")
    void laTransferenciaGeneraDosMovimientos() {
        Producto origen = cuenta(1L, TipoCuenta.CORRIENTE, "200000", EstadoCuenta.ACTIVA);
        Producto destino = cuenta(2L, TipoCuenta.AHORROS, "50000", EstadoCuenta.ACTIVA);
        given(productoRepositoryPort.buscarPorIdConBloqueo(1L)).willReturn(Optional.of(origen));
        given(productoRepositoryPort.buscarPorIdConBloqueo(2L)).willReturn(Optional.of(destino));
        devuelveLoQueSeGuarda();

        List<Transaccion> movimientos = transaccionService.transferir(
                1L, 2L, new BigDecimal("80000"), "Pago arriendo");

        assertThat(origen.getSaldo()).isEqualByComparingTo("120000");
        assertThat(destino.getSaldo()).isEqualByComparingTo("130000");
        assertThat(movimientos).hasSize(2);

        Transaccion debito = movimientos.get(0);
        Transaccion credito = movimientos.get(1);

        assertThat(debito.getNaturaleza()).isEqualTo(NaturalezaMovimiento.DEBITO);
        assertThat(debito.getProductoId()).isEqualTo(1L);
        assertThat(debito.getProductoRelacionadoId()).isEqualTo(2L);
        assertThat(credito.getNaturaleza()).isEqualTo(NaturalezaMovimiento.CREDITO);
        assertThat(credito.getProductoId()).isEqualTo(2L);
        assertThat(credito.getProductoRelacionadoId()).isEqualTo(1L);
        assertThat(debito.getReferencia()).isEqualTo(credito.getReferencia());
        verify(productoRepositoryPort, times(1)).guardar(origen);
        verify(productoRepositoryPort, times(1)).guardar(destino);
    }

    @Test
    @DisplayName("No se puede transferir a la misma cuenta")
    void noSeTransfiereALaMismaCuenta() {
        assertThatThrownBy(() -> transaccionService.transferir(1L, 1L, new BigDecimal("1000"), null))
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("deben ser distintas");

        verify(transaccionRepositoryPort, never()).guardar(any(Transaccion.class));
    }

    @Test
    @DisplayName("No se puede transferir a una cuenta que no existe en el sistema")
    void noSeTransfiereACuentaInexistente() {
        given(productoRepositoryPort.buscarPorIdConBloqueo(1L))
                .willReturn(Optional.of(cuenta(1L, TipoCuenta.CORRIENTE, "200000", EstadoCuenta.ACTIVA)));
        given(productoRepositoryPort.buscarPorIdConBloqueo(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> transaccionService.transferir(1L, 99L, new BigDecimal("1000"), null))
                .isInstanceOf(ExcepcionDeRecursoNoEncontrado.class)
                .hasMessageContaining("No existe un producto");

        verify(transaccionRepositoryPort, never()).guardar(any(Transaccion.class));
    }

    @Test
    @DisplayName("Una transferencia sin saldo suficiente no registra ningun movimiento")
    void laTransferenciaSinSaldoNoRegistraMovimientos() {
        given(productoRepositoryPort.buscarPorIdConBloqueo(1L))
                .willReturn(Optional.of(cuenta(1L, TipoCuenta.AHORROS, "5000", EstadoCuenta.ACTIVA)));
        given(productoRepositoryPort.buscarPorIdConBloqueo(2L))
                .willReturn(Optional.of(cuenta(2L, TipoCuenta.AHORROS, "1000", EstadoCuenta.ACTIVA)));

        assertThatThrownBy(() -> transaccionService.transferir(1L, 2L, new BigDecimal("90000"), null))
                .isInstanceOf(ExcepcionDeNegocio.class)
                .hasMessageContaining("no puede tener un saldo menor a $0");

        verify(transaccionRepositoryPort, never()).guardar(any(Transaccion.class));
    }

    @Test
    @DisplayName("Lanza excepcion cuando se consulta una transaccion que no existe")
    void lanzaExcepcionCuandoLaTransaccionNoExiste() {
        given(transaccionRepositoryPort.buscarPorId(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> transaccionService.obtenerPorId(99L))
                .isInstanceOf(ExcepcionDeRecursoNoEncontrado.class)
                .hasMessageContaining("No existe una transacción");
    }

    @Test
    @DisplayName("Lista todos los movimientos")
    void listaTodosLosMovimientos() {
        given(transaccionRepositoryPort.listarTodas())
                .willReturn(List.of(Transaccion.builder().id(1L).build()));

        assertThat(transaccionService.listar()).hasSize(1);
    }

    @Test
    @DisplayName("El estado de cuenta devuelve los movimientos del producto")
    void elEstadoDeCuentaDevuelveLosMovimientosDelProducto() {
        given(productoRepositoryPort.buscarPorId(1L))
                .willReturn(Optional.of(cuenta(1L, TipoCuenta.AHORROS, "1000", EstadoCuenta.ACTIVA)));
        given(transaccionRepositoryPort.listarPorProductoId(1L))
                .willReturn(List.of(Transaccion.builder().id(1L).build()));

        assertThat(transaccionService.listarPorProducto(1L)).hasSize(1);
    }

    @Test
    @DisplayName("El estado de cuenta falla si el producto no existe")
    void elEstadoDeCuentaFallaSiElProductoNoExiste() {
        given(productoRepositoryPort.buscarPorId(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> transaccionService.listarPorProducto(99L))
                .isInstanceOf(ExcepcionDeRecursoNoEncontrado.class)
                .hasMessageContaining("No existe un producto");
    }
}
