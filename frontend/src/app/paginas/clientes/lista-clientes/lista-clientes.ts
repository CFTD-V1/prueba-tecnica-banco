import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Cliente } from '../../../core/modelos/cliente';
import { ClienteService } from '../../../core/servicios/cliente.service';
import { mensajeDeError } from '../../../core/servicios/mensaje-error';

@Component({
  selector: 'app-lista-clientes',
  imports: [RouterLink, DatePipe],
  templateUrl: './lista-clientes.html',
})
export class ListaClientes {
  private readonly servicio = inject(ClienteService);

  protected readonly clientes = signal<Cliente[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal('');
  protected readonly aviso = signal('');

  constructor() {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set('');

    this.servicio.listar().subscribe({
      next: (clientes) => {
        this.clientes.set(clientes);
        this.cargando.set(false);
      },
      error: (e) => {
        this.error.set(mensajeDeError(e));
        this.cargando.set(false);
      },
    });
  }

  protected eliminar(cliente: Cliente): void {
    const confirmado = confirm(
      `¿Eliminar a ${cliente.nombres} ${cliente.apellido}? Esta acción no se puede deshacer.`,
    );
    if (!confirmado) {
      return;
    }

    this.aviso.set('');
    this.error.set('');

    this.servicio.eliminar(cliente.id).subscribe({
      next: () => {
        this.aviso.set('Cliente eliminado.');
        this.cargar();
      },
      // El backend impide eliminar un cliente con productos vinculados.
      error: (e) => this.error.set(mensajeDeError(e)),
    });
  }
}
