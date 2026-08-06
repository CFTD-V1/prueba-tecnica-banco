package com.pruebatecnica.banco.infrastructure.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.pruebatecnica.banco.domain.model.NaturalezaMovimiento;
import com.pruebatecnica.banco.domain.model.TipoTransaccion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "transacciones",
        indexes = {
                @Index(name = "idx_transacciones_producto_id", columnList = "producto_id"),
                @Index(name = "idx_transacciones_referencia", columnList = "referencia")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    private TipoTransaccion tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 10)
    private NaturalezaMovimiento naturaleza;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "producto_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_transacciones_producto")
    )
    private ProductoEntity producto;

    /** Cuenta contraparte en una transferencia; nulo en consignaciones y retiros. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "producto_relacionado_id",
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_transacciones_producto_relacionado")
    )
    private ProductoEntity productoRelacionado;

    @Column(nullable = false, updatable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @Column(name = "saldo_resultante", nullable = false, updatable = false, precision = 19, scale = 2)
    private BigDecimal saldoResultante;

    /** Agrupa los dos movimientos que genera una misma transferencia. */
    @Column(nullable = false, updatable = false, length = 36)
    private String referencia;

    @Column(updatable = false, length = 200)
    private String descripcion;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;
}
