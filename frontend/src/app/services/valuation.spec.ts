import { TestBed } from '@angular/core/testing';

import { ValuationService } from './valuation';

describe('Valuation', () => {
  let service: ValuationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ValuationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
