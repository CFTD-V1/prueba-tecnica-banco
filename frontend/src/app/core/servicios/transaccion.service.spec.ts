import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { Transaccion } from '../modelos/transaccion';
import { TransaccionService } from './transaccion.service';

describe('TransaccionService', () => {
  const url = `${environment.urlApi}/transacciones`;
  let servicio: TransaccionService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(TransaccionService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('registra una consignación en su propia subruta', () => {
    servicio.consignar({ productoId: 1, monto: 50000, descripcion: 'Nómina' }).subscribe();

    const peticion = http.expectOne(`${url}/consignaciones`);
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.body).toEqual({ productoId: 1, monto: 50000, descripcion: 'Nómina' });

    peticion.flush({});
  });

  it('registra un retiro en su propia subruta', () => {
    servicio.retirar({ productoId: 1, monto: 20000 }).subscribe();

    const peticion = http.expectOne(`${url}/retiros`);
    expect(peticion.request.method).toBe('POST');

    peticion.flush({});
  });

  it('la transferencia devuelve los dos movimientos generados', () => {
    let recibidos: Transaccion[] | undefined;

    servicio
      .transferir({ productoOrigenId: 1, productoDestinoId: 2, monto: 30000 })
      .subscribe((t) => (recibidos = t));

    const peticion = http.expectOne(`${url}/transferencias`);
    expect(peticion.request.method).toBe('POST');

    peticion.flush([
      { id: 1, naturaleza: 'DEBITO', productoId: 1, referencia: 'abc' },
      { id: 2, naturaleza: 'CREDITO', productoId: 2, referencia: 'abc' },
    ]);

    expect(recibidos?.length).toBe(2);
    expect(recibidos?.[0].naturaleza).toBe('DEBITO');
    expect(recibidos?.[1].naturaleza).toBe('CREDITO');
    expect(recibidos?.[0].referencia).toBe(recibidos?.[1].referencia);
  });

  it('pide el estado de cuenta filtrando por producto', () => {
    servicio.listar(9).subscribe();

    const peticion = http.expectOne((r) => r.url === url && r.params.get('productoId') === '9');
    expect(peticion.request.method).toBe('GET');

    peticion.flush([]);
  });
});
