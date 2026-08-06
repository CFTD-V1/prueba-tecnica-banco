import { FormControl } from '@angular/forms';

import { mayorDeEdad } from './formulario-cliente';

describe('validador mayorDeEdad', () => {
  function fechaHaceAnios(anios: number, dias = 0): string {
    const fecha = new Date();
    fecha.setFullYear(fecha.getFullYear() - anios);
    fecha.setDate(fecha.getDate() - dias);
    return fecha.toISOString().slice(0, 10);
  }

  it('acepta a una persona mayor de edad', () => {
    const control = new FormControl(fechaHaceAnios(30));

    expect(mayorDeEdad(control)).toBeNull();
  });

  it('rechaza a una persona menor de edad', () => {
    const control = new FormControl(fechaHaceAnios(10));

    expect(mayorDeEdad(control)).toEqual({ menorDeEdad: true });
  });

  it('acepta a quien acaba de cumplir 18 años', () => {
    const control = new FormControl(fechaHaceAnios(18, 1));

    expect(mayorDeEdad(control)).toBeNull();
  });

  it('rechaza a quien cumple 18 mañana', () => {
    const fecha = new Date();
    fecha.setFullYear(fecha.getFullYear() - 18);
    fecha.setDate(fecha.getDate() + 1);
    const control = new FormControl(fecha.toISOString().slice(0, 10));

    expect(mayorDeEdad(control)).toEqual({ menorDeEdad: true });
  });

  it('no valida cuando el campo está vacío: de eso se encarga el required', () => {
    expect(mayorDeEdad(new FormControl(''))).toBeNull();
  });
});
