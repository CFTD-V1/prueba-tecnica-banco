import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { ProductoService } from './producto.service';

describe('ProductoService', () => {
  const url = `${environment.urlApi}/productos`;
  let servicio: ProductoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(ProductoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista todos los productos cuando no se filtra', () => {
    servicio.listar().subscribe();

    const peticion = http.expectOne(url);
    expect(peticion.request.method).toBe('GET');
    expect(peticion.request.params.has('clienteId')).toBe(false);

    peticion.flush([]);
  });

  it('filtra por cliente enviando el parámetro clienteId', () => {
    servicio.listar(4).subscribe();

    const peticion = http.expectOne((r) => r.url === url && r.params.get('clienteId') === '4');
    expect(peticion.request.method).toBe('GET');

    peticion.flush([]);
  });

  it('crea la cuenta sin enviar saldo: la abre el backend en cero', () => {
    servicio.crear({ tipoCuenta: 'AHORROS', clienteId: 1, exentaGmf: true }).subscribe();

    const peticion = http.expectOne(url);
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.body).toEqual({ tipoCuenta: 'AHORROS', clienteId: 1, exentaGmf: true });
    expect(peticion.request.body.saldo).toBeUndefined();
    expect(peticion.request.body.saldoInicial).toBeUndefined();

    peticion.flush({});
  });

  it('cambia el estado con PATCH sobre la subruta de estado', () => {
    servicio.cambiarEstado(3, 'CANCELADA').subscribe();

    const peticion = http.expectOne(`${url}/3/estado`);
    expect(peticion.request.method).toBe('PATCH');
    expect(peticion.request.body).toEqual({ estado: 'CANCELADA' });

    peticion.flush({});
  });

  it('actualiza únicamente la exención del GMF con PUT', () => {
    servicio.actualizarExencionGmf(3, true).subscribe();

    const peticion = http.expectOne(`${url}/3`);
    expect(peticion.request.method).toBe('PUT');
    expect(peticion.request.body).toEqual({ exentaGmf: true });

    peticion.flush({});
  });

  it('elimina un producto con DELETE', () => {
    servicio.eliminar(3).subscribe();

    const peticion = http.expectOne(`${url}/3`);
    expect(peticion.request.method).toBe('DELETE');

    peticion.flush(null);
  });
});
