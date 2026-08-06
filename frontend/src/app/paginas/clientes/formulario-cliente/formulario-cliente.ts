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

/** Misma regla que aplica el backend: el cliente debe ser mayor de edad. */
function mayorDeEdad(control: AbstractControl): ValidationErrors | null {
  if (!control.value) {
    return null;
  }

  const nacimiento = new Date(control.value);
  const limite = new Date();
  limite.setFullYear(limite.getFullYear() - 18);

  return nacimiento > limite ? { menorDeEdad: true } : null;
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

  protected readonly formulario = this.fb.nonNullable.group({
    tipoIdentificacion: ['CC' as TipoIdentificacion, Validators.required],
    numeroIdentificacion: ['', [Validators.required, Validators.maxLength(20)]],
    nombres: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    apellido: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    correo: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
    fechaNacimiento: ['', [Validators.required, mayorDeEdad]],
  });

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

  /** Un control solo muestra su error cuando el usuario ya interactuó con él. */
  protected invalido(campo: string): boolean {
    const control = this.formulario.get(campo);
    return !!control && control.invalid && (control.touched || control.dirty);
  }
}
