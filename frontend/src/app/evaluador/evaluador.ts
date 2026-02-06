import { Component, OnInit } from '@angular/core';
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

  constructor(private fb: FormBuilder, private ibs: InmobilarioBuscarService, private hds: HomeDataService) {}

  ngOnInit(): void {
    const direccionDeHome = this.hds.direccion;

    this.formEvaluacion = this.fb.group({
      direccion: [direccionDeHome, Validators.required],
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