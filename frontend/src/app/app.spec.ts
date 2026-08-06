import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      // El componente usa routerLink, así que necesita el router en la prueba.
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('se construye correctamente', () => {
    const fixture = TestBed.createComponent(App);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('muestra el menú de navegación con las tres secciones', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();

    const enlaces = (fixture.nativeElement as HTMLElement).querySelectorAll('.menu__enlace');
    const textos = Array.from(enlaces).map((e) => e.textContent?.trim());

    expect(textos).toEqual(['Clientes', 'Productos', 'Movimientos']);
  });
});
