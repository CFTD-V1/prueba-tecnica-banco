import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { Cliente } from '../../../core/modelos/cliente';
import { EstadoCuenta, Producto } from '../../../core/modelos/producto';
import { ClienteService } from '../../../core/servicios/cliente.service';
import { mensajeDeError } from '../../../core/servicios/mensaje-error';
import { ProductoService } from '../../../core/servicios/producto.service';

@Component({
  selector: 'app-lista-productos',
  imports: [RouterLink, CurrencyPipe, FormsModule],
  templateUrl: './lista-productos.html',
})
export class ListaProductos {
  private readonly servicio = inject(ProductoService);
  private readonly clienteServicio = inject(ClienteService);

  protected readonly productos = signal<Producto[]>([]);
  protected readonly clientes = signal<Cliente[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal('');
  protected readonly aviso = signal('');
  protected readonly filtroCliente = signal<number | null>(null);

  /** Índice para mostrar el nombre del titular sin pedirlo cuenta por cuenta. */
  private readonly clientesPorId = computed(
    () => new Map(this.clientes().map((c) => [c.id, c])),
  );

  constructor() {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set('');

    forkJoin({
      productos: this.servicio.listar(this.filtroCliente() ?? undefined),
      clientes: this.clienteServicio.listar(),
    }).subscribe({
      next: ({ productos, clientes }) => {
        this.productos.set(productos);
        this.clientes.set(clientes);
        this.cargando.set(false);
      },
      error: (e) => {
        this.error.set(mensajeDeError(e));
        this.cargando.set(false);
      },
    });
  }

  protected filtrar(valor: string): void {
    this.filtroCliente.set(valor ? Number(valor) : null);
    this.cargar();
  }

  protected titular(producto: Producto): string {
    const cliente = this.clientesPorId().get(producto.clienteId);
    return cliente ? `${cliente.nombres} ${cliente.apellido}` : `Cliente ${producto.clienteId}`;
  }

  protected cambiarEstado(producto: Producto, estado: EstadoCuenta): void {
    this.aviso.set('');
    this.error.set('');

    this.servicio.cambiarEstado(producto.id, estado).subscribe({
      next: (actualizado) => {
        this.aviso.set(`La cuenta ${actualizado.numeroCuenta} quedó ${actualizado.estado}.`);
        this.cargar();
      },
      // El backend rechaza cancelar una cuenta con saldo o reactivar una cancelada.
      error: (e) => this.error.set(mensajeDeError(e)),
    });
  }

  protected alternarGmf(producto: Producto): void {
    this.aviso.set('');
    this.error.set('');

    this.servicio.actualizarExencionGmf(producto.id, !producto.exentaGmf).subscribe({
      next: () => this.cargar(),
      error: (e) => this.error.set(mensajeDeError(e)),
    });
  }

  protected eliminar(producto: Producto): void {
    if (!confirm(`¿Eliminar la cuenta ${producto.numeroCuenta}?`)) {
      return;
    }

    this.aviso.set('');
    this.error.set('');

    this.servicio.eliminar(producto.id).subscribe({
      next: () => {
        this.aviso.set('Producto eliminado.');
        this.cargar();
      },
      error: (e) => this.error.set(mensajeDeError(e)),
    });
  }

  protected claseEstado(estado: EstadoCuenta): string {
    return `etiqueta etiqueta--${estado.toLowerCase()}`;
  }
}
