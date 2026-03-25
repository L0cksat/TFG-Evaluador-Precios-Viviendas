import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { InmobilarioBuscarService } from '../services/inmobilario-buscar';
import { HomeDataService } from '../services/home-data';

@Component({
  selector: 'app-evaluador',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './evaluador.html',
  styleUrl: './evaluador.css'
})
export class EvaluadorComponent implements OnInit {
  formEvaluacion!: FormGroup;
  isLoading: boolean = false;
  resultadoValoracion: any = null; // Variable to hold the backend response

  constructor(
    private fb: FormBuilder, 
    private ibs: InmobilarioBuscarService, 
    private hds: HomeDataService,
    private cdr: ChangeDetectorRef //injected the change detector ref to handle the HTML changes.
  ) {}

 // Add 'valuationMode' to your class properties
valuationMode: 'basic' | 'pro' = 'basic';

ngOnInit(): void {
  const addressPattern = /^[a-zA-ZñÑáéíóúÁÉÍÓÚ\s\.]+ \d+[a-zA-Z]?, [a-zA-ZñÑáéíóúÁÉÍÓÚ\s]+$/;
  // Catastral references in Spain are usually 20 alphanumeric characters
  const rcPattern = /^[0-9A-Z]{20}$/;

  this.formEvaluacion = this.fb.group({
    direccion: [this.hds.direccion, [Validators.required, Validators.pattern(addressPattern)]],
    referenciaCatastral: ['', [Validators.pattern(rcPattern)]], // New field
    metros: ['', [Validators.required, Validators.min(1)]],
    habitaciones: ['', [Validators.required, Validators.min(0)]]
  });
}

// Helper to switch modes and update validators
setMode(mode: 'basic' | 'pro') {
  this.valuationMode = mode;
  const dirCtrl = this.formEvaluacion.get('direccion');
  const rcCtrl = this.formEvaluacion.get('referenciaCatastral');
  const metrosCtrl = this.formEvaluacion.get('metros');

  if (mode === 'pro') {
    rcCtrl?.setValidators([Validators.required, Validators.pattern(/^[0-9A-Z]{20}$/)]);
    dirCtrl?.clearValidators();
    metrosCtrl?.clearValidators();
  } else {
    rcCtrl?.clearValidators();
    dirCtrl?.setValidators([Validators.required, Validators.pattern(/.../ )]); // use your pattern
    metrosCtrl?.setValidators([Validators.required, Validators.min(1)]);
  }
  
  rcCtrl?.updateValueAndValidity();
  dirCtrl?.updateValueAndValidity();
  metrosCtrl?.updateValueAndValidity();
}

  // frontend/src/app/evaluador/evaluador.ts

  onSubmit() {
    if (this.formEvaluacion.valid) {
      this.isLoading = true;
      this.resultadoValoracion = null;
      this.cdr.detectChanges();

      const formValues = this.formEvaluacion.value;
      let payload: any;

      if (this.valuationMode === 'basic') {
        payload = {
          direccion: formValues.direccion,
          metrosCuadrados: formValues.metros,
          habitaciones: formValues.habitaciones
        };
      } else {
        payload = {
          referenciaCatastral: formValues.referenciaCatastral,
          habitaciones: formValues.habitaciones
        };
      }

      // Use the updated service method passing the payload object
      this.ibs.botInmobilario(payload).subscribe({
        next: (response) => {
          console.log('Valoración procesada exitosamente:', response);
          this.resultadoValoracion = response;
          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error al conectar con el backend:', err);
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
    }
  }
}