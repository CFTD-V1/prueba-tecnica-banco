import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';

import { Producto } from '../../../core/modelos/producto';
import { Transaccion, TipoTransaccion } from '../../../core/modelos/transaccion';
import { mensajeDeError } from '../../../core/servicios/mensaje-error';
import { ProductoService } from '../../../core/servicios/producto.service';
import { TransaccionService } from '../../../core/servicios/transaccion.service';

@Component({
  selector: 'app-formulario-movimiento',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './formulario-movimiento.html',
})
export class FormularioMovimiento {
  private readonly fb = inject(FormBuilder);
  private readonly servicio = inject(TransaccionService);
  private readonly productoServicio = inject(ProductoService);
  private readonly router = inject(Router);

  protected readonly productos = signal<Producto[]>([]);
  protected readonly guardando = signal(false);
  protected readonly error = signal('');
  protected readonly tipo = signal<TipoTransaccion>('CONSIGNACION');

  /** Solo se puede operar sobre cuentas activas, igual que valida el backend. */
  protected readonly cuentasOperables = computed(() =>
    this.productos().filter((p) => p.estado === 'ACTIVA'),
  );

  protected readonly esTransferencia = computed(() => this.tipo() === 'TRANSFERENCIA');

  protected readonly formulario = this.fb.nonNullable.group({
    productoId: [null as number | null, Validators.required],
    productoDestinoId: [null as number | null],
    monto: [null as number | null, [Validators.required, Validators.min(0.01)]],
    descripcion: [''],
  });

  constructor() {
    this.productoServicio.listar().subscribe({
      next: (productos) => this.productos.set(productos),
      error: (e) => this.error.set(mensajeDeError(e)),
    });
  }

  protected cambiarTipo(tipo: TipoTransaccion): void {
    this.tipo.set(tipo);
    this.error.set('');

    const destino = this.formulario.controls.productoDestinoId;
    if (tipo === 'TRANSFERENCIA') {
      destino.addValidators(Validators.required);
    } else {
      destino.removeValidators(Validators.required);
      destino.setValue(null);
    }
    destino.updateValueAndValidity();
  }

  protected guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const { productoId, productoDestinoId, monto, descripcion } = this.formulario.getRawValue();

    if (this.esTransferencia() && productoId === productoDestinoId) {
      this.error.set('La cuenta de origen y la de destino deben ser distintas.');
      return;
    }

    this.guardando.set(true);
    this.error.set('');

    const movimiento = {
      productoId: productoId!,
      monto: monto!,
      descripcion: descripcion || undefined,
    };

    // La transferencia devuelve dos movimientos y las demás uno solo: se unifica el
    // tipo para poder suscribirse igual en los tres casos.
    const peticion: Observable<Transaccion | Transaccion[]> =
      this.tipo() === 'CONSIGNACION'
        ? this.servicio.consignar(movimiento)
        : this.tipo() === 'RETIRO'
          ? this.servicio.retirar(movimiento)
          : this.servicio.transferir({
              productoOrigenId: productoId!,
              productoDestinoId: productoDestinoId!,
              monto: monto!,
              descripcion: descripcion || undefined,
            });

    peticion.subscribe({
      next: () => this.router.navigate(['/transacciones']),
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
