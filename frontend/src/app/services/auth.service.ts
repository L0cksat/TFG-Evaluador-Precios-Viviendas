import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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

  constructor(private http: HttpClient) {}

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this.loginUrl, credentials);
  }

  register(userData: RegisterRequest): Observable<any> {
    return this.http.post(this.registerUrl, userData);
  }
}