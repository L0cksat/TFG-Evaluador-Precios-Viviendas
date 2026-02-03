import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-evaluador',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './evaluador.html',
  styleUrl: './evaluador.css'
})
export class EvaluadorComponent implements OnInit {
  formEvaluacion!: FormGroup;

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.formEvaluacion = this.fb.group({
      direccion: ['', Validators.required],
      metros: ['', [Validators.required, Validators.min(1)]],
      habitaciones: ['', [Validators.required, Validators.min(0)]]
    });
  }

  onSubmit() {
    if (this.formEvaluacion.valid) {
      
    }
  }
}