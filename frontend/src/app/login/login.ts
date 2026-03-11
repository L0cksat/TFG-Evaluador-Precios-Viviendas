import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import { AuthService, LoginRequest } from '../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class LoginComponent {
  credentials: LoginRequest = { email: '', password: '' };
  errorMessage: string = '';

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit() {
    this.authService.login(this.credentials).subscribe({
      next: (response) => {
        // Here you typically save the token to localStorage/sessionStorage
        localStorage.setItem('token', response.token);
        
        // Redirect to home or dashboard after successful login
        this.router.navigate(['/']); 
      },
      error: (err) => {
        console.error('Error during login:', err);
        this.errorMessage = 'Credenciales inválidas. Por favor, inténtelo de nuevo.';
      }
    });
  }
}