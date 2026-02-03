import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Evaluador } from './evaluador';

describe('Evaluador', () => {
  let component: Evaluador;
  let fixture: ComponentFixture<Evaluador>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Evaluador]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Evaluador);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
