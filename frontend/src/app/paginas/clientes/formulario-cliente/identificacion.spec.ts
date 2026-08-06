import { FormBuilder } from '@angular/forms';

import { edadRazonable, identificacionSegunTipo } from './formulario-cliente';

describe('validador identificacionSegunTipo', () => {
  const fb = new FormBuilder();

  function grupo(tipoIdentificacion: string, numeroIdentificacion: string) {
    return fb.group({ tipoIdentificacion: [tipoIdentificacion], numeroIdentificacion: [numeroIdentificacion] });
  }

  it('acepta una cédula con solo dígitos', () => {
    expect(identificacionSegunTipo(grupo('CC', '1020304050'))).toBeNull();
  });

  it('rechaza una cédula con letras', () => {
    expect(identificacionSegunTipo(grupo('CC', 'ABC12345'))).not.toBeNull();
  });

  it('rechaza una cédula con símbolos o espacios', () => {
    expect(identificacionSegunTipo(grupo('CC', '123-456'))).not.toBeNull();
    expect(identificacionSegunTipo(grupo('CC', '123 456'))).not.toBeNull();
  });

  it('acepta un pasaporte con letras y números', () => {
    expect(identificacionSegunTipo(grupo('PASAPORTE', 'AB123456'))).toBeNull();
  });

  it('rechaza un pasaporte con símbolos', () => {
    expect(identificacionSegunTipo(grupo('PASAPORTE', 'AB-123456'))).not.toBeNull();
  });

  it('no valida el formato mientras el campo esté vacío', () => {
    expect(identificacionSegunTipo(grupo('CC', ''))).toBeNull();
  });
});

describe('validador edadRazonable', () => {
  it('acepta una fecha de nacimiento normal', () => {
    expect(edadRazonable(new FormBuilder().control('1992-07-08'))).toBeNull();
  });

  it('rechaza una fecha que implica más de 120 años', () => {
    expect(edadRazonable(new FormBuilder().control('1850-01-01'))).toEqual({ edadImposible: true });
  });
});
