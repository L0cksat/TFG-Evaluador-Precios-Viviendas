import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  email: string;
  nombre: string;
  role: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  nombre: string;
  apellidos: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private loginUrl = 'http://localhost:8080/api/auth/login'; 
  private registerUrl = 'http://localhost:8080/api/auth/register';

  constructor(private http: HttpClient, private router: Router) {}

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this.loginUrl, credentials);
  }

  register(userData: RegisterRequest): Observable<any> {
    return this.http.post(this.registerUrl, userData);
  }

  isLoggedIn(): boolean {
    // Checks to see if the token exists in the local storage
    return !!localStorage.getItem('token');
  }

  logout(): void {
    // Deletes the token and redirects to the home or the login page
    localStorage.removeItem('token');
    this.router.navigate(['/login']);
  }
}