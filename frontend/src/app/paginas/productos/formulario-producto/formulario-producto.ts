import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { Cliente } from '../../../core/modelos/cliente';
import { TIPOS_CUENTA, TipoCuenta } from '../../../core/modelos/producto';
import { ClienteService } from '../../../core/servicios/cliente.service';
import { mensajeDeError } from '../../../core/servicios/mensaje-error';
import { ProductoService } from '../../../core/servicios/producto.service';

@Component({
  selector: 'app-formulario-producto',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './formulario-producto.html',
})
export class FormularioProducto {
  private readonly fb = inject(FormBuilder);
  private readonly servicio = inject(ProductoService);
  private readonly clienteServicio = inject(ClienteService);
  private readonly router = inject(Router);

  protected readonly tiposCuenta = TIPOS_CUENTA;
  protected readonly clientes = signal<Cliente[]>([]);
  protected readonly guardando = signal(false);
  protected readonly error = signal('');

  protected readonly formulario = this.fb.nonNullable.group({
    tipoCuenta: ['AHORROS' as TipoCuenta, Validators.required],
    clienteId: [null as number | null, Validators.required],
    exentaGmf: [false],
  });

  constructor() {
    this.clienteServicio.listar().subscribe({
      next: (clientes) => this.clientes.set(clientes),
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

    const { tipoCuenta, clienteId, exentaGmf } = this.formulario.getRawValue();

    this.servicio.crear({ tipoCuenta, clienteId: clienteId!, exentaGmf }).subscribe({
      next: () => this.router.navigate(['/productos']),
      error: (e) => {
        this.error.set(mensajeDeError(e));
        this.guardando.set(false);
      },
    });
  }

  protected invalido(campo: string): boolean {
    const control = this.formulario.get(campo);
    return !!control && control.invalid && (control.touched || control.dirty);
  }
}
