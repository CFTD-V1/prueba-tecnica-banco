-- =============================================================================
-- DML - Datos de ejemplo
-- Motor: PostgreSQL 16
--
-- Carga un juego de datos minimo para probar la aplicacion: dos clientes, tres
-- cuentas y los movimientos que las dejan con el saldo indicado al final.
--
-- Ejecucion (despues de schema.sql):
--   psql -U banco_user -d banco_db -f datos-ejemplo.sql
-- =============================================================================

DELETE FROM transacciones;
DELETE FROM productos;
DELETE FROM clientes;

-- -----------------------------------------------------------------------------
-- Clientes
-- -----------------------------------------------------------------------------
INSERT INTO clientes (id, tipo_identificacion, numero_identificacion, nombres, apellido,
                      correo, fecha_nacimiento, fecha_creacion, fecha_modificacion)
VALUES
    (1, 'CC', '1020304050', 'Laura', 'Gomez', 'laura.gomez@banco.com',
     DATE '1992-07-08', TIMESTAMP '2026-08-01 09:00:00', TIMESTAMP '2026-08-01 09:00:00'),
    (2, 'CE', '7007007007', 'Carlos', 'Mejia', 'carlos.mejia@banco.com',
     DATE '1990-11-02', TIMESTAMP '2026-08-01 09:15:00', TIMESTAMP '2026-08-01 09:15:00');

-- -----------------------------------------------------------------------------
-- Productos
-- Los numeros de cuenta respetan la regla: ahorros inicia en 53, corriente en 33.
-- -----------------------------------------------------------------------------
INSERT INTO productos (id, tipo_cuenta, numero_cuenta, estado, saldo, saldo_disponible,
                       exenta_gmf, cliente_id, fecha_creacion, fecha_modificacion)
VALUES
    (1, 'AHORROS',   '5340494842', 'ACTIVA',   50000.00, 50000.00, true,  1,
     TIMESTAMP '2026-08-01 09:30:00', TIMESTAMP '2026-08-01 10:10:00'),
    (2, 'CORRIENTE', '3357730622', 'ACTIVA',   70000.00, 70000.00, false, 1,
     TIMESTAMP '2026-08-01 09:35:00', TIMESTAMP '2026-08-01 10:10:00'),
    (3, 'AHORROS',   '5312345678', 'INACTIVA',     0.00,     0.00, false, 2,
     TIMESTAMP '2026-08-01 09:40:00', TIMESTAMP '2026-08-01 09:45:00');

-- -----------------------------------------------------------------------------
-- Transacciones
-- Consignacion y retiro generan un movimiento; la transferencia genera dos,
-- unidos por la misma referencia.
-- -----------------------------------------------------------------------------
INSERT INTO transacciones (id, tipo, naturaleza, producto_id, producto_relacionado_id,
                           monto, saldo_resultante, referencia, descripcion, fecha)
VALUES
    (1, 'CONSIGNACION',  'CREDITO', 1, NULL, 150000.00, 150000.00,
     '2b3f5e50-8e04-496c-a084-38c69007b564', 'Pago de nomina',
     TIMESTAMP '2026-08-01 09:50:00'),

    (2, 'RETIRO',        'DEBITO',  1, NULL,  30000.00, 120000.00,
     'b346cb35-6712-46cf-982a-1fbfa53be044', 'Retiro por cajero',
     TIMESTAMP '2026-08-01 10:00:00'),

    (3, 'TRANSFERENCIA', 'DEBITO',  1, 2,     70000.00,  50000.00,
     '66155b9f-a154-4de3-9df1-050be4fe6f0c', 'Pago de arriendo',
     TIMESTAMP '2026-08-01 10:10:00'),

    (4, 'TRANSFERENCIA', 'CREDITO', 2, 1,     70000.00,  70000.00,
     '66155b9f-a154-4de3-9df1-050be4fe6f0c', 'Pago de arriendo',
     TIMESTAMP '2026-08-01 10:10:00');

-- Las columnas de identidad se asignaron manualmente, asi que hay que adelantar
-- las secuencias para que los siguientes registros no choquen con las llaves ya usadas.
SELECT setval(pg_get_serial_sequence('clientes', 'id'),      (SELECT MAX(id) FROM clientes));
SELECT setval(pg_get_serial_sequence('productos', 'id'),     (SELECT MAX(id) FROM productos));
SELECT setval(pg_get_serial_sequence('transacciones', 'id'), (SELECT MAX(id) FROM transacciones));
