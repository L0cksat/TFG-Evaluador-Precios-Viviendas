import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService, RegisterRequest } from '../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class RegisterComponent {
  userData: RegisterRequest = {
    email: '',
    password: '',
    nombre: '',
    apellidos: ''
  };
  errorMessage: string = '';

  constructor(private authService: AuthService, private router: Router) {}

  onRegister() {
    this.authService.register(this.userData).subscribe({
      next: () => {
        // Redirect to login after successful registration
        this.router.navigate(['/login']); 
      },
      error: (err) => {
        console.error('Registration error:', err);
        this.errorMessage = 'Error al registrar el usuario. Verifique los datos.';
      }
    });
  }
}