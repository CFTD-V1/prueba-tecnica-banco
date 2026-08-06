import { Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { TIPOS_IDENTIFICACION, TipoIdentificacion } from '../../../core/modelos/cliente';
import { ClienteService } from '../../../core/servicios/cliente.service';
import { mensajeDeError } from '../../../core/servicios/mensaje-error';

/** Letras de cualquier idioma, espacios, apóstrofes, guiones y puntos; debe empezar por letra. */
const REGEX_NOMBRE = /^\p{L}[\p{L} .'-]*$/u;

/**
 * Misma regla que aplica el backend: el cliente debe ser mayor de edad.
 * Validar también aquí evita un viaje al servidor para un dato que ya se sabe
 * inválido; la garantía sigue estando en el backend.
 */
export function mayorDeEdad(control: AbstractControl): ValidationErrors | null {
  if (!control.value) {
    return null;
  }

  const nacimiento = new Date(control.value);
  const limite = new Date();
  limite.setFullYear(limite.getFullYear() - 18);

  return nacimiento > limite ? { menorDeEdad: true } : null;
}

/** Ninguna persona viva supera los 120 años: una fecha así es un error de digitación. */
export function edadRazonable(control: AbstractControl): ValidationErrors | null {
  if (!control.value) {
    return null;
  }

  const nacimiento = new Date(control.value);
  const limite = new Date();
  limite.setFullYear(limite.getFullYear() - 120);

  return nacimiento < limite ? { edadImposible: true } : null;
}

/**
 * El formato del documento depende de su tipo: las cédulas son numéricas y los
 * pasaportes admiten letras. Como la regla combina dos campos, se valida sobre el
 * grupo y no sobre un control aislado.
 */
export function identificacionSegunTipo(grupo: AbstractControl): ValidationErrors | null {
  const tipo = grupo.get('tipoIdentificacion')?.value;
  const numero: string = grupo.get('numeroIdentificacion')?.value ?? '';

  if (!numero) {
    return null;
  }

  const patron = tipo === 'PASAPORTE' ? /^[A-Za-z0-9]+$/ : /^\d+$/;
  return patron.test(numero) ? null : { identificacionInvalida: { esPasaporte: tipo === 'PASAPORTE' } };
}

@Component({
  selector: 'app-formulario-cliente',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './formulario-cliente.html',
})
export class FormularioCliente {
  private readonly fb = inject(FormBuilder);
  private readonly servicio = inject(ClienteService);
  private readonly router = inject(Router);
  private readonly ruta = inject(ActivatedRoute);

  protected readonly tiposIdentificacion = TIPOS_IDENTIFICACION;
  protected readonly guardando = signal(false);
  protected readonly error = signal('');
  protected readonly id = signal<number | null>(null);

  /** Mismas reglas que aplica el backend, para avisar antes de enviar la petición. */
  protected readonly formulario = this.fb.nonNullable.group(
    {
      tipoIdentificacion: ['CC' as TipoIdentificacion, Validators.required],
      numeroIdentificacion: [
        '',
        [Validators.required, Validators.minLength(5), Validators.maxLength(20)],
      ],
      nombres: [
        '',
        [
          Validators.required,
          Validators.minLength(2),
          Validators.maxLength(100),
          Validators.pattern(REGEX_NOMBRE),
        ],
      ],
      apellido: [
        '',
        [
          Validators.required,
          Validators.minLength(2),
          Validators.maxLength(100),
          Validators.pattern(REGEX_NOMBRE),
        ],
      ],
      correo: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
      fechaNacimiento: ['', [Validators.required, mayorDeEdad, edadRazonable]],
    },
    { validators: identificacionSegunTipo },
  );

  constructor() {
    const parametro = this.ruta.snapshot.paramMap.get('id');
    if (parametro) {
      this.id.set(Number(parametro));
      this.cargarCliente(Number(parametro));
    }
  }

  protected get editando(): boolean {
    return this.id() !== null;
  }

  private cargarCliente(id: number): void {
    this.servicio.obtenerPorId(id).subscribe({
      next: (cliente) =>
        this.formulario.patchValue({
          tipoIdentificacion: cliente.tipoIdentificacion,
          numeroIdentificacion: cliente.numeroIdentificacion,
          nombres: cliente.nombres,
          apellido: cliente.apellido,
          correo: cliente.correo,
          fechaNacimiento: cliente.fechaNacimiento,
        }),
      error: (e) => this.error.set(mensajeDeError(e)),
    });
  }

  protected guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    this.error.set('');

    const datos = this.formulario.getRawValue();
    const id = this.id();
    const peticion = id ? this.servicio.actualizar(id, datos) : this.servicio.crear(datos);

    peticion.subscribe({
      next: () => this.router.navigate(['/clientes']),
      error: (e) => {
        this.error.set(mensajeDeError(e));
        this.guardando.set(false);
      },
    });
  }

  /** El formato de la identificación se valida sobre el grupo, no sobre el control. */
  protected identificacionConFormatoInvalido(): boolean {
    const control = this.formulario.get('numeroIdentificacion');
    return (
      this.formulario.hasError('identificacionInvalida') &&
      !!control &&
      (control.touched || control.dirty)
    );
  }

  protected esPasaporte(): boolean {
    return this.formulario.get('tipoIdentificacion')?.value === 'PASAPORTE';
  }

  /** Un control solo muestra su error cuando el usuario ya interactuó con él. */
  protected invalido(campo: string): boolean {
    const control = this.formulario.get(campo);
    return !!control && control.invalid && (control.touched || control.dirty);
  }
}
