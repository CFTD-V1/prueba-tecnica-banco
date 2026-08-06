import { HttpErrorResponse } from '@angular/common/http';

/**
 * El backend responde los errores con el formato ProblemDetail (RFC 7807):
 * { title, status, detail, errores? }. Esta función extrae el texto que se le
 * debe mostrar al usuario, sin que cada componente tenga que conocer el formato.
 */
export function mensajeDeError(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return 'Ocurrió un error inesperado.';
  }

  if (error.status === 0) {
    return 'No se pudo conectar con el servidor. Verifique que la API esté en ejecución.';
  }

  const cuerpo = error.error;

  // Errores de validación: se muestran campo por campo.
  if (cuerpo?.errores) {
    return Object.values(cuerpo.errores as Record<string, string>).join('. ');
  }

  return cuerpo?.detail ?? 'Ocurrió un error al procesar la solicitud.';
}
