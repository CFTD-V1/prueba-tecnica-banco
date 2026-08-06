import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { Producto } from '../../../core/modelos/producto';
import { NaturalezaMovimiento, Transaccion } from '../../../core/modelos/transaccion';
import { mensajeDeError } from '../../../core/servicios/mensaje-error';
import { ProductoService } from '../../../core/servicios/producto.service';
import { TransaccionService } from '../../../core/servicios/transaccion.service';

@Component({
  selector: 'app-lista-movimientos',
  imports: [RouterLink, CurrencyPipe, DatePipe],
  templateUrl: './lista-movimientos.html',
})
export class ListaMovimientos {
  private readonly servicio = inject(TransaccionService);
  private readonly productoServicio = inject(ProductoService);

  protected readonly movimientos = signal<Transaccion[]>([]);
  protected readonly productos = signal<Producto[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal('');
  protected readonly cuentaSeleccionada = signal<number | null>(null);

  private readonly productosPorId = computed(
    () => new Map(this.productos().map((p) => [p.id, p])),
  );

  /** Cuando se filtra por una cuenta, se muestra su saldo actual como resumen. */
  protected readonly cuentaActual = computed(() => {
    const id = this.cuentaSeleccionada();
    return id ? (this.productosPorId().get(id) ?? null) : null;
  });

  constructor() {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set('');

    forkJoin({
      movimientos: this.servicio.listar(this.cuentaSeleccionada() ?? undefined),
      productos: this.productoServicio.listar(),
    }).subscribe({
      next: ({ movimientos, productos }) => {
        this.movimientos.set(movimientos);
        this.productos.set(productos);
        this.cargando.set(false);
      },
      error: (e) => {
        this.error.set(mensajeDeError(e));
        this.cargando.set(false);
      },
    });
  }

  protected filtrar(valor: string): void {
    this.cuentaSeleccionada.set(valor ? Number(valor) : null);
    this.cargar();
  }

  protected numeroCuenta(id: number | null): string {
    if (id === null) {
      return '';
    }
    return this.productosPorId().get(id)?.numeroCuenta ?? `Cuenta ${id}`;
  }

  protected claseNaturaleza(naturaleza: NaturalezaMovimiento): string {
    return `etiqueta etiqueta--${naturaleza.toLowerCase()}`;
  }

  /** El débito resta y el crédito suma: se muestra con el signo correspondiente. */
  protected montoConSigno(movimiento: Transaccion): number {
    return movimiento.naturaleza === 'DEBITO' ? -movimiento.monto : movimiento.monto;
  }
}
