import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { Cliente, ClienteRequest } from '../modelos/cliente';
import { ClienteService } from './cliente.service';

/**
 * Se verifica el contrato con el backend sin levantarlo: la URL, el método HTTP
 * y el cuerpo de cada petición.
 */
describe('ClienteService', () => {
  const url = `${environment.urlApi}/clientes`;
  let servicio: ClienteService;
  let http: HttpTestingController;

  const clienteRequest: ClienteRequest = {
    tipoIdentificacion: 'CC',
    numeroIdentificacion: '1020304050',
    nombres: 'Laura',
    apellido: 'Gomez',
    correo: 'laura.gomez@banco.com',
    fechaNacimiento: '1992-07-08',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(ClienteService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('pide la lista de clientes con GET', () => {
    let recibidos: Cliente[] | undefined;
    servicio.listar().subscribe((c) => (recibidos = c));

    const peticion = http.expectOne(url);
    expect(peticion.request.method).toBe('GET');

    peticion.flush([{ id: 1, ...clienteRequest, fechaCreacion: '', fechaModificacion: null }]);
    expect(recibidos?.length).toBe(1);
  });

  it('crea un cliente con POST y envía el cuerpo recibido', () => {
    servicio.crear(clienteRequest).subscribe();

    const peticion = http.expectOne(url);
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.body).toEqual(clienteRequest);

    peticion.flush({});
  });

  it('actualiza un cliente con PUT sobre su identificador', () => {
    servicio.actualizar(7, clienteRequest).subscribe();

    const peticion = http.expectOne(`${url}/7`);
    expect(peticion.request.method).toBe('PUT');

    peticion.flush({});
  });

  it('consulta un cliente por identificador', () => {
    servicio.obtenerPorId(7).subscribe();

    const peticion = http.expectOne(`${url}/7`);
    expect(peticion.request.method).toBe('GET');

    peticion.flush({});
  });

  it('elimina un cliente con DELETE', () => {
    servicio.eliminar(7).subscribe();

    const peticion = http.expectOne(`${url}/7`);
    expect(peticion.request.method).toBe('DELETE');

    peticion.flush(null);
  });
});
