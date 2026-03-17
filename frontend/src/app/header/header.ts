import { NgOptimizedImage } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink } from "@angular/router";
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-header',
  imports: [NgOptimizedImage, RouterLink],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header {
  // It is public so the HTML can read it
  constructor(public authService: AuthService){}
}
