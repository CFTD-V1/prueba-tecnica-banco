import { HttpErrorResponse } from '@angular/common/http';

import { mensajeDeError } from './mensaje-error';

describe('mensajeDeError', () => {
  it('devuelve el detalle que envía el backend en el ProblemDetail', () => {
    const error = new HttpErrorResponse({
      status: 400,
      error: {
        title: 'Regla de negocio incumplida',
        detail: 'Una cuenta de ahorros no puede tener un saldo menor a $0',
      },
    });

    expect(mensajeDeError(error)).toBe('Una cuenta de ahorros no puede tener un saldo menor a $0');
  });

  it('junta los errores de validación campo por campo', () => {
    const error = new HttpErrorResponse({
      status: 400,
      error: {
        title: 'Error de validación',
        errores: {
          correo: 'El correo no tiene un formato válido',
          nombres: 'Los nombres son obligatorios',
        },
      },
    });

    const mensaje = mensajeDeError(error);

    expect(mensaje).toContain('El correo no tiene un formato válido');
    expect(mensaje).toContain('Los nombres son obligatorios');
  });

  it('avisa cuando no hay conexión con el servidor', () => {
    const error = new HttpErrorResponse({ status: 0 });

    expect(mensajeDeError(error)).toContain('No se pudo conectar con el servidor');
  });

  it('devuelve un mensaje genérico ante un error desconocido', () => {
    expect(mensajeDeError(new Error('algo'))).toBe('Ocurrió un error inesperado.');
  });
});
