export type TipoTransaccion = 'CONSIGNACION' | 'RETIRO' | 'TRANSFERENCIA';
export type NaturalezaMovimiento = 'CREDITO' | 'DEBITO';

export interface Transaccion {
  id: number;
  tipo: TipoTransaccion;
  naturaleza: NaturalezaMovimiento;
  productoId: number;
  productoRelacionadoId: number | null;
  monto: number;
  saldoResultante: number;
  referencia: string;
  descripcion: string | null;
  fecha: string;
}

/** Cuerpo de una consignación o un retiro. */
export interface MovimientoRequest {
  productoId: number;
  monto: number;
  descripcion?: string;
}

export interface TransferenciaRequest {
  productoOrigenId: number;
  productoDestinoId: number;
  monto: number;
  descripcion?: string;
}
