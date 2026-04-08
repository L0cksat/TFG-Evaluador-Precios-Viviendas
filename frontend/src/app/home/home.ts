import { Component } from '@angular/core';
import { NgOptimizedImage } from "@angular/common";
import { Router } from '@angular/router';

@Component({
  selector: 'app-body',
  imports: [NgOptimizedImage],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class HomeComponent {
  constructor(private router: Router) {}
  
  direccion: string = '';


  redirectToEvaluador(dir: string) {
    this.router.navigate(['/evaluador']);
  }
}
