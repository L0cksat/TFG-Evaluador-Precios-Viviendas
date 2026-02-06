import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvaluadorComponent } from './evaluador';

describe('Evaluador', () => {
  let component: EvaluadorComponent;
  let fixture: ComponentFixture<EvaluadorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvaluadorComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvaluadorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
