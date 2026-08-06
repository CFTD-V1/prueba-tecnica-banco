export type TipoCuenta = 'AHORROS' | 'CORRIENTE';
export type EstadoCuenta = 'ACTIVA' | 'INACTIVA' | 'CANCELADA';

export const TIPOS_CUENTA: { valor: TipoCuenta; etiqueta: string }[] = [
  { valor: 'AHORROS', etiqueta: 'Cuenta de ahorros' },
  { valor: 'CORRIENTE', etiqueta: 'Cuenta corriente' },
];

export interface Producto {
  id: number;
  tipoCuenta: TipoCuenta;
  numeroCuenta: string;
  estado: EstadoCuenta;
  saldo: number;
  saldoDisponible: number;
  exentaGmf: boolean;
  clienteId: number;
  fechaCreacion: string;
  fechaModificacion: string | null;
}

/** La cuenta se abre siempre en $0, por eso no se envía saldo. */
export interface ProductoRequest {
  tipoCuenta: TipoCuenta;
  clienteId: number;
  exentaGmf: boolean;
}
