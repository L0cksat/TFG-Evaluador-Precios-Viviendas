import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { InmobilarioBuscarService } from '../services/inmobilario-buscar';

@Component({
  selector: 'app-evaluador',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './evaluador.html',
  styleUrl: './evaluador.css'
})
export class EvaluadorComponent implements OnInit {
  formEvaluacion!: FormGroup;

  constructor(private fb: FormBuilder, private ibs: InmobilarioBuscarService) {}

  ngOnInit(): void {
    this.formEvaluacion = this.fb.group({
      direccion: ['', Validators.required],
      metros: ['', [Validators.required, Validators.min(1)]],
      habitaciones: ['', [Validators.required, Validators.min(0)]]
    });
  }

  onSubmit() {
    const dir = this.formEvaluacion.value.direccion;
    const metros = this.formEvaluacion.value.metros;
    const habitaciones = this.formEvaluacion.value.habitaciones
    
    console.log(dir + ':' + metros + ':' + habitaciones)
    if (this.formEvaluacion.valid) {

      this.ibs.botInmobilario(dir, metros, habitaciones);
      
    }
  }
}