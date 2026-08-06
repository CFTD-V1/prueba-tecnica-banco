import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { MovimientoRequest, Transaccion, TransferenciaRequest } from '../modelos/transaccion';

@Injectable({ providedIn: 'root' })
export class TransaccionService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.urlApi}/transacciones`;

  /** Sin argumento devuelve todos los movimientos; con productoId, el estado de cuenta. */
  listar(productoId?: number): Observable<Transaccion[]> {
    const params = productoId ? new HttpParams().set('productoId', productoId) : undefined;
    return this.http.get<Transaccion[]>(this.url, { params });
  }

  consignar(movimiento: MovimientoRequest): Observable<Transaccion> {
    return this.http.post<Transaccion>(`${this.url}/consignaciones`, movimiento);
  }

  retirar(movimiento: MovimientoRequest): Observable<Transaccion> {
    return this.http.post<Transaccion>(`${this.url}/retiros`, movimiento);
  }

  /** Devuelve los dos movimientos generados: el débito del origen y el crédito del destino. */
  transferir(transferencia: TransferenciaRequest): Observable<Transaccion[]> {
    return this.http.post<Transaccion[]>(`${this.url}/transferencias`, transferencia);
  }
}
