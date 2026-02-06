import { TestBed } from '@angular/core/testing';

import { InmobilarioBuscarService } from './inmobilario-buscar'

describe('InmobilarioBuscar', () => {
  let service: InmobilarioBuscarService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(InmobilarioBuscarService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
