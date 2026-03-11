import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

// Matching your backend LoginRequest DTO
export interface LoginRequest {
  email: string;
  password: string;
}

// Matching your backend AuthResponse DTO
export interface AuthResponse {
  token: string;
  email: string;
  nombre: string;
  role: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  // Update the port if your Spring Boot app runs on a different one
  private apiUrl = 'http://localhost:8080/api/auth/login'; 

  constructor(private http: HttpClient) {}

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this.apiUrl, credentials);
  }
}