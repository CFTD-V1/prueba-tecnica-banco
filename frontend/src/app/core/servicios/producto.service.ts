import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { EstadoCuenta, Producto, ProductoRequest } from '../modelos/producto';

@Injectable({ providedIn: 'root' })
export class ProductoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.urlApi}/productos`;

  listar(clienteId?: number): Observable<Producto[]> {
    const params = clienteId ? new HttpParams().set('clienteId', clienteId) : undefined;
    return this.http.get<Producto[]>(this.url, { params });
  }

  obtenerPorId(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.url}/${id}`);
  }

  crear(producto: ProductoRequest): Observable<Producto> {
    return this.http.post<Producto>(this.url, producto);
  }

  actualizarExencionGmf(id: number, exentaGmf: boolean): Observable<Producto> {
    return this.http.put<Producto>(`${this.url}/${id}`, { exentaGmf });
  }

  cambiarEstado(id: number, estado: EstadoCuenta): Observable<Producto> {
    return this.http.patch<Producto>(`${this.url}/${id}/estado`, { estado });
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
