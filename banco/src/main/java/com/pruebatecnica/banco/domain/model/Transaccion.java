package com.pruebatecnica.banco.domain.model;

import com.pruebatecnica.banco.domain.exception.ExcepcionDeNegocio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Un movimiento sobre una cuenta. Es un registro inmutable: una vez ocurrido no se
 * modifica ni se elimina, se compensa con otro movimiento. Por eso el caso de uso
 * solo expone crear y consultar.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaccion {

    private Long id;
    private TipoTransaccion tipo;
    private NaturalezaMovimiento naturaleza;
    private Long productoId;
    private Long productoRelacionadoId;
    private BigDecimal monto;
    private BigDecimal saldoResultante;
    private String referencia;
    private String descripcion;
    private LocalDateTime fecha;

    public void validar() {
        if (tipo == null) {
            throw new ExcepcionDeNegocio("El tipo de transacción es obligatorio");
        }
        if (naturaleza == null) {
            throw new ExcepcionDeNegocio("La naturaleza del movimiento es obligatoria");
        }
        if (productoId == null) {
            throw new ExcepcionDeNegocio("El movimiento debe estar asociado a un producto");
        }
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ExcepcionDeNegocio("El monto de la transacción debe ser mayor que cero");
        }
        if (tipo == TipoTransaccion.TRANSFERENCIA && productoRelacionadoId == null) {
            throw new ExcepcionDeNegocio("Una transferencia debe indicar la cuenta contraparte");
        }
    }
}
