export type TipoIdentificacion = 'CC' | 'CE' | 'PASAPORTE';

export const TIPOS_IDENTIFICACION: { valor: TipoIdentificacion; etiqueta: string }[] = [
  { valor: 'CC', etiqueta: 'Cédula de ciudadanía' },
  { valor: 'CE', etiqueta: 'Cédula de extranjería' },
  { valor: 'PASAPORTE', etiqueta: 'Pasaporte' },
];

/** Respuesta del backend. */
export interface Cliente {
  id: number;
  tipoIdentificacion: TipoIdentificacion;
  numeroIdentificacion: string;
  nombres: string;
  apellido: string;
  correo: string;
  fechaNacimiento: string;
  fechaCreacion: string;
  fechaModificacion: string | null;
}

/** Cuerpo que espera el backend al crear o actualizar. */
export interface ClienteRequest {
  tipoIdentificacion: TipoIdentificacion;
  numeroIdentificacion: string;
  nombres: string;
  apellido: string;
  correo: string;
  fechaNacimiento: string;
}
