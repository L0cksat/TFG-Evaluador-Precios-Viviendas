import { Component } from '@angular/core';
import { NgOptimizedImage } from "@angular/common";
import { Router } from '@angular/router';
import { HomeDataService } from '../services/home-data';

@Component({
  selector: 'app-body',
  imports: [NgOptimizedImage],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class HomeComponent {
  constructor(private router: Router, private hds: HomeDataService) {}
  
  direccion: string = '';


  redirectToEvaluador(dir: string) {
    this.hds.direccion = dir;
    this.router.navigate(['/evaluador']);
  }
}
