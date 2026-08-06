import { FormControl } from '@angular/forms';

import { maximoDosDecimales } from './formulario-movimiento';

describe('validador maximoDosDecimales', () => {
  it('acepta un monto entero', () => {
    expect(maximoDosDecimales(new FormControl(50000))).toBeNull();
  });

  it('acepta un monto con dos decimales', () => {
    expect(maximoDosDecimales(new FormControl(1500.75))).toBeNull();
  });

  it('rechaza un monto con tres decimales', () => {
    expect(maximoDosDecimales(new FormControl(100.999))).toEqual({ demasiadosDecimales: true });
  });

  it('no valida cuando el campo está vacío', () => {
    expect(maximoDosDecimales(new FormControl(null))).toBeNull();
    expect(maximoDosDecimales(new FormControl(''))).toBeNull();
  });
});
