package com.pruebatecnica.banco.infrastructure.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.pruebatecnica.banco.domain.model.EstadoCuenta;
import com.pruebatecnica.banco.domain.model.TipoCuenta;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "productos",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_productos_numero_cuenta",
                columnNames = "numero_cuenta"
        ),
        indexes = @Index(
                name = "idx_productos_cliente_id",
                columnList = "cliente_id"
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cuenta", nullable = false, updatable = false, length = 20)
    private TipoCuenta tipoCuenta;

    @Column(name = "numero_cuenta", nullable = false, updatable = false, length = 10)
    private String numeroCuenta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCuenta estado;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;

    @Column(name = "saldo_disponible", nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoDisponible;

    @Column(name = "exenta_gmf", nullable = false)
    private boolean exentaGmf;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cliente_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_productos_cliente")
    )
    private ClienteEntity cliente;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;
}
