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

  // evaluador.ts
ngOnInit(): void {
  const direccionDeHome = this.hds.direccion;
  // Define the regex pattern
  const addressPattern = /^[a-zA-ZñÑáéíóúÁÉÍÓÚ\s\.]+ \d+[a-zA-Z]?, [a-zA-ZñÑáéíóúÁÉÍÓÚ\s]+$/;

  this.formEvaluacion = this.fb.group({
    direccion: [
      direccionDeHome, 
      [
        Validators.required, 
        Validators.pattern(addressPattern)
      ]
    ],
    metros: ['', [Validators.required, Validators.min(1)]],
    habitaciones: ['', [Validators.required, Validators.min(0)]]
  });
}

  onSubmit() {
    if (this.formEvaluacion.valid) {
      const dir = this.formEvaluacion.value.direccion;
      const metros = this.formEvaluacion.value.metros;
      const habitaciones = this.formEvaluacion.value.habitaciones;
      
      this.isLoading = true;
      this.resultadoValoracion = null; // We reset the resultadoValoracion just in case
      this.cdr.detectChanges(); // We fgrce the visual changes at the start of loading.

      this.ibs.botInmobilario(dir, metros, habitaciones).subscribe({
        next: (response) => {
          console.log('Valoración procesada exitosamente:', response);
          this.resultadoValoracion = response;
          this.isLoading = false;

          this.cdr.detectChanges(); //Here we force Angular to show the HTML again
          
          // Optionally: Show a success message or display the data in your HTML
        },
        error: (err) => {
          console.error('Error al conectar con el backend:', err);
          this.isLoading = false;
          this.cdr.detectChanges(); // We visual update it in the case of an error
        }
      });
    }
  }
}